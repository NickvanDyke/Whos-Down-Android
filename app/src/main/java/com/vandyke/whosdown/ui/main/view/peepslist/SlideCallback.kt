/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.DisplayMetrics
import com.vandyke.whosdown.R
import com.vandyke.whosdown.util.Intents
import com.vandyke.whosdown.util.getBitmapFromVectorDrawable

class SlideCallback(val context: Context, private val list: RecyclerView) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private val leftIcon = getBitmapFromVectorDrawable(context, R.drawable.ic_call)
    private val rightIcon = getBitmapFromVectorDrawable(context, R.drawable.ic_message)
    private val leftColor = Paint()
    private val rightColor = Paint()
    private val topShadow = context.resources.getDrawable(R.drawable.top_shadow, null)
    private val bottomShadow = context.resources.getDrawable(R.drawable.bottom_shadow, null)

    init {
        leftColor.setARGB(255, 4, 201, 59)
        rightColor.setARGB(255, 249, 203, 87)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.RIGHT -> context.startActivity(Intents.call((viewHolder as PeepHolder).phoneNumber))
            ItemTouchHelper.LEFT -> context.startActivity(Intents.text((viewHolder as PeepHolder).phoneNumber))
        }
        list.postDelayed({ list.adapter.notifyItemChanged(viewHolder.adapterPosition) }, 1000)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
            val itemView = viewHolder.itemView

            val rect: Rect
            if (dX > 0) {
                rect = Rect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                c.drawRect(rect, leftColor)
                c.drawBitmap(leftIcon,
                        itemView.left + convertDpToPx(16).toFloat(),
                        itemView.top + (itemView.bottom - itemView.top - leftIcon.height) / 2f,
                        leftColor)
            } else {
                rect = Rect((itemView.right + dX).toInt(), itemView.top, itemView.right, itemView.bottom)
                c.drawRect(rect, rightColor)
                c.drawBitmap(rightIcon,
                        itemView.right - convertDpToPx(16) - rightIcon.width.toFloat(),
                        itemView.top + (itemView.bottom - itemView.top - rightIcon.height) / 2f,
                        rightColor)
            }
            topShadow.setBounds(rect.left, rect.top, rect.right, rect.top + convertDpToPx(4))
            bottomShadow.setBounds(rect.left, rect.bottom - convertDpToPx(4), rect.right, rect.bottom)
            topShadow.draw(c)
            bottomShadow.draw(c)
            viewHolder.itemView.translationX = dX
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}