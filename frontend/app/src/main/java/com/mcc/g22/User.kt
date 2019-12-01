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

class User(val username: String) {
    var profileImage: String = ""
        private set

    var projects: Set<String> = mutableSetOf()
        private set

    companion object {
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val database = FirebaseDatabase.getInstance()
            .reference.child("users")

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser(): User? {
            return User("test user")
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

    fun setProfileImage(newProfileImage: Uri,
                        onProfileImageUploaded: () -> Unit, onFailure: () -> Unit) {
        val tmpImage = newProfileImage.lastPathSegment!!
        val uploading = storage.reference
                    .child("$username/profile_image/$tmpImage").putFile(newProfileImage)
        uploading.addOnCanceledListener { onFailure() }
        uploading.addOnCompleteListener {
            profileImage = tmpImage
            database.child(username).child("profile_image").setValue(profileImage)
            onProfileImageUploaded()
        }
    }

    fun showProfileImage(context: Context, targetImageView: ImageView) {
        if (profileImage.isEmpty()) {
            // Load default profile
            // TODO
        } else {
            // Load from the storage
            val imageRef = storage.reference
                        .child("$username/profile_image/$profileImage")
            Glide.with(context).load(imageRef).into(targetImageView)
        }
    }
}
