package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        edit_password_button.setOnClickListener {

            checkCurrentPassword()
        }
    }

    private fun checkCurrentPassword() {

        var currentPassword = current_password_editText.text.toString()
        if (currentPassword.isEmpty()) {
            current_password_editText.error = resources.getString(R.string.password_required)
            return
        }

        if(user?.email != null){
            val authCredential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            //TODO create a progressbar is better
            user.reauthenticate(authCredential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                    }
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        current_password_editText.error =
                            resources.getString(R.string.invalid_password)
                    }

                }
        }
    }
}
