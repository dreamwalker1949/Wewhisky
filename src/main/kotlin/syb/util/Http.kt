package syb.util

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


/**
 * http工具类
 * Created by SunYiBo on 11/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
private val LOG = LoggerFactory.getLogger("http")

private val POOL = ConnectionPool(5, 20, TimeUnit.MINUTES)

private val TRUST_MANAGER = object : X509TrustManager {

  override fun checkClientTrusted(xcs: Array<X509Certificate>, string: String) {
  }

  override fun checkServerTrusted(xcs: Array<X509Certificate>, string: String) {
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

}

val SSL_CONTEXT: SSLContext = SSLContext.getInstance("TLS").apply {
  init(null, arrayOf(TRUST_MANAGER), null)
}

/**
 * get请求
 * @param url     请求链接
 * @param headers 请求头
 * @return ResponseBody响应
 */
fun doGet(url: String, headers: Map<String, String>? = null): Response? =
    Request.Builder()
        .url(url)
        .let { doHttp(it, headers) }

/**
 * 从响应中得到字符串
 * @date 12/02/2019
 * @user SunYiBo
 */
fun Response?.string() =
    try {
      this?.body()?.string()
    } catch (e: IOException) {
      e.printLine()
      null
    }

/**
 * 发送请求
 * @param builder 请求
 * @param headers 请求头
 * @return Response 相应
 * @date 12/02/2019
 * @user SunYiBo
 */
private fun doHttp(builder: Request.Builder, headers: Map<String, String>?, ssl: Boolean = false): Response? =
    try {
      headers?.forEach { k, v ->
        builder.addHeader(k, v)
      }
      val res = getClient(ssl)
          .newCall(builder.build())
          .execute()
      if (res.isSuccessful || res.code() == 302) {
        res
      } else {
        LOG.info("请求code： ${res.code()}")
        null
      }
    } catch (e: IOException) {
      e.printLine()
      null
    }

/**
 * @param ssl 是否为ssl链接
 * @return OkHttpClient
 * @date 15/02/2019
 * @user SunYiBo
 */
private fun getClient(ssl: Boolean): OkHttpClient =
    OkHttpClient.Builder()
        .apply {
          if (ssl) {
            sslSocketFactory(SSL_CONTEXT.socketFactory, TRUST_MANAGER)
          }
        }
        .connectTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .followRedirects(false)
        .connectionPool(POOL)
        .build()
