package cn.zy.function


/**
 * zy
 */
@FunctionalInterface
interface Consumer< T > {
     fun accept(t: T)
}