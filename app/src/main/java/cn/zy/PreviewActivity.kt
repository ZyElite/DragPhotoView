package cn.zy

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.zy.function.Consumer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val position = intent.getIntExtra("position", 0)
        val location = intent.getIntArrayExtra("location")
        val heigth = intent.getIntExtra("height", 0)
        val width = intent.getIntExtra("width", 0)

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
        viewPager.currentItem = position
    }

    class ImageAdapter(private var datas: List<Drawable>) : PagerAdapter() {
        private var location: IntArray? = null
        private var currentPosition: Int = 0

        fun setLocation(location: IntArray, position: Int) {
            this.location = location
            this.currentPosition = position
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dragPhotoView = LayoutInflater.from(container.context).inflate(R.layout.item_preview_layout, null) as DragPhotoView
            Glide.with(container.context).load(datas[position]).into(dragPhotoView)
            dragPhotoView.setOnExitListener(object : Consumer<Float> {
                override fun accept(t: Float) {
                    if (currentPosition == position) {
                        val animator = ValueAnimator.ofInt(t.toInt(), location!![0])
                        animator.duration = 1000
                        animator.addUpdateListener {
                            val value: Int = it.animatedValue as Int
                            dragPhotoView.layout(value, value, value + dragPhotoView.width, value + dragPhotoView.height)
                        }
                        animator.start()
                    }
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
