package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Enable persistence to be sure that realtime database will be updated
        // It must be done before any other usage of the instance.
        // That's why it is here
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
