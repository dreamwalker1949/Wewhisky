package syb.util

import java.io.File

/**
 * write bean to file
 * date 2019-10-12
 * @author SunYiBo
 */
fun File.write(t: Any, clear: Boolean = false) =
    if (t is List<*>) {
      if (clear) {
        writeText("")
      }
      appendText(t.joinToString("") { OBJECT_MAPPER.writeValueAsString(it) + "\n" })
    } else {
      appendText(OBJECT_MAPPER.writeValueAsString(t) + "\n")
    }