package com.vandyke.whosdown.ui.main.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ShareActionProvider
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityMainBinding
import com.vandyke.whosdown.ui.blocking.BlockingActivity
import com.vandyke.whosdown.ui.main.view.peepslist.PeepsAdapter
import com.vandyke.whosdown.ui.main.viewmodel.ViewModel
import com.vandyke.whosdown.ui.permissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    lateinit var viewModel: ViewModel

    lateinit var shareProvider: ShareActionProvider

    val pixelHeight = Resources.getSystem().displayMetrics.heightPixels
    val downHeight = (pixelHeight * 0.25).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* set action bar title to nothing and overlay it */
        window.addFlags(Window.FEATURE_ACTION_BAR_OVERLAY)
        actionBar.title = ""

        /* check for contacts permission and that user is authorized on Firebase, launch permissions activity if either is false */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
            return
        }

        /* instantiate the ViewModel and bind the view to it */
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewModel = ViewModel(application)
        binding.viewModel = viewModel

        /* make the downLayout start as filling the entire screen */
        val params = downLayout.layoutParams as ConstraintLayout.LayoutParams
        params.height = pixelHeight
        downLayout.layoutParams = params

        /* set RecyclerView stuff */
        val layoutManager = LinearLayoutManager(this)
        peepsList.layoutManager = layoutManager
        peepsList.addItemDecoration(DividerItemDecoration(peepsList.context, layoutManager.orientation))
        peepsList.adapter = PeepsAdapter(viewModel)

        /* set swipe refresh for peeps list color and function */
        peepsListSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))
        peepsListSwipe.setOnRefreshListener {
            viewModel.refreshListeners()
            peepsListSwipe.isRefreshing = false
        }

        downSwitch.setOnCheckedChangeListener { compoundButton, b ->
            viewModel.onDownSwitchClicked(b)
        }


        /* add a listener that will animate changing the height of the downLayout when down changes */
        viewModel.down.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    val resizeAnimation = ResizeAnimation(downLayout, pixelHeight, downHeight)
                    resizeAnimation.duration = 300

                    val fadeAnimation = AlphaAnimation(1.0f, 0.0f)
                    fadeAnimation.duration = 300
                    fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            downLogo.visibility = View.INVISIBLE
                        }
                        override fun onAnimationRepeat(p0: Animation?) {}
                        override fun onAnimationStart(p0: Animation?) {}
                    })

                    downLayout.startAnimation(resizeAnimation)
                    downLogo.startAnimation(fadeAnimation)
                } else {
                    val resizeAnimation = ResizeAnimation(downLayout, downHeight, pixelHeight)
                    resizeAnimation.duration = 300

                    val fadeAnimation = AlphaAnimation(0.0f, 1.0f)
                    fadeAnimation.duration = 300
                    fadeAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            downLogo.visibility = View.VISIBLE
                        }
                        override fun onAnimationRepeat(p0: Animation?) {}
                        override fun onAnimationStart(p0: Animation?) {}
                    })

                    downLayout.startAnimation(resizeAnimation)
                    downLogo.startAnimation(fadeAnimation)
                }
            }
        })

        /* update viewModel message, close the keyboard and clear focus from message box when done is pressed on the keyboard */
        userMessage.setOnEditorActionListener { textView, i, keyEvent ->
            userMessage.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(textView.windowToken, 0)
            viewModel.message.set(textView.text.toString())
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.blockContacts -> startActivity(Intent(this, BlockingActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_action_bar, menu)
        shareProvider = menu.findItem(R.id.share).actionProvider as ShareActionProvider
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "play store url") // TODO: put play store url here
        shareIntent.type = "text/plain"
        shareProvider.setShareIntent(shareIntent)
        return true
    }

    /* clears EditText focus and hides keyboard when tapping anywhere else */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
