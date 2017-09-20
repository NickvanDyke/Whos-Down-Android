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
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityMainBinding
import com.vandyke.whosdown.ui.blocking.BlockingActivity
import com.vandyke.whosdown.ui.main.view.peepslist.PeepsAdapter
import com.vandyke.whosdown.ui.main.viewmodel.ViewModel
import com.vandyke.whosdown.ui.permissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity(), PopupMenu.OnMenuItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        /* check for contacts permission and that user is authorized on Firebase, launch permissions activity if either is false */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
            return
        }

        /* instantiate the ViewModel and bind the view to it */
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val viewModel = ViewModel(application)
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
        viewModel.down.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    // TODO: maybe fade the logo or something. Should wait until I have an actual logo to see what looks best
                    val resizeAnimation = ResizeAnimation(downLayout, pixelHeight, downWrapContentHeight)
                    resizeAnimation.duration = 300
                    resizeAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {
                            downLogo.visibility = View.INVISIBLE
                        }
                    })

                    downLayout.startAnimation(resizeAnimation)
                } else {
                    val resizeAnimation = ResizeAnimation(downLayout, downWrapContentHeight, pixelHeight)
                    resizeAnimation.duration = 300
                    resizeAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            downLogo.visibility = View.VISIBLE
                        }
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                    })

                    downLayout.startAnimation(resizeAnimation)
                }
            }
        })

        /* set RecyclerView stuff */
        peepsList.addItemDecoration(DividerItemDecoration(peepsList.context, (peepsList.layoutManager as LinearLayoutManager).orientation))
        peepsList.adapter = PeepsAdapter(viewModel)

        /* set swipe refresh for peeps list color and function */
        peepsListSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        peepsListSwipe.setOnRefreshListener {
            viewModel.refreshListeners()
            peepsListSwipe.isRefreshing = false
        }

        /* update the viewModel's message when the EditText loses focus (which is caused when somewhere else is touched, or done is pressed on kb) */
        userMessage.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            viewModel.message.set((view as EditText).text.toString())
        }

        /* close the keyboard and clear focus from message box when done is pressed on the keyboard */
        userMessage.setOnEditorActionListener { textView, i, keyEvent ->
            userMessage.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(textView.windowToken, 0)
            true
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.blockContacts -> startActivity(Intent(this, BlockingActivity::class.java))
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
