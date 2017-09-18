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

    lateinit var viewModel: ViewModel

    val pixelHeight = Resources.getSystem().displayMetrics.heightPixels

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /* check for contacts permission and that user is authorized on Firebase, launch permissions activity if either is false */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
            return
        }

        /* instantiate the ViewModel and bind it to the view */
        viewModel = ViewModel(application)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.viewModel = viewModel


        val layoutManager = LinearLayoutManager(this)
        peepsList.layoutManager = layoutManager
        peepsList.addItemDecoration(DividerItemDecoration(peepsList.context, layoutManager.orientation))
        peepsList.adapter = PeepsAdapter(viewModel)


        /* add a listener that will change the height of the dividing guideline when down changes */
        viewModel.down.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    val params = guideline.layoutParams as ConstraintLayout.LayoutParams
                    downLayout.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST))
                    params.guideEnd = pixelHeight - downLayout.measuredHeight
                    guideline.layoutParams = params
                } else {
                    val params = guideline.layoutParams as ConstraintLayout.LayoutParams
                    params.guideEnd = 0
                    guideline.layoutParams = params
                }
            }
        })

        userMessage.setOnEditorActionListener { textView, i, keyEvent ->
            userMessage.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(textView.windowToken, 0)
            true
        }
    }

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.main_popup)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.blockContacts -> startActivity(Intent(this, BlockingActivity::class.java))
        }
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
