package cn.zy

import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val position = intent.getIntExtra("position",0)
        val datas = ArrayList<Drawable>()
        datas.add(resources.getDrawable(R.mipmap.ic_1))
        datas.add(resources.getDrawable(R.mipmap.ic_2))
        datas.add(resources.getDrawable(R.mipmap.ic_3))
        datas.add(resources.getDrawable(R.mipmap.ic_4))
        datas.add(resources.getDrawable(R.mipmap.ic_5))
        viewPager.adapter = ImageAdapter(datas)
        viewPager.currentItem = position
    }

    class ImageAdapter(private var datas: List<Drawable>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dragPhotoView  = LayoutInflater.from(container.context).inflate(R.layout.item_preview_layout,null) as DragPhotoView
            Glide.with(container.context).load(datas[position]).into(dragPhotoView)
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
