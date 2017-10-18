package com.vandyke.whosdown.ui.main.view

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.DisplayMetrics
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
import com.vandyke.whosdown.ui.contact.view.ContactActivity
import com.vandyke.whosdown.ui.main.view.peepslist.PeepHolder
import com.vandyke.whosdown.ui.main.view.peepslist.PeepsAdapter
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel
import com.vandyke.whosdown.ui.permissions.PermissionsActivity
import com.vandyke.whosdown.util.addOnPropertyChangedListener
import com.vandyke.whosdown.util.callIntent
import com.vandyke.whosdown.util.getBitmapFromVectorDrawable
import com.vandyke.whosdown.util.textIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), PopupMenu.OnMenuItemClickListener {

    val CONTACT_CODE = 108

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
        peepsList.setHasFixedSize(true)
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            val leftIcon = getBitmapFromVectorDrawable(this@MainActivity, R.drawable.ic_call)
            val rightIcon = getBitmapFromVectorDrawable(this@MainActivity, R.drawable.ic_message)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.RIGHT -> startActivity(callIntent((viewHolder as PeepHolder).number))
                    ItemTouchHelper.LEFT -> startActivity(textIntent((viewHolder as PeepHolder).number))
                }
                peepsList.adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
                    // Get RecyclerView item from the ViewHolder
                    val itemView = viewHolder.itemView

                    val p = Paint()

                    if (dX > 0) {
                        /* Set your color for positive displacement */
                        p.setARGB(255, 4, 201, 59)

                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), p)

                        // Set the image icon for Right swipe
                        c.drawBitmap(leftIcon,
                                itemView.left + convertDpToPx(16).toFloat(),
                                itemView.top + (itemView.bottom - itemView.top - leftIcon.height) / 2f,
                                p)
                    } else {
                        /* Set your color for negative displacement */
                        p.setARGB(255, 249, 203, 87)

                        // Draw Rect with varying left side, equal to the item's right side
                        // plus negative displacement dX
                        c.drawRect(itemView.right + dX, itemView.top.toFloat(),
                                itemView.right.toFloat(), itemView.bottom.toFloat(), p)

                        //Set the image icon for Left swipe
                        c.drawBitmap(rightIcon,
                                itemView.right - convertDpToPx(16) - rightIcon.width.toFloat(),
                                itemView.top + (itemView.bottom - itemView.top - rightIcon.height) / 2f,
                                p)
                    }
                    viewHolder.itemView.alpha = 1f
                    viewHolder.itemView.translationX = dX
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(peepsList)

        /* set swipe refresh for peeps list color and function */
        peepsListSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        peepsListSwipe.setOnRefreshListener {
            viewModel.refreshListeners()
            peepsListSwipe.isRefreshing = false
        }

        /* hide the keyboard when the EditText loses focus */
        userMessage.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /* when enter is pressed on keyboard, update the viewmodel and set status in the db, and clear focus from the message box */
        userMessage.setOnEditorActionListener { textView, i, keyEvent ->
            userMessage.clearFocus()
            viewModel.message.set(userMessage.text.toString())
            viewModel.setUserStatus()
            true
        }

        /* since the downSwitch isn't focusable, need to manually clear focus from the EditText when it's touched */
        downSwitch.setOnTouchListener { view, motionEvent ->
            if (currentFocus is EditText)
                currentFocus.clearFocus()
            false
        }

        // TODO: fix message being overriden
        downSwitch.setOnCheckedChangeListener { compoundButton, b ->
            viewModel.down.set(downSwitch.isChecked)
            viewModel.message.set(userMessage.text.toString())
            viewModel.setUserStatus()
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.contacts -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(intent, CONTACT_CODE)
            }
            R.id.share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_TEXT, "play store url") // TODO: put play store url here
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(shareIntent, "Share Who's Down"))
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTACT_CODE) {
                val uri = data?.data
                val cursor = contentResolver.query(uri,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val intent = Intent(this, ContactActivity::class.java)
                    intent.putExtra("phoneNumber", cursor.getString(0))
                    startActivity(intent)
                }
                cursor.close()
            }
        }
    }

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.main_popup)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    /* cancel all notifications upon resuming */
    override fun onResume() {
        super.onResume()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putStringSet("notifications", mutableSetOf()).apply()
    }

    private fun convertDpToPx(dp: Int): Int {
        return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}
