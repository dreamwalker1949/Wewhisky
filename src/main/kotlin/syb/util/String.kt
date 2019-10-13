package syb.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

/**  json解析 */
val OBJECT_MAPPER = ObjectMapper()
    .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
//    .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)!!
    .apply {
      serializerFactory = serializerFactory.withSerializerModifier(SerializerModifier())
    }

/**
 * @return 指定字符后的字符串
 */
fun String.after(s: String, last: Boolean = false) =
    if (s in this) {
      drop(
          (if (last) {
            lastIndexOf(s)
          } else {
            indexOf(s)
          }) + s.length
      )
    } else {
      this
    }

/**
 * @return 指定字符前的字符串
 */
fun String.before(s: String) = if (s in this) {
  take(indexOf(s))
} else {
  this
}

/**
 * 测试时输出
 * @date 23/05/2018
 * @user SunYiBo
 */
fun Any.print() = print("${toString().takeIf { "@" !in it } ?: ObjectMapper().writeValueAsString(this)}\n")
