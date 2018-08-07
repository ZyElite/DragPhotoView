package cn.zy

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import com.github.chrisbanes.photoview.PhotoView


/**
 * @author ZyElite
 * @date 2018-06-25
 * @depot https://github.com/ZyElite/DragPhotoView
 *
 */
class DragPhotoView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PhotoView(context, attrs, defStyleAttr) {
    private var tag = "DragPhotoView"

    //界面位置区域
    private var mRect: RectF? = null
    //预览区域
    private var mReviewRect: RectF = RectF()

    private var mWidth = 0
    private var mHeight = 0

    private var mPaint: Paint? = null
    private var mDownX = 0F
    private var mDownY = 0F
    //不透明
    private var mAlpha = 255

    private var mScale = 1F
    //最小缩放
    private var MIN_SCALE = 0.3f

    private var mTranslateY: Float = 0F
    private var mTranslateX: Float = 0F
    private var DURATION: Long = 300

    //用作移动的距离 算出缩放 透明度 的比值
    private var MAX_TRANSLATEY = 500

    private var mOnExitClickListener: OnExitClickListener? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        mPaint = Paint()
        mPaint!!.color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas) {
        mPaint?.alpha = mAlpha
        canvas.drawRect(0F, 0F, mWidth.toFloat(), mHeight.toFloat(), mPaint)
        canvas.translate(mTranslateX, mTranslateY)
        canvas.scale(mScale, mScale, (mWidth / 2).toFloat(), (mHeight / 2).toFloat())
        super.onDraw(canvas)
    }

    fun setRect(rect: RectF) {
        this.mRect = rect
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }


    fun setOnExitListener(consumer: OnExitClickListener) {
        this.mOnExitClickListener = consumer
    }

    interface OnExitClickListener {
        fun onExit()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.rawX
                mDownY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (mTranslateY > 0) {
                    if (mOnExitClickListener != null) {
//                        val maxTranslateY = height / 2 * 1F - (height * mScale / 2)
//                        val maxTranslateX = width / 2 * 1F - (width * mScale / 2)
//                        if (mTranslateY > maxTranslateY) {
//                            mTranslateY = maxTranslateY
//                            invalidate()
//                        }
//                        if (Math.abs(mTranslateX) > maxTranslateX) {
//                            mTranslateX = if (mTranslateX < 0)
//                                -maxTranslateX
//                            else maxTranslateX
//                            invalidate()
//                        }
//                        mAlpha = 0
//                        invalidate()
                        finishAnimator()
                        //mOnExitClickListener!!.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight)
                        return true
                    }
                } else {
                    mTranslateX = 0F
                    mTranslateY = 0F
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val rawX = event.rawX
                val rawY = event.rawY
                mTranslateY = rawY - mDownY
                mTranslateX = rawX - mDownX
                if (Math.abs(mTranslateY) > 10) {
                    val percent: Float = mTranslateY / MAX_TRANSLATEY
                    mScale = 1 - mTranslateY / (MAX_TRANSLATEY * 2.5f)
                    mAlpha = (100 * (1 - if (percent > 1) 1f else percent) + 155).toInt()
//                    mAlpha = (255 * (1 - percent)).toInt()
//                    mScale = 1 - percent
                    if (mScale <= MIN_SCALE) {
                        mScale = MIN_SCALE
                    } else if (mScale > 1) {
                        mScale = 1F
                    }
                    if (mAlpha > 255) {
                        mAlpha = 255
                    } else if (mAlpha < 0) {
                        mAlpha = 0
                    }
                    invalidate()
                }

            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 如果位置不一致 图片在屏幕中心消失
     */
    fun finishAnimator() {
        mReviewRect = displayRect
        //新的缩放比
        val newScale = if (mReviewRect.height() >= mReviewRect.width()) {
            mRect!!.height() / mReviewRect.height()
        } else {
            mRect!!.width() / mReviewRect.width()
        }
        val trueLeft = mTranslateX + (mReviewRect.width() / 2) * (1 - newScale)
        val offsetX = trueLeft - mRect!!.left
        val trueTop = mTranslateY + (mReviewRect.height() / 2) * (1 - newScale) + mReviewRect.top
        val offsetY = trueTop - mRect!!.top


        val animatorSet = AnimatorSet()
        val valueAnimatorX = ValueAnimator.ofFloat(mTranslateX, mTranslateX - offsetX)
        val valueAnimatorY = ValueAnimator.ofFloat(mTranslateY, mTranslateY - offsetY)
        val valueAnimatorScale = ValueAnimator.ofFloat(mScale, newScale - 0.02f)
        val valueAnimatorAlpha = ValueAnimator.ofInt(mAlpha, 100)
        valueAnimatorX.addUpdateListener { animation ->
            mTranslateX = animation.animatedValue as Float
            invalidate()
        }
        valueAnimatorY.addUpdateListener { animation ->
            mTranslateY = animation.animatedValue as Float
            invalidate()
        }

        valueAnimatorScale.addUpdateListener { animation ->
            mScale = animation.animatedValue as Float
            invalidate()
        }
        valueAnimatorAlpha.addUpdateListener { animation ->
            mAlpha = animation.animatedValue as Int
            invalidate()
        }
        animatorSet.playTogether(valueAnimatorX, valueAnimatorY, valueAnimatorScale, valueAnimatorAlpha)
        animatorSet.duration = DURATION - 50
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                if (mOnExitClickListener != null) {
                    mOnExitClickListener!!.onExit()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animatorSet.start()
    }
}