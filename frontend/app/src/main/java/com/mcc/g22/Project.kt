package com.mcc.g22

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mcc.g22.apiclient.ApiClient
import com.mcc.g22.apiclient.infrastructure.ClientException
import com.mcc.g22.apiclient.infrastructure.ServerException
import com.mcc.g22.apiclient.models.InlineObject
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class Project {

    var projectId: String = ""
        private set

    var name: String = ""
        private set

    var isPrivate: Boolean = false
        private set

    var creationDate: Instant = Instant.now()
        private set

    var lastModificationDate: Instant = Instant.now()
        private set

    var description: String = ""
        private set

    var admin: String = ""
        private set

    var deadline: Instant? = null
        private set

    var badgeUrl: String? = null
        private set

    var membersIds: Set<String> = mutableSetOf()
        private set

    var keywords: Set<String> = mutableSetOf()
        private set

    var tasksIds: Set<String> = mutableSetOf()
        private set

    lateinit var attachmentsManager: AttachmentsManager
        private set

    companion object {
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()

        private val projectsRef = FirebaseDatabase.getInstance().reference.
            child("projects")

        fun createProject(name: String, isPrivate: Boolean, description: String,
                          keywords: Array<String>, members: Array<User>,
                          onProjectCreated: (p: Project) -> Unit,
                          onFailure: () -> Unit,
                          deadline: Instant? = null,
                          badge: Uri? = null) {
            val p = Project()
            p.creationDate = Instant.now()
            p.name = name
            p.admin = User.getRegisteredUser()!!.uid
            p.description = description
            p.isPrivate = isPrivate
            p.deadline = deadline

            val ks = mutableSetOf<String>()
            for (k in keywords) {
                ks.add(k)
            }
            p.keywords = ks

            val memsIds = mutableSetOf<String>()
            for (i in members) {
                memsIds.add(i.uid)
            }
            p.membersIds = memsIds

            thread {
                val apiProject = com.mcc.g22.apiclient.models.Project(
                    name = p.name,
                    description = p.description, private = isPrivate, badgeUrl = null,
                    keywords = keywords, deadline = p.deadline?.toString()
                )

                try {
                    p.projectId = ApiClient.api.createProject(apiProject).id
                    p.addMembers(members)
                    if (badge == null) {
                        onProjectCreated(p)
                        return@thread
                    }
                } catch (e: Exception) {
                    onFailure()
                    return@thread
                }

                p.changeBadge(badge, {
                    onProjectCreated(p)
                }, {
                    onFailure()
                })
            }
        }

        /**
         * Fetch from the database all projects which has the currently logged user as a member.
         *
         * This function is blocking!
         */
        fun getAllUsersProjects(onProjectsFound: (projects: Set<Project>) -> Unit,
                                onFailure: () -> Unit) {
            val user = User.getRegisteredUser() ?: return

            val projects = mutableSetOf<Project>()
            val latch = CountDownLatch(user.projects.size)

            for (projectId in user.projects) {
                projectsRef.child(projectId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        onFailure()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        projects.add(buildProjectFromDatabase(dataSnapshot))
                        latch.countDown()
                    }
                })
            }
            latch.await()
            onProjectsFound(projects)
        }

        fun fromProjectId(projectId: String, onProjectFound: (p: Project) -> Unit,
                          onFailure: () -> Unit) {
            projectsRef.child(projectId).ref
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        onFailure()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        onProjectFound( buildProjectFromDatabase(p0) )
                    }

                })
        }

        fun buildProjectFromDatabase(dataSnapshot: DataSnapshot): Project {
            val p = Project()

            p.projectId = dataSnapshot.key as String
            p.name = dataSnapshot.child("name").value as String
            p.isPrivate = dataSnapshot.child("private").value as Boolean
            p.description = dataSnapshot.child("description").value as String
            p.admin = dataSnapshot.child("admin").value as String
            p.badgeUrl = dataSnapshot.child("badgeUrl").value as String?
            p.creationDate =
                Instant.parse( dataSnapshot.child("creationDate").value as String )

            val lastMod = dataSnapshot.child("lastModificationDate").value as String?
            if (lastMod == null) {
                p.lastModificationDate = p.creationDate
            } else {
                p.lastModificationDate = Instant.parse(lastMod)
            }

            val deadlineStr = dataSnapshot.child("deadline").value as String?
            if (deadlineStr != null) {
                p.deadline = Instant.parse(deadlineStr)
            }

            val mutableSetOfMembers = mutableSetOf<String>()
            if (dataSnapshot.hasChild("members")) {
                for (m in dataSnapshot.child("members").children) {
                    mutableSetOfMembers.add(m.key as String)
                }
            }
            p.membersIds = mutableSetOfMembers

            val mutableSetOfTasks = mutableSetOf<String>()
            for (t in dataSnapshot.child("tasks").children) {
                mutableSetOfTasks.add(t.key as String)
            }
            p.tasksIds = mutableSetOfTasks

            val mutableSetOfKeywords = mutableSetOf<String>()
            if (dataSnapshot.hasChild("keywords")) {
                for (a in dataSnapshot.child("keywords").children) {
                    mutableSetOfKeywords.add(a.key as String)
                }
                p.keywords = mutableSetOfKeywords
            }

            p.attachmentsManager = AttachmentsManager(p.projectId)

            return p
        }
    }

    /**
     * Add new member to the project
     *
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    fun addMembers(newMembers: Array<User>) {
        if (newMembers.isEmpty()) return

        val usersIds = mutableListOf<String>()
        newMembers.forEach { usersIds.add(it.uid) }
        ApiClient.api.addMemberToProject(projectId,
                                            InlineObject(userIds = usersIds.toTypedArray()))

        membersIds = membersIds + usersIds
    }

    /**
     * Delete this project in the backend. The local object is still valid!
     *
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    fun deleteThisProject() {
        ApiClient.api.deleteProjectWithId(projectId)
    }

    fun changeBadge(newBadgeUri: Uri, onBadgeUploaded: () -> Unit, onFailure: () -> Unit) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference(filename)
        val uploading = ref.putFile(newBadgeUri)
        uploading.addOnCanceledListener { onFailure() }
        uploading.addOnCompleteListener {
            ref.downloadUrl.addOnSuccessListener {
                badgeUrl = it.toString()
                projectsRef.child(projectId).child("badgeUrl").setValue(badgeUrl)
                onBadgeUploaded()
            }.addOnFailureListener { onFailure() }
        }
    }

    fun loadBadgeIntoImageView(context: Context, targetImageView: ImageView) {
        if (badgeUrl == null) return
        Glide.with(context).load(badgeUrl).into(targetImageView)
    }
}