package cn.zy

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.chrisbanes.photoview.PhotoView


/**
 * @author ZyElite
 * @date 2018-06-25
 * @depot https://github.com/ZyElite/DragPhotoView
 *
 */
class DragPhotoView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PhotoView(context, attrs, defStyleAttr) {
    private var tag = "DragPhotoView"

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


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

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


    //用作移动的距离 算出缩放 透明度 的比值
    private var MAX_TRANSLATEY = 500

    private var mOnExitClickListener: OnExitClickListener? = null

    fun setOnExitListener(consumer: OnExitClickListener) {
        this.mOnExitClickListener = consumer
    }

    interface OnExitClickListener {
        fun onExit(dragPhotoView: DragPhotoView, translateX: Float, translateY: Float, width: Int, height: Int)
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
                        val maxTranslateY = height / 2 * 1F - (height * mScale / 2)
                        val maxTranslateX = width / 2 * 1F - (width * mScale / 2)
                        if (mTranslateY > maxTranslateY) {
                            mTranslateY = maxTranslateY
                            invalidate()
                        }
                        if (Math.abs(mTranslateX) > maxTranslateX) {
                            mTranslateX = if (mTranslateX < 0)
                                -maxTranslateX
                            else maxTranslateX
                            invalidate()
                        }
                        mAlpha = 0
                        invalidate()
                        mOnExitClickListener!!.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight)
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
                    val percent = mTranslateY / MAX_TRANSLATEY
                    if (mScale in MIN_SCALE..1.0F) {
                        mAlpha = (255 * (1 - percent)).toInt()
                        mScale = 1 - percent
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
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 如果位置不一致 图片在屏幕中心消失
     */
    fun finishAnimator(activity: AppCompatActivity) {
        val animatorX = ValueAnimator.ofFloat(mTranslateX, 0F)
        val animatorY = ValueAnimator.ofFloat(mTranslateY, 0F)

        animatorX.addUpdateListener {
            mTranslateX = it.animatedValue as Float
            invalidate()
        }
        animatorY.addUpdateListener {
            mTranslateY = it.animatedValue as Float
            invalidate()
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorX, animatorY)
        animatorSet.duration = 500
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                activity.finish()
                activity.overridePendingTransition(0, 0 )
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        animatorSet.start()
    }
}