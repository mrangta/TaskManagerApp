package com.mcc.g22

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception
import java.util.*

class User(val username: String = "", var profileImage: String = "" , var email: String = "") {

    var uid: String = ""
        internal set

    private var imageSizeEnum: AttachmentsManager.ImageSize = AttachmentsManager.ImageSize.FULL

    var imageSize: String = imageSizeEnum.size
        get() { return imageSizeEnum.size }
        set(value) {
            when (value) {
                AttachmentsManager.ImageSize.FULL.size -> {
                    field = value
                    imageSizeEnum = AttachmentsManager.ImageSize.FULL
                }
                AttachmentsManager.ImageSize.LOW.size -> {
                    field = value
                    imageSizeEnum = AttachmentsManager.ImageSize.LOW
                }
                AttachmentsManager.ImageSize.HIGH.size -> {
                    field = value
                    imageSizeEnum = AttachmentsManager.ImageSize.HIGH
                }
                else -> {
                    field = AttachmentsManager.ImageSize.FULL.size
                    imageSizeEnum = AttachmentsManager.ImageSize.FULL
                }
            }
        }

    companion object {
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val database = FirebaseDatabase.getInstance()
            .reference.child("users")

        private var currentUser: User? = null

        fun listenToAuthState(ctx: Context) {
            FirebaseAuth.getInstance().addAuthStateListener {
                val authUser = it.currentUser
                try {
                    val uid = authUser!!.uid
                    database.child(uid).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val user = dataSnapshot.getValue(User::class.java)!!
                                currentUser = user
                                currentUser!!.email = authUser.email ?: ""
                                currentUser!!.uid = uid
                                NotificationsService.startNotificationService(ctx)
                            }
                            else {
                                currentUser = null
                                NotificationsService.stopNotificationService(ctx)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            currentUser = null
                            NotificationsService.stopNotificationService(ctx)
                        }
                    })
                } catch (e: Exception) {
                    currentUser = null
                    NotificationsService.stopNotificationService(ctx)
                }
            }
        }

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser():User?{

            return currentUser
        }

        fun resolveDisplayName(userId: String, onResolved: (displayName: String) -> Unit,
                               onFailure: () -> Unit) {

            database.child(userId).child("username").ref
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        onFailure()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val displayName = dataSnapshot.value as String?
                        if (displayName != null) {
                            onResolved(displayName)
                        } else onFailure()
                    }
                })
        }

        fun showProfileImageOfUserWithId(userId: String, context: Context,
                                         targetImageView: ImageView) {

            database.child(userId).child("profileImage").ref
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val profileImgUrl = dataSnapshot.value as String?
                        if (profileImgUrl != null) {

                            val image = storage.getReference("/Images/$profileImgUrl")
                            Glide.with(context).load(image).into(targetImageView)
                        }
                    }
                })
        }

        /**
         * Find users with display names which start with usernameStart
         *
         * @param usernameStart the beginning of the username to find
         */
        fun searchForUsers(usernameStart: String,
                           onUsersFound: (fullUsernames: Set<Pair<String, String>>) -> Unit,
                           onFailure: () -> Unit,
                           limitNumberOfResults: Int = 50) {
            val usersRef = database.ref

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usersNames = mutableSetOf<Pair<String, String>>()
                    for (usr in dataSnapshot.children) {
                        val username = usr.child("username").value as String
                        if (username.contains(usernameStart))
                            usersNames.add( Pair(usr.key as String, username) )
                    }
                    onUsersFound(usersNames)
                }
            })
        }
    }

    fun setProfileImage(profilePhoto: Uri,
                        onProfileImageUploaded: () -> Unit, onFailure: () -> Unit) {

        val filename = UUID.randomUUID().toString()
        val ref = storage.getReference("/Images/$filename")

        ref.putFile(profilePhoto)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener{
                    profileImage = filename
                    if (uid.isNotEmpty())
                        database.child(uid).child("profileImage").setValue(profileImage)
                    onProfileImageUploaded()
                }.addOnFailureListener { onFailure() }
            }
            .addOnFailureListener{
                onFailure()
            }
    }

    fun showProfileImage(context: Context, targetImageView: ImageView) {
        if (profileImage.isEmpty()) {
            // Load default profile
            // TODO load default profile
        } else {
            // Load from the storage
            val image = storage.getReference("/Images/$profileImage")
            Glide.with(context).load(image).into(targetImageView)
        }
    }

    fun getUserFavorites(onFavoritesReady: (favorites: Set<Project>) -> Unit,
                         onFailure: () -> Unit) {

        database.child(uid).child("favorites").ref
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        onFailure()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val projects = mutableSetOf<Project>()
                        val projectsToDownload = dataSnapshot.childrenCount.toInt()
                        if (projectsToDownload == 0) {
                            onFavoritesReady(projects)
                            return
                        }

                        for (t in dataSnapshot.children) {

                            if (t.key == null) continue
                            val projectId = t.key as String
                            Log.d("" , "project is$projectId")
                            Project.fromProjectId(projectId, {
                                projects.add(it)
                                if (projects.size == projectsToDownload) {
                                    onFavoritesReady(projects)
                                }
                            }, {
                                onFailure()
                                return@fromProjectId
                            })
                        }
                    }
                })
    }

    fun getUsersTasks(onTasksReady: (tasks: Set<Task>) -> Unit, onFailure: () -> Unit) {

        database.child(uid).child("tasks").ref
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tasks = mutableSetOf<Task>()
                    val tasksToDownload = dataSnapshot.childrenCount.toInt()
                    if (tasksToDownload == 0) {
                        onTasksReady(tasks)
                        return
                    }
                    for (t in dataSnapshot.children) {
                        if (t.key == null) continue
                        val taskId = t.key as String
                        Task.getTaskFromDatabase(taskId, {
                            tasks.add(it)
                            if (tasks.size == tasksToDownload) {
                                onTasksReady(tasks)
                            }
                        }, {
                            onFailure()
                            return@getTaskFromDatabase
                        })
                    }
                }
            })
    }

    fun getUsersProjects(onProjectsReady: (projects: Set<Project>) -> Unit, onFailure: () -> Unit) {

        database.child(uid).child("projects").ref
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val projects = mutableSetOf<Project>()
                    val projectsToDownload = dataSnapshot.childrenCount.toInt()
                    if (projectsToDownload == 0) {
                        onProjectsReady(projects)
                        return
                    }
                    for (t in dataSnapshot.children) {
                        if (t.key == null) continue
                        val projectId = t.key as String
                        Project.fromProjectId(projectId, {
                            projects.add(it)
                            if (projects.size == projectsToDownload) {
                                onProjectsReady(projects)
                            }
                        }, {
                            onFailure()
                            return@fromProjectId
                        })
                    }
                }
            })
    }

    fun saveCurrentImageSize() {
        database.child(uid).child("imageSize").setValue(imageSizeEnum.size)
    }

    fun setImageResolutionToFull() {
        imageSizeEnum = AttachmentsManager.ImageSize.FULL
    }

    fun setImageResolutionToLow() {
        imageSizeEnum = AttachmentsManager.ImageSize.LOW
    }

    fun setImageResolutionToHigh() {
        imageSizeEnum = AttachmentsManager.ImageSize.HIGH
    }

    fun getImageSizeAsEnum(): AttachmentsManager.ImageSize {
        return imageSizeEnum
    }

    fun isProjectAdmin(p: Project): Boolean {
        return uid == p.admin
    }
}
