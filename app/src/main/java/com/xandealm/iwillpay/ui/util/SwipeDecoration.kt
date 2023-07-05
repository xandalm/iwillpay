package com.xandealm.iwillpay.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlin.math.abs
import kotlin.math.round

private const val TAG = "SwipeDecoration"

class SwipeDecoration(context: Context): ItemDecoration() {

    private var mViewHolder: RecyclerView.ViewHolder? = null
    private var mView: FrameLayout = FrameLayout(context)
    private var mPointX = 0f
    private var mPointY = 0f

    init {
        mView.setBackgroundColor(Color.RED)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if(mViewHolder == null) {
            return
        }

        mViewHolder!!.itemView.let {
            mPointX = if(it.x > 0) 0f else it.x + it.width
            mPointY = it.y
            with(mView) {
                val width = round(abs(it.x)).toInt()
                val height = it.height
                measure(
                    MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY)
                )
                layout(
                    round(mPointX).toInt(),
                    round(mPointY).toInt(),
                    round(mPointX + width).toInt(),
                    round(mPointY + height).toInt())
                c.save()
                c.translate(mPointX,mPointY)
                draw(c)
                c.restore()
            }
        }
    }

    fun setViewHolder(viewHolder: RecyclerView.ViewHolder?) {
        mViewHolder = viewHolder
    }

    fun setLayout(view: View) {
        with(mView) {
            removeAllViews()

            view.id = View.generateViewId()

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(view, layoutParams)
        }
    }
}