package cn.zy.function


/**
 * zy
 */
@FunctionalInterface
interface Consumer< in T> {
     fun accept(t: T)
}