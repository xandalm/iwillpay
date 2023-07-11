package com.xandealm.iwillpay.ui.util

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.*

private const val TAG = "SwipeItemHelper"

open class SwipeItemHelper(
    callback: Callback
): RecyclerView.ItemDecoration(),RecyclerView.OnChildAttachStateChangeListener {

    abstract class Callback(directions: Byte) {

        private val mDirs = directions
        val directions get() = mDirs
        abstract fun beforeSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte)
        abstract fun onSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte)
        abstract fun afterSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte)
        abstract fun afterSlidingWithoutSwiped(viewHolder: RecyclerView.ViewHolder, direction: Byte)
        abstract fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Byte)
        abstract fun onRestoreSwiped(viewHolder: RecyclerView.ViewHolder)
    }

    open class SimpleCallback(directions: Byte): Callback(directions) {
        override fun beforeSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
        }

        override fun onSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
        }

        override fun afterSliding(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
        }

        override fun afterSlidingWithoutSwiped(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Byte) {
        }

        override fun onRestoreSwiped(viewHolder: RecyclerView.ViewHolder) {
        }

    }

    private val mScope = CoroutineScope(Job() + Dispatchers.Main)
    private val mCallback: Callback = callback
    private var mDensity: Float? = null
    private lateinit var mRecyclerView: RecyclerView
    private var mTargetViewHolder: RecyclerView.ViewHolder? = null
    private var mTargetViewHolderX: Float? = null
    private var mInitialX = 0f
    private var mInitialY = 0f

    private var mDeltaX = 0f
    private var mDeltaY = 0f

    private val mPosition = PointF()

    private var mActivePointerId = -1
    private var mInvalidatedPointerId = -1

    private var mStatus: Byte = STATUS_IDLE

    private val mAnimations = mutableListOf<CustomAnimator>()
    private val mPendingRestores = mutableListOf<RecyclerView.ViewHolder>()

    private val mDirs get() = mCallback.directions

    private var mVelocityTracker: VelocityTracker? = null

    private val mOnItemTouchListener = object: RecyclerView.SimpleOnItemTouchListener() {

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            var forceIntercept = false
            when(val action = e.actionMasked) {
                MotionEvent.ACTION_DOWN  -> {
                    mActivePointerId = e.getPointerId(0)
                    updateStartPoint(e.x,e.y)
//                    getVelocityTracker()
//                    if(mTargetViewHolder == null) {
//                        mRecyclerView.findChildViewUnder(e.x,e.y)?.let {
//                            val viewHolder = mRecyclerView.getChildViewHolder(it)
//                            val animation = findViewHolderAnimation(viewHolder)
//                            animation?.let {
//                                mInitialX -= animation.final
//                                with(animation.viewHolder) {
//                                    endAnimation(this, true)
//                                    if(mPendingRestores.remove(this))
//                                        restoreItem(this)
//                                    select(this, STATUS_SWIPING)
//                                }
//                                updateDeltas(e,0)
//                            }
//                        }
//                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // check if has blocked swipe
                    if(mInvalidatedPointerId == mActivePointerId)
                        forceIntercept = true
                    mActivePointerId = -1
                    mInvalidatedPointerId = -1
                    select(null, STATUS_IDLE)
                }
                else -> {
                    if(mActivePointerId != -1) {
                        val idx = e.findPointerIndex(mActivePointerId)
                        if(idx >= 0) {
                            checkSelectedToSwipe(action,e,idx)
                        }
                    }
                }
            }
            mVelocityTracker?.addMovement(e)
            return (mTargetViewHolder != null) or forceIntercept
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            mVelocityTracker?.addMovement(e)
            if (mActivePointerId == -1) {
                return
            }
            val action = e.actionMasked
            val activePointerIndex = e.findPointerIndex(mActivePointerId)
            if (activePointerIndex >= 0) {
                checkSelectedToSwipe(action, e, activePointerIndex)
            }
            if(mTargetViewHolder == null)
                return
            when(action) {
                MotionEvent.ACTION_MOVE -> {
                    if(activePointerIndex >= 0) {
                        touchMove(e,activePointerIndex)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    select(null, STATUS_IDLE)
                    mActivePointerId = -1
                    mInvalidatedPointerId = -1
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            if(disallowIntercept)
                select(null, STATUS_IDLE)
        }
    }

    init {
        mDensity = 50 / Resources.getSystem().displayMetrics.density
    }

    private fun getVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = VelocityTracker.obtain()
    }

    private fun findViewHolderAnimation(viewHolder: RecyclerView.ViewHolder?): CustomAnimator? {
        var i = mAnimations.size - 1
        var found: CustomAnimator? = null
        while(i >= 0) {
            val anim = mAnimations[i]
            if(anim.viewHolder == viewHolder) {
                found = anim
                i = 0
            }
            i--
        }
        return found
    }

    private fun updatePosition() {
        if((mDirs and (TO_LEFT or TO_RIGHT)) != 0.toByte()){
            mPosition.x = mTargetViewHolderX!! + mDeltaX - mTargetViewHolder!!.itemView.left
        } else {
            mPosition.x = mTargetViewHolder!!.itemView.translationX
        }
    }

    private fun select(viewHolder: RecyclerView.ViewHolder?, status: Byte) {
        if(mTargetViewHolder == viewHolder && mStatus == status)
            return
        endAnimation(viewHolder, true)
        setStatus(status)
        var preventLayout = false
        mTargetViewHolder?.let {
            val prevTarget = it
            if(prevTarget.itemView.parent != null) {

                updatePosition()
                val currentX = mPosition.x
                val destinationX = (mDeltaX.sign * mRecyclerView.width).let { d ->
                    if(abs(currentX) > abs(d/2)) d else 0f
                }

                val animator = object : CustomAnimator(prevTarget, currentX, destinationX) {
                    override fun onAnimationEnd(animation: Animator) {
                        if(overridden)
                            return
                        super.onAnimationEnd(animation)
                        mPendingRestores.add(prevTarget)
                        isPending = true
                        val dir = if(destinationX > 0) TO_RIGHT else TO_LEFT
                        mCallback.afterSliding(this.viewHolder,dir)
                        if(destinationX != 0f) {
                            postOnSwipe(this, dir)
                        } else {
                            mCallback.afterSlidingWithoutSwiped(this.viewHolder,dir)
                        }
                    }
                }
                val duration = (abs(destinationX - currentX) / mRecyclerView.width * 300).toLong()
                animator.setDuration(duration)
                mAnimations.add(animator)
                animator.start()
                preventLayout = true
            } else {
                restoreItem(prevTarget)
            }
            mTargetViewHolder = null
        }
        viewHolder?.let {
            mTargetViewHolderX = it.itemView.left.toFloat()
            mTargetViewHolder = it
        }
        mRecyclerView.parent?.requestDisallowInterceptTouchEvent(mTargetViewHolder != null)
        if(!preventLayout)
            mRecyclerView.layoutManager?.requestSimpleAnimationsInNextLayout()
        mRecyclerView.invalidate()
    }

    private fun postOnSwipe(anim: CustomAnimator, dir: Byte) {
        mRecyclerView.post(object: Runnable {
            override fun run() {
                if(mRecyclerView.isAttachedToWindow
                    && !anim.overridden
                    && anim.viewHolder.absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    val animator = mRecyclerView.itemAnimator
                    if((animator == null || !animator.isRunning) && !hasRunningAnimation()) {
                        mCallback.onSwiped(anim.viewHolder, dir)
                    } else {
                        mRecyclerView.post(this)
                    }
                }
            }
        })
    }

    private fun hasRunningAnimation(): Boolean {
        return mAnimations.find { !it.terminated } != null
    }

    open fun onSelectToSwipe(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    private fun checkSelectedToSwipe(action: Int, e: MotionEvent, index: Int) {
        if(mTargetViewHolder != null || action != MotionEvent.ACTION_MOVE || mStatus == STATUS_SCROLLING)
            return
        if (mRecyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
            return
        }
        // check if item is blocked (SECOND+ times recognized item in same event id)
        if(mActivePointerId == mInvalidatedPointerId)
            return
        val v = mRecyclerView.findChildViewUnder(e.x,e.y) ?: return

        val deltaX = e.getX(index) - mInitialX
        val deltaY = e.getY(index) - mInitialY
        val absX = abs(deltaX)
        val absY = abs(deltaY)

        if(absX > absY) {
            if(absX > mDensity!!) {
                if((deltaX < 0).and(mDirs and TO_LEFT == 0.toByte()))
                    return
                if((deltaX > 0).and(mDirs and TO_RIGHT == 0.toByte()))
                    return
                val vh = mRecyclerView.getChildViewHolder(v)
                // check for proceed (FIRST time recognized item)
                if(!onSelectToSwipe(vh)) {
                    // when not proceed
                    // block item in this event id
                    mInvalidatedPointerId = mActivePointerId
                    return
                }
                mDeltaX = 0f
                mDeltaY = 0f
                mActivePointerId = e.getPointerId(0)
                select(vh, STATUS_SWIPING)
                mScope.launch {
                    mCallback.beforeSliding(
                        mTargetViewHolder!!,
                        if (deltaX > 0) TO_RIGHT else TO_LEFT
                    )
                }
            }
        } else if(absY > mDensity!!) {
            setStatus(STATUS_SCROLLING)
        }
    }

    private fun endAnimation(viewHolder: RecyclerView.ViewHolder?, overridden: Boolean) {
        var i = mAnimations.size - 1
        while(i >= 0) {
            val anim = mAnimations[i]
            if(anim.viewHolder == viewHolder) {
                anim.overridden = anim.overridden or overridden
                if(!anim.terminated)
                    anim.cancel()
                mAnimations.remove(anim)
                i = 0
            }
            i--
        }
    }

    private fun updateStartPoint(x: Float,y: Float) {
        mInitialX = x
        mInitialY = y
    }

    private fun initialize() {
        mRecyclerView.apply {
            addOnItemTouchListener(mOnItemTouchListener)
            addItemDecoration(this@SwipeItemHelper)
            addOnChildAttachStateChangeListener(this@SwipeItemHelper)
        }
    }

    private fun clear() {
        mRecyclerView.apply {
            removeItemDecoration(this@SwipeItemHelper)
            removeOnItemTouchListener(mOnItemTouchListener)
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        try {
            if(mRecyclerView == recyclerView)
                return
            if(recyclerView != null)
                clear()
            mRecyclerView = recyclerView!!
            initialize()
        } catch (e: UninitializedPropertyAccessException) {
            mRecyclerView = recyclerView!!
            initialize()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        mTargetViewHolder?.let {
            updatePosition()
        }
        for (animation in mAnimations) {
            animation.update()
            val count = c.save()
            animation.viewHolder.itemView.translationX = animation.final
            c.restoreToCount(count)
        }
        mTargetViewHolder?.let {
            val count = c.save()
            it.itemView.translationX = mPosition.x
            c.restoreToCount(count)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        mTargetViewHolder?.let {
            updatePosition()
        }
        for (animation in mAnimations) {
            animation.update()
            val count = c.save()
            animation.viewHolder.itemView.translationX = animation.final
            c.restoreToCount(count)
        }
        mTargetViewHolder?.let {
            val count = c.save()
            it.itemView.translationX = mPosition.x
            c.restoreToCount(count)
        }
        var hasRunningAnimation = false
        var i = mAnimations.size - 1
        while (i >= 0) {
            val anim = mAnimations[i]
            if(anim.terminated && !anim.isPending)
                mAnimations.removeAt(i)
            else if(!anim.terminated)
                hasRunningAnimation = true
            i--
        }
        if(hasRunningAnimation) parent.invalidate()
    }

    private fun restoreItem(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.apply {
            translationX = 0f
            translationY = 0f
        }
    }

    private fun setStatus(status: Byte) {
        mStatus = status
    }

    private fun updateDeltas(e: MotionEvent, index: Int) {
        mDeltaX = e.getX(index) - mInitialX
        mDeltaY = e.getY(index) - mInitialY
        if (mDirs and TO_LEFT == 0.toByte()) {
            mDeltaX = max(0f, mDeltaX)
        }
        if (mDirs and TO_RIGHT == 0.toByte()) {
            mDeltaX = min(0f, mDeltaX)
        }
    }

    private fun touchMove(e: MotionEvent, index: Int) {
        when(mStatus) {
            STATUS_SWIPING -> {
                mTargetViewHolder?.let {
                    val v = it.itemView
                    if(e.y !in v.y..(v.y + v.height)) {
                        select(null, STATUS_SCROLLING)
                        mActivePointerId = -1
                        return
                    }
                    updateDeltas(e,index)
                    mScope.launch {
                        mCallback.onSliding(it,if(mDeltaX > 0) TO_RIGHT else TO_LEFT)
                    }
                    mRecyclerView.invalidate()
                }
            }
//            STATUS_SCROLLING -> {
//                scrollVertically(previousY - e.y)
//            }
//            STATUS_IDLE -> {
//                if(/*mTarget != null && */toDp(absX) > 50) {
//                    mTargetView!!.let {
//                        val viewHolder = mRecyclerView.findContainingViewHolder(it)
//                        setStatus(STATUS_SWIPING)
//                        mScope.launch {
//                            mCallback.beforeSliding(
//                                viewHolder!!,
//                                if (deltaX > 0) TO_RIGHT else TO_LEFT
//                            )
//                        }
//                    }
//                }
//                else if(absY > 20)
//                    setStatus(STATUS_SCROLLING)
//            }
        }
    }

    open class CustomAnimator(viewHolder: RecyclerView.ViewHolder, from: Float, to: Float): AnimatorListener {
        private val mViewHolder = viewHolder
        val viewHolder get() = mViewHolder
        private val mFrom = from
        private val mTo = to
        private val mDelta = mTo - mFrom
        private var mFinal: Float = 0f
        val final get() = mFinal

        private var mFraction = 0f
        val fraction get() = mFraction

        private var mIsPending = false
        var isPending
            set(value) {
                mIsPending = value
            }
            get() = mIsPending

        private var mEnded = false
        val terminated get() = mEnded

        private var mOverridden = false
        var overridden
            set(value) {
                mOverridden = value
            }
            get() = mOverridden

        private var mAnimator: ValueAnimator = ValueAnimator.ofFloat(0f,1f)

        init {
            mAnimator.apply {
                addUpdateListener {
                    mFraction = it.animatedFraction
                }
                setTarget(mViewHolder.itemView)
                addListener(this@CustomAnimator)
                mFraction = 0f
            }
        }

        fun setDuration(value: Long) {
            mAnimator.duration = value
        }

        private fun setFraction(value: Float) {
            mFraction = value
        }

        fun start() {
            mViewHolder.setIsRecyclable(false)
            mAnimator.start()
        }

        fun cancel() {
            mAnimator.cancel()
        }

        fun update() {
            mFinal = if(mFrom == mTo)
                mViewHolder.itemView.translationX
            else
                mFrom + mFraction * mDelta
        }

        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            if(!mEnded)
                mViewHolder.setIsRecyclable(true)
            mEnded = true
        }

        override fun onAnimationCancel(animation: Animator) {
            setFraction(1f)
        }

        override fun onAnimationRepeat(animation: Animator) {
        }

    }

    override fun onChildViewAttachedToWindow(view: View) {
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        val viewHolder = mRecyclerView.getChildViewHolder(view)
        viewHolder?.let {
            if(mTargetViewHolder != null && mTargetViewHolder == it) {
                select(null, STATUS_IDLE)
            } else {
                endAnimation(it, false)
                if(mPendingRestores.remove(it))
                    restoreItem(it)
            }
        }
    }

    companion object {
        const val TO_RIGHT: Byte = 0x01
        const val TO_LEFT: Byte = 0x02
        const val STATUS_IDLE: Byte = 0x00
        const val STATUS_SWIPING: Byte = 0x01
        const val STATUS_SCROLLING: Byte = 0x02
    }

}