package com.mcc.g22.utils

import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.EditText
import com.mcc.g22.DashboardActivity
import com.mcc.g22.LoginActivity
import com.mcc.g22.HomeActivity
import com.mcc.g22.R

/*
* Start HomeActivity after login
* flags are set for not backing again to login activity
* */

fun Context.login(){

    val intent = Intent(this, DashboardActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}


fun Context.logout() {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.checkFormatEmail( email : String , view : EditText): Boolean {

    if (email.isEmpty()) {
        view.error = resources.getString(R.string.email_required)
        view.requestFocus()
        return false
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        view.error = resources.getString(R.string.invalid_mail)
        view.requestFocus()
        return false
    }
    return true
}