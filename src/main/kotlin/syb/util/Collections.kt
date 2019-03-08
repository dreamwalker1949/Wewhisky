package syb.util

/**
 * @return 是否为null或空
 */
fun <T> Iterable<T>?.isNullOrEmpty() = this?.iterator()?.hasNext() != true

/**
 * @return 是否为null或空
 */
fun <T> List<T>?.notEmpty() = this?.takeIf { !it.isNullOrEmpty() }
