package com.vandyke.whosdown.ui.requirements

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.vandyke.whosdown.R
import com.vandyke.whosdown.ui.main.view.MainActivity
import com.vandyke.whosdown.util.getBitmapFromVectorDrawable
import kotlinx.android.synthetic.main.activity_requirements.*

class RequirementsActivity : Activity() {

    private val VERIFY_PHONE_NUMBER = 173
    private val REQUEST_CONTACTS_PERMISSION = 93

    private var hasContactsPermission = false
    private var loggedIn = FirebaseAuth.getInstance().currentUser != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requirements)
        hasContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        checkStatus()
        /* need to set these programmatically for some reason */
        verifyButton.setBackgroundResource(R.drawable.button_shape_no_fill)
        contactsButton.setBackgroundResource(R.drawable.button_shape_no_fill)

        if (loggedIn)
            verifyButton.visibility = View.GONE
        if (hasContactsPermission)
            contactsButton.visibility = View.GONE
    }

    fun verify(view: View? = null) {
        verifyButton.startAnimation()
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
                verifyButton.doneLoadingAnimation(ContextCompat.getColor(this, R.color.white),
                        getBitmapFromVectorDrawable(this, R.drawable.ic_check))
                val fadeAnimation = AlphaAnimation(1.0f, 0.0f)
                fadeAnimation.duration = 600
                fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        verifyButton.visibility = View.INVISIBLE
                        checkStatus()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {}
                })
                verifyButton.startAnimation(fadeAnimation)
            } else {
                verifyButton.revertAnimation()
            }
        }
    }

    fun requestContactsPermission(view: View? = null) {
        contactsButton.startAnimation()
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACTS_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray) {
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasContactsPermission = true
                contactsButton.doneLoadingAnimation(ContextCompat.getColor(this, R.color.white),
                        getBitmapFromVectorDrawable(this, R.drawable.ic_check))
                val fadeAnimation = AlphaAnimation(1.0f, 0.0f)
                fadeAnimation.duration = 600
                fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        contactsButton.visibility = View.INVISIBLE
                        checkStatus()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {}
                })
                contactsButton.startAnimation(fadeAnimation)
            } else {
                contactsButton.revertAnimation()
            }
        }
    }

    fun checkStatus() {
        if (hasContactsPermission && loggedIn) {
            FirebaseDatabase.getInstance().reference.child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
                    .child("notificationToken")
                    .setValue(FirebaseInstanceId.getInstance().token)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        verifyButton.dispose()
        contactsButton.dispose()
    }
}