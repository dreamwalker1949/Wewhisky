package syb.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier

/**
 * json序列化设置
 * Created by SunYiBo on 18/01/2019.
 * @user SunYiBo
 * @since 1.0
 */
class SerializerModifier : BeanSerializerModifier() {

  private val arraySerializer = object : JsonSerializer<Any>() {

    override fun serialize(value: Any?, jgen: JsonGenerator, p2: SerializerProvider) {
      if (value == null) {
        jgen.writeStartArray()
        jgen.writeEndArray()
      } else {
        jgen.writeObject(value)
      }
    }

  }

  private val stringSerializer = object : JsonSerializer<Any>() {

    override fun serialize(value: Any?, jgen: JsonGenerator, p2: SerializerProvider) {
      if (value == null) {
        jgen.writeString("")
      } else {
        jgen.writeObject(value)
      }
    }

  }

  override fun changeProperties(
      config: SerializationConfig,
      desc: BeanDescription,
      properties: List<BeanPropertyWriter>
  ): List<BeanPropertyWriter> {
    properties.forEach {
      if (it.type.rawClass == String::class.java) {
        it.assignNullSerializer(stringSerializer)
      } else if (it.type.isInterface) {
        it.assignNullSerializer(arraySerializer)
      }
    }
    return properties
  }

}
