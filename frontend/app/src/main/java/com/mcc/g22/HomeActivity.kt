package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.mcc.g22.utils.logout

class HomeActivity : AppCompatActivity() {

    lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        FirebaseAuth.getInstance().signOut()
        logout()

        logoutButton.setOnClickListener {

            val alert = AlertDialog.Builder(this@HomeActivity)
            alert.setTitle("Confirm")
            alert.setMessage(resources.getString(R.string.alertExit))

            alert.setPositiveButton("YES") { dialog, yes ->
                FirebaseAuth.getInstance().signOut()
                logout()
            }
            alert.setNegativeButton("No") { dialog, no ->
            }

            val dialog: AlertDialog = alert.create()
            dialog.show()
        }

    }


}
