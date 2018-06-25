package cn.zy

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val position = intent.getIntExtra("position", 0)
        val location = intent.getIntArrayExtra("location")
        mCurrentHeight = intent.getIntExtra("height", 0)
        mCurrentWidth = intent.getIntExtra("width", 0)

        Log.e("DragPhotoView", "x:" + location[0] + "  y:" + location[1])
        val datas = ArrayList<Drawable>()
        datas.add(resources.getDrawable(R.mipmap.ic_1))
        datas.add(resources.getDrawable(R.mipmap.ic_2))
        datas.add(resources.getDrawable(R.mipmap.ic_3))
        datas.add(resources.getDrawable(R.mipmap.ic_4))
        datas.add(resources.getDrawable(R.mipmap.ic_5))
        val adapter = ImageAdapter(datas)
        viewPager.adapter = adapter
        adapter.setLocation(location, position)
        adapter.setTarget(mCurrentHeight, mCurrentWidth)
        adapter.setExitListener(object : Consumer<View> {
            override fun accept(t: View) {
                finish()
            }

        })
        viewPager.currentItem = position
    }

    class ImageAdapter(private var datas: List<Drawable>) : PagerAdapter() {
        private var location: IntArray? = null
        private var currentPosition: Int = 0
        private var mCurrentHeight = 0
        private var mCurrentWidth = 0


        private var consumer: Consumer<View>? = null

        fun setTarget(targetHeight: Int, targerWidth: Int) {
            this.mCurrentHeight = targetHeight
            this.mCurrentWidth = targerWidth
        }


        fun setExitListener(consumer: Consumer<View>) {
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
                override fun onExitLostener(dragPhotoView: DragPhotoView, translateX: Float, translateY: Float, width: Int, height: Int) {
                    dragPhotoView.scaleY = mCurrentHeight / height * 1F
                    dragPhotoView.scaleX = mCurrentWidth / width * 1F

                    dragPhotoView.translationX = translateX
                    dragPhotoView.translationY = translateY
//                    dragPhotoView.scaleX = 0.3F
//                    dragPhotoView.scaleY = 0.3F


                    val animator = ValueAnimator.ofFloat(translateX, 0F)
                    animator!!.duration = 1000
                    animator.addUpdateListener {
                        dragPhotoView.x = it.animatedValue as Float
                    }
                    animator.start()
                    Log.e("asd", "x = ${dragPhotoView.x} y = ${dragPhotoView.y}")
                }


            })

//            dragPhotoView.setOnExitListener(object : Consumer<Int> {
//                override fun accept(t: Int) {
//                    if (currentPosition == position) {
//                        val animator = ValueAnimator.ofInt(t, location!![0])
//                        animator.duration = 1000
//                        animator.addUpdateListener {
//                            val value: Int = it.animatedValue as Int
//                            dragPhotoView.layout(value, value, value + dragPhotoView.width, value + dragPhotoView.height)
//                        }
//
//                        animator.addListener(object : Animator.AnimatorListener {
//                            override fun onAnimationRepeat(animation: Animator?) {
//                            }
//
//                            override fun onAnimationEnd(animation: Animator?) {
//                                if (consumer!=null) {
//                                    consumer!!.accept(dragPhotoView)
//                                }
//                            }
//
//                            override fun onAnimationCancel(animation: Animator?) {
//                            }
//
//                            override fun onAnimationStart(animation: Animator?) {
//                            }
//
//                        })
//                        animator.start()
//
//                    }
//                }
//            })
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
