package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun forgotPassword(view: View) {
        intent = Intent(this, ForgotPassActivity::class.java)
        startActivity(intent)
    }

    fun signUp(view: View) {
        intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun logIn(view: View) {
        intent = Intent(this, DashboardActivity::class.java)
       startActivity(intent)
    }
}
