package com.mcc.g22

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class User(val username: String) {
    var profileImage: String = ""
        private set

    var projects: Set<String> = mutableSetOf()
        private set

    companion object {
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser(): User? {
            return User("test user")
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
            database.reference.child("users").child(username).child("profile_image")
                .setValue(profileImage)
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
