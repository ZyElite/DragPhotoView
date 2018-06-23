package cn.zy

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import cn.zy.function.Consumer
import com.github.chrisbanes.photoview.PhotoView


class DragPhotoView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : PhotoView(context, attrs, defStyleAttr) {
    private var tag = "DragPhotoView"

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        mPaint = Paint()
        mPaint!!.color = Color.TRANSPARENT
    }

    override fun onDraw(canvas: Canvas) {
        mPaint?.alpha = mAlpha
        canvas.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint)
        canvas.translate(mTranslateX, mTranslateY)
        canvas.scale(mScale, mScale, (mWidth / 2).toFloat(), (mHeight / 2).toFloat())
        super.onDraw(canvas)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        Log.e(tag,"width : $w   height:$h")
    }

    private var mWidth = 0
    private var mHeight = 0

    private var mPaint: Paint? = null
    private var mDownX = 0F
    private var mDownY = 0F
    //不透明
    private var mAlpha = 255
    //默认动画执行时间
    private val DURATION: Long = 300
    //最小滑动距离
    private var mMinY = 50F
    private var mScale = 1F
    //最小缩放
    private var MIN_SCALE = 0.5f

    private var mTranslateY: Float = 0F
    private var mTranslateX: Float = 0F
    private var moveX:Float = 0F


    //用作移动的距离 算出缩放 透明度 的比值
    private var MAX_TRANSLATEY = 500

    private var consumer:Consumer<Float>? = null

    fun setOnExitListener(consumer: Consumer<Float>){
        this.consumer = consumer
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.rawX
                mDownY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if(mTranslateY>0){
                    moveX = event.rawX
                   if (consumer!=null) consumer!!.accept(moveX)
                }else{
                    mTranslateX=0F
                    mTranslateY=0F
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
                    mAlpha = (255 * (1 - percent)).toInt()
                    mScale = 1 - percent
                    if (mScale < MIN_SCALE) {
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


    private fun animation() {
        val alphaAnimation = AlphaAnimation(1F, 0F)
        alphaAnimation.duration = 500
        this.startAnimation(alphaAnimation)
    }


    private fun getAlphaAnimation(): ValueAnimator {
        val animator = ValueAnimator.ofInt(mAlpha, 255)
        animator.duration = DURATION
        animator.addUpdateListener { valueAnimator -> mAlpha = valueAnimator.animatedValue as Int }
        return animator
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }


}