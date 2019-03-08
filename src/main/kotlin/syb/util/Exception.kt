package syb.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 异常通用方法
 * @user SunYiBo
 * @date 31/01/2018
 */
private val log: Logger = LoggerFactory.getLogger("exception")

/**
 * 打印项目报错行信息
 * @param msg 信息
 */
fun Exception.printLine(msg: String? = null): String {
  val list = stackTrace
      .filter { "aegis" in it.className }
      .dropLastWhile { "aegis" !in it.className }
      .notEmpty()
    ?: stackTrace.take(5)
  printStackTrace()
  return "$msg, ${toString()}\n" + list.joinToString("\n") { it.toString() }
      .also { log.error(it) }
}
