package com.vandyke.whosdown

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.ui.MainActivity
import kotlinx.android.synthetic.main.activity_permissions.*

class PermissionsActivity : Activity() {

    private val VERIFY_PHONE_NUMBER = 100
    private val REQUEST_CONTACTS_PERMISSION = 93

    private var hasContactsPermission = false
    private var loggedIn = FirebaseAuth.getInstance().currentUser != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        hasContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        checkStatus()
        if (loggedIn) logInButton.visibility = View.GONE
        if (hasContactsPermission) contactsButton.visibility = View.GONE
    }

    fun logIn(view: View? = null) {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(listOf(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                        .build(),
                VERIFY_PHONE_NUMBER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VERIFY_PHONE_NUMBER) {
            if (resultCode == Activity.RESULT_OK) {
                loggedIn = true
                logInButton.visibility = View.GONE
                checkStatus()
            }
        }
    }

    fun requestContactsPermission(view: View? = null) {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray) {
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasContactsPermission = true
                contactsButton.visibility = View.GONE
                checkStatus()
            }
        }
    }

    fun checkStatus() {
        if (hasContactsPermission && loggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}