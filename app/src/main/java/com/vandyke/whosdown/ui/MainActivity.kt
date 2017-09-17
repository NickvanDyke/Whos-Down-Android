package com.vandyke.whosdown.ui

import android.app.Activity
import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityMainBinding
import com.vandyke.whosdown.ui.peepslist.PeepsAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    lateinit var viewModel: ViewModel

    val VERIFY_PHONE_NUMBER = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().signOut()
        /* instantiate the ViewModel and bind it to the view */
        viewModel = ViewModel(application)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.viewModel = viewModel


        val layoutManager = LinearLayoutManager(this)
        peepsList.layoutManager = layoutManager
        peepsList.addItemDecoration(DividerItemDecoration(peepsList.context, layoutManager.orientation))
        peepsList.adapter = PeepsAdapter(viewModel)


        viewModel.down.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    // TODO: animate moving of guideline
                } else {

                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VERIFY_PHONE_NUMBER) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.authorized.set(true)
            }
        }
    }

    fun verifyPhoneNumber(view: View? = null) {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(listOf(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                        .build(),
                VERIFY_PHONE_NUMBER)
    }

    object Binding {
        @JvmStatic
        @BindingAdapter("layout_constraintGuide_percent")
        fun setLayoutConstraintGuideBegin(guideline: Guideline, percent: Float) {
            val params = guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = percent
            guideline.layoutParams = params
        }
    }
}
