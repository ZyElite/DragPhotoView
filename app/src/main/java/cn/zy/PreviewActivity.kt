package cn.zy

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.zy.function.Consumer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    private var mCurrentHeight = 0
    private var mCurrentWidth = 0
    private var mAdapter: ImageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val position = intent.getIntExtra("position", 0)
        val location = intent.getIntArrayExtra("location")
        mCurrentHeight = intent.getIntExtra("height", 0)
        mCurrentWidth = intent.getIntExtra("width", 0)
        val left = intent.getIntExtra("left", 0)
        val top = intent.getIntExtra("top", 0)
        Log.e("DragPhotoView", "x:" + location[0] + "  y:" + location[1])
        val datas = ArrayList<Drawable>()
        datas.add(resources.getDrawable(R.mipmap.ic_1))
        datas.add(resources.getDrawable(R.mipmap.ic_2))
        datas.add(resources.getDrawable(R.mipmap.ic_3))
        datas.add(resources.getDrawable(R.mipmap.ic_4))
        datas.add(resources.getDrawable(R.mipmap.ic_5))
        mAdapter = ImageAdapter(datas)
        viewPager.adapter = mAdapter
        mAdapter!!.setLocation(location, position)
        mAdapter!!.setTarget(mCurrentHeight, mCurrentWidth, left, top)
        viewPager.currentItem = position
        mAdapter!!.setActivity(this)
        mAdapter!!.setExit(object : ImageAdapter.OnExit {
            override fun onExit() {
                finish()
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    class ImageAdapter(private var datas: List<Drawable>) : PagerAdapter() {
        private var location: IntArray? = null
        private var currentPosition: Int = 0
        private var mCurrentHeight = 0
        private var mCurrentWidth = 0
        private var left = 0
        private var top = 0
        private var mActivity: AppCompatActivity? = null
        private var consumer: Consumer<Animator>? = null

        private var exit: OnExit? = null

        fun setExit(exit: OnExit) {
            this.exit = exit
        }

        interface OnExit {
            fun onExit()
        }

        fun setTarget(targetHeight: Int, targerWidth: Int, left: Int, top: Int) {
            this.mCurrentHeight = targetHeight
            this.mCurrentWidth = targerWidth
            this.left = left
            this.top = top

        }

        fun setActivity(activity: AppCompatActivity) {
            mActivity = activity
        }

        fun setCancelListener(consumer: Consumer<Animator>) {
            this.consumer = consumer
        }

        fun setLocation(location: IntArray, position: Int) {
            this.location = location
            this.currentPosition = position
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dragPhotoView = LayoutInflater.from(container.context).inflate(R.layout.item_preview_layout, null) as DragPhotoView
            Glide.with(container.context).load(datas[position]).into(dragPhotoView)
            dragPhotoView.setOnExitListener(object : DragPhotoView.OnExitClickListener {
                override fun onExit(view: DragPhotoView, translateX: Float, translateY: Float, width: Int, height: Int) {

//                    val viewX = width / 2 + translateX - location!![0] / 2
//                    val viewY = height / 2 + translateY - location!![1] / 2
//                    view.x = viewX
//                    view.y = viewY
                    val targetX = location!![0] + mCurrentWidth / 2
                    val targetY = location!![1] + mCurrentHeight / 2
                    val mTranslateX = if (targetX > width / 2) {
                        -translateX + (targetX - width / 2)
                    } else {
                        -translateX - Math.abs(targetX - width / 2)
                    }

                    val mTranslateY = if (targetY > height / 2) {
                        -translateY + (targetY - height / 2)
                    } else {
                        -translateY -  Math.abs(targetY - height / 2)
                    }
                    var animatorX = ValueAnimator.ofFloat(0F, mTranslateX)
                    var animatorY = ValueAnimator.ofFloat(0F, mTranslateY)
                    animatorX.duration = 3000
                    animatorY.duration = 3000


                    animatorX.addUpdateListener {
                        view.x = it.animatedValue as Float
                    }
                    animatorY.addUpdateListener {
                        view.y = it.animatedValue as Float
                    }


                    animatorX.start()
                    animatorY.start()


//                    view.finishAnimationCallBack()

//                    val viewX = width / 2 + translateX - location!![0] / 2
//                    val viewY = height / 2 + translateY - location!![1] / 2
//                    view.x = viewX
//                    view.y = viewY
//                    val centerX = view.x + width / 2
//                    val centerY = view.y + height / 2
//                    val mtranslateX = left + width / 2 - centerX
//                    val mtranslateY = top + height / 2 - centerY
//                    val translateXAnimator = ValueAnimator.ofFloat(view.x, view.x + mtranslateX)
//                    translateXAnimator.addUpdateListener { valueAnimator -> view.x = valueAnimator.animatedValue as Float }
//                    translateXAnimator.duration = 3000
//                    translateXAnimator.start()
//                    val translateYAnimator = ValueAnimator.ofFloat(view.y, view.y + mtranslateY)
//                    translateYAnimator.addUpdateListener { valueAnimator -> view.y = valueAnimator.animatedValue as Float }
//                    translateYAnimator.addListener(object : Animator.AnimatorListener {
//                        override fun onAnimationStart(animator: Animator) {
//
//                        }
//
//                        override fun onAnimationEnd(animator: Animator) {
//                            animator.removeAllListeners()
//                            mActivity!!.finish()
//                            mActivity!!.overridePendingTransition(0,0)
////                            finish()
////                            overridePendingTransition(0, 0)
//                        }
//
//                        override fun onAnimationCancel(animator: Animator) {
//
//                        }
//
//                        override fun onAnimationRepeat(animator: Animator) {
//
//                        }
//                    })
//                    translateYAnimator.duration = 3000
//                    translateYAnimator.start()
                }

                override fun onExitListener() {
                    if (exit != null) {
                        exit!!.onExit()
                    }
                }

                override fun onCancelListener(animation: Animator?) {

                }
            })
            container.addView(dragPhotoView)
            return dragPhotoView
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return datas.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }
    }


}
