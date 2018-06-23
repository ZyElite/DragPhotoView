package cn.zy.function

import android.support.v7.widget.AppCompatImageView


@FunctionalInterface
interface BiConsumer<T, U> {

    fun accept(t: AppCompatImageView, u: U)
}
