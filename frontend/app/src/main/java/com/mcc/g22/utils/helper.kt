package com.mcc.g22.utils

import android.content.Context
import android.content.Intent
import com.mcc.g22.LoginActivity
import com.mcc.g22.HomeActivity

/*
* Start HomeActivity after login
* flags are set for not backing again to login activity
* */

fun Context.login(){

    val intent = Intent(this, HomeActivity::class.java).apply {
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