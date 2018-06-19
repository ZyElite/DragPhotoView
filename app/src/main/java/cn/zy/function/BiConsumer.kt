package cn.zy.function


@FunctionalInterface
interface BiConsumer<T, U> {

    fun accept(t: T, u: U)
}
