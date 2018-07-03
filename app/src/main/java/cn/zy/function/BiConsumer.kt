package cn.zy.function

import android.support.v7.widget.AppCompatImageView



/**
 * @author ZyElite
 * @date 2018-06-25
 * @depot https://github.com/ZyElite/DragPhotoView
 *
 */
@FunctionalInterface
interface BiConsumer<T, U> {
    fun accept(t: T, u: U)
}
