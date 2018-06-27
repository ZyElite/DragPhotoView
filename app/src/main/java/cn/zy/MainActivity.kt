package cn.zy

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.zy.function.BiConsumer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_recycle_layout.view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = Adapter()
        recycleView.adapter = adapter
        val datas = ArrayList<Drawable>()
        datas.add(resources.getDrawable(R.mipmap.ic_1))
        datas.add(resources.getDrawable(R.mipmap.ic_2))
        datas.add(resources.getDrawable(R.mipmap.ic_3))
        datas.add(resources.getDrawable(R.mipmap.ic_4))
        datas.add(resources.getDrawable(R.mipmap.ic_5))
        adapter.replace(datas)
        adapter.setClick(object : BiConsumer<DragPhotoView, Int> {
            override fun accept(t: AppCompatImageView, u: Int) {
                val location = IntArray(2)
                t.getLocationInWindow(location)
                startActivity(Intent(this@MainActivity, PreviewActivity::class.java)
                        .putExtra("position", u)
                        .putExtra("location", location)
                        .putExtra("height", t.height)
                        .putExtra("width", t.width)
                        .putExtra("left", t.left)
                        .putExtra("top", t.top)
                )
            }
        })
    }

    class Adapter : RecyclerView.Adapter<Adapter.Holder>() {
        private lateinit var mContex: Context
        private var datas = ArrayList<Drawable>()
        private var mOnItemClickListener: BiConsumer<DragPhotoView, Int>? = null

        fun setClick(mOnItemClickListener: BiConsumer<DragPhotoView, Int>) {
            this.mOnItemClickListener = mOnItemClickListener
        }


        fun replace(datas: ArrayList<Drawable>) {
            this.datas.addAll(datas)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            mContex = parent.context
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_recycle_layout, null))
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.itemView.setOnClickListener {
                mOnItemClickListener?.accept(holder.itemView.iv, position)
            }
            Glide.with(mContex).load(datas[position]).into(holder.itemView.iv)
        }


        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)


    }

}
