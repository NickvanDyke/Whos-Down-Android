package com.vandyke.whosdown.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.PermissionsActivity
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityMainBinding
import com.vandyke.whosdown.ui.peepslist.PeepsAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    lateinit var viewModel: ViewModel

    val pixelHeight = Resources.getSystem().displayMetrics.heightPixels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* check for contacts permission and that user is authorized on Firebase, launch permissions activity if either is false */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PermissionsActivity::class.java))
            finish()
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
    }
}
