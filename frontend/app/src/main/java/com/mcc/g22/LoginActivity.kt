package com.mcc.g22

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mcc.g22.utils.login
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loginAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginAuth = FirebaseAuth.getInstance()
        login_button.setOnClickListener {
            loginUser()
        }

        forgotPassword_textView.setOnClickListener{
            startActivity(Intent(this@LoginActivity , ResetPasswordActivity :: class.java))
        }

        create_account_login_textView.setOnClickListener{
            startActivity(Intent(this@LoginActivity , ResetPasswordActivity :: class.java))
        }


    }

    private fun loginUser(){

        val email = email_login_editText.text.toString()
        val password = password_login_editText.text.toString()

        if(email.isEmpty()){
            email_login_editText.error = "Email Required"
            email_login_editText.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 6){
            password_login_editText.error = "The Password must be at least 6 characters"
            password_login_editText.requestFocus()
            return
        }
        progressbar_login.visibility = View.VISIBLE

        loginAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->     // result, if successful login

                progressbar_login.visibility = View.GONE
                if(task.isSuccessful)
                   login()
            }
            .addOnFailureListener {
            Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        loginAuth.currentUser?.let {
            login()
        }
    }
}