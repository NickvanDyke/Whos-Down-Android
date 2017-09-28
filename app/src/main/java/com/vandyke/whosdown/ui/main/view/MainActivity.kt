package com.vandyke.whosdown.ui.main.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityMainBinding
import com.vandyke.whosdown.ui.contacts.ContactsActivity
import com.vandyke.whosdown.ui.main.view.peepslist.PeepsAdapter
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel
import com.vandyke.whosdown.ui.permissions.PermissionsActivity
import com.vandyke.whosdown.util.addOnPropertyChangedListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity(), PopupMenu.OnMenuItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        /* check for contacts permission and that user is authorized on Firebase, launch permissions activity if either is false */
        // TODO: also check for google play services availability, and make it downloadable in the permissionsactivity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
            return
        }

        /* instantiate the MainViewModel and bind the view to it */
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val viewModel = MainViewModel(application)
        binding.viewModel = viewModel

        /* make the downLayout start as filling the entire screen */
        val pixelHeight = Resources.getSystem().displayMetrics.heightPixels
        val params = downLayout.layoutParams as ConstraintLayout.LayoutParams
        params.height = pixelHeight
        downLayout.layoutParams = params

        /* calculate the wrap_content height of downLayout for when it's moved */
        downLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val downWrapContentHeight = downLayout.measuredHeight

        /* add a listener that will animate changing the height of the downLayout when down changes */
        viewModel.down.addOnPropertyChangedListener { sender, propertyId ->
            if ((sender as ObservableBoolean).get()) {
                val resizeAnimation = ResizeAnimation(downLayout, pixelHeight, downWrapContentHeight)
                resizeAnimation.duration = 300

                val fadeAnimation = AlphaAnimation(1.0f, 0.0f)
                fadeAnimation.duration = 300
                fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        downLogo.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {
                    }
                })

                downLogo.startAnimation(fadeAnimation)
                downLayout.startAnimation(resizeAnimation)
            } else {
                val resizeAnimation = ResizeAnimation(downLayout, downWrapContentHeight, pixelHeight)
                resizeAnimation.duration = 300

                val fadeAnimation = AlphaAnimation(0.0f, 1.0f)
                fadeAnimation.duration = 300
                fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {
                        downLogo.visibility = View.VISIBLE
                    }
                })

                downLogo.startAnimation(fadeAnimation)
                downLayout.startAnimation(resizeAnimation)
            }
        }

        /* set peeps list stuff */
//        peepsList.addItemDecoration(DividerItemDecoration(peepsList.context, (peepsList.layoutManager as LinearLayoutManager).orientation))
        peepsList.adapter = PeepsAdapter(viewModel)

        /* set swipe refresh for peeps list color and function */
        peepsListSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        peepsListSwipe.setOnRefreshListener {
            viewModel.refreshListeners()
            peepsListSwipe.isRefreshing = false
        }

        /* update the viewModel's message and hide the keyboard when the EditText loses focus */
        userMessage.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            viewModel.message.set((view as EditText).text.toString())
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /* close the keyboard and clear focus from message box when done is pressed on the keyboard */
        userMessage.setOnEditorActionListener { textView, i, keyEvent ->
            userMessage.clearFocus()
            true
        }

        /* since the downSwitch isn't focusable, need to manually clear focus from the EditText when it's touched */
        downSwitch.setOnTouchListener { view, motionEvent ->
            if (currentFocus is EditText)
                currentFocus.clearFocus()
            false
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.contacts -> startActivity(Intent(this, ContactsActivity::class.java))
            R.id.share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_TEXT, "play store url") // TODO: put play store url here
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(shareIntent, "Share Who's Down"))
            }
        }
        return true
    }

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.main_popup)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }
}
