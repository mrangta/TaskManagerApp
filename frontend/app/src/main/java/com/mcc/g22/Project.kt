package com.mcc.g22

import android.annotation.SuppressLint
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mcc.g22.apiclient.ApiClient
import com.mcc.g22.apiclient.infrastructure.ClientException
import com.mcc.g22.apiclient.infrastructure.ServerException
import com.mcc.g22.apiclient.models.InlineObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Project {

    var projectId: String = ""
        private set

    var name: String = ""
        private set

    var isPrivate: Boolean = false
        private set

    var creationDate: Date = Date()
        private set

    var lastModificationDate: Date = Date()
        private set

    var description: String = ""
        private set

    var admin: String = ""
        private set

    var deadline: Date = Date()
        private set

    var badge: String = ""
        private set

    lateinit var membersIds: Set<String>
        private set

    lateinit var keywords: Set<String>
        private set

    lateinit var tasksIds: Set<String>
        private set

    lateinit var attachmentsManager: AttachmentsManager
        private set

    companion object {

        private val projectsRef = FirebaseDatabase.getInstance().reference.
            child("projects")

        @SuppressLint("SimpleDateFormat")
        private val dataFormat: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        /**
         * Fetch from the database all projects which has the currently logged user as a member.
         */
        fun getAllUsersProjects(onProjectsFound: (projects: Set<Project>) -> Unit,
                                onFailure: () -> Unit) {
            val user = User.getRegisteredUser() ?: return

            val projects = mutableSetOf<Project>()
            projectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (p in dataSnapshot.children) {
                        if (p.child("members").hasChild(user.username)) {
                            projects.add( buildProjectFromDatabase(p) )
                        }
                    }
                    onProjectsFound(projects)
                }
            })
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
            p.badge = dataSnapshot.child("badge").value as String

            p.creationDate =
                dataFormat.parse( dataSnapshot.child("creation_date").value as String ) as Date
            p.lastModificationDate =
                dataFormat.parse( dataSnapshot.child("last_modification_date").value as String ) as Date
            p.deadline =
                dataFormat.parse( dataSnapshot.child("deadline").value as String ) as Date

            val mutableSetOfMembers = mutableSetOf<String>()
            for (m in dataSnapshot.child("members").children) {
                mutableSetOfMembers.add(m.key as String)
            }
            p.membersIds = mutableSetOfMembers

            val mutableSetOfTasks = mutableSetOf<String>()
            for (t in dataSnapshot.child("tasks").children) {
                mutableSetOfTasks.add(t.key as String)
            }
            p.tasksIds = mutableSetOfTasks

            val mutableSetOfKeywords = mutableSetOf<String>()
            for (a in dataSnapshot.child("keywords").children) {
                mutableSetOfKeywords.add(a.key as String)
            }
            p.keywords = mutableSetOfKeywords

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
        newMembers.forEach { usersIds.add(it.username) }
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
}