package com.mcc.g22

import android.content.Context
import android.net.Uri
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

class User(val username: String, var profileImage: String = "" , email :String) {


    var uid: String = ""
        private set

    companion object {
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val database = FirebaseDatabase.getInstance()
            .reference.child("users")

        private var currentUser: User? = null

        fun listenToAuthState(ctx: Context) {
            FirebaseAuth.getInstance().addAuthStateListener {
                val authUser = it.currentUser
                try {
                    currentUser = User(authUser!!.displayName!!, authUser.photoUrl!!.toString(),  authUser.email!! )
                    currentUser!!.uid = authUser.uid
                    NotificationsService.startNotificationService(ctx)
                } catch (e: Exception) {
                    currentUser = null
                    NotificationsService.stopNotificationService(ctx)
                }
            }
        }

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser(): User? {

            return currentUser
        }

        fun resolveDisplayName(userId: String, onResolved: (displayName: String) -> Unit,
                               onFailure: () -> Unit) {

            database.child(userId).child("displayName").ref
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
                            Glide.with(context).load(profileImgUrl).into(targetImageView)
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
                           onUsersFound: (fullUsernames: Set<String>) -> Unit,
                           onFailure: () -> Unit,
                           limitNumberOfResults: Int = 50) {
            val usersRef = database.orderByKey().startAt(usernameStart)
                                .endAt(usernameStart + "\uf8ff")
                                .limitToFirst(limitNumberOfResults)
                                .ref

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usersNames = mutableSetOf<String>()
                    for (usr in dataSnapshot.children) {
                        usersNames.add( usr.key as String )
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

    fun getUserFavorites(onFavoritesReady: (favorites: Set<String>) -> Unit,
                         onFailure: () -> Unit) {

        database.child(uid).child("favorites").ref
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val favs = mutableSetOf<String>()
                    for (f in dataSnapshot.children) {
                        val fav = f.key
                        if (fav != null) favs.add(fav)
                    }
                    onFavoritesReady(favs)
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
}
