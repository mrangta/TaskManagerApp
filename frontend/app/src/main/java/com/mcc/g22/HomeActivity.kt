package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.mcc.g22.utils.logout
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
        




    //opening our navigation drawer and also back button
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(Navigation.findNavController(this , R.id.nav_host_fragment), drawer_layout)
    }

}
