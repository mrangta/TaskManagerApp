package com.mcc.g22

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mcc.g22.utils.checkFormatEmail
import com.mcc.g22.utils.login
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerAuth = FirebaseAuth.getInstance()

        signUp_register_button.setOnClickListener {
            registerUser()
        }

        back_login_register.setOnClickListener{
            startActivity(Intent(this@RegisterActivity , RegisterActivity :: class.java))
        }

    }

    private fun registerUser(){

        val email = email_register_editText.text.toString()
        val password = password_register_editText.text.toString()

        if (!checkFormatEmail(email ,email_register_editText))
            return

        if (password.isEmpty() || password.length < 6){
            password_register_editText.error = resources.getString(R.string.password_check_login)
            password_register_editText.requestFocus()
            return
        }

        progressbar_register.visibility = View.VISIBLE

        registerAuth.createUserWithEmailAndPassword(email , password)
            .addOnCompleteListener(this){task ->
                progressbar_register.visibility = View.GONE
                if(task.isSuccessful){
                    Toast.makeText(this, resources.getString(R.string.create_user), Toast.LENGTH_SHORT).show()
                    login()
                }
                else{
                    Toast.makeText(this, resources.getString(R.string.failed_create_user), Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        registerAuth.currentUser?.let {
            login()
        }
    }
}