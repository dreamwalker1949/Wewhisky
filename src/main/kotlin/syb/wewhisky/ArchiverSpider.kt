package syb.wewhisky

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import syb.util.*
import syb.wewhisky.bean.Post
import syb.wewhisky.bean.Reply
import syb.wewhisky.bean.User
import java.io.File
import java.lang.Thread.sleep
import java.text.ParseException

/**
 * WeWhisky archiver爬虫
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
class ArchiverSpider {

  //用户名与id对照
  private val userMap: Map<String, Int> by lazy {
    getUsers().associate { it.name to it.id }
  }

  private companion object {
    const val basicUrl = "http://wewhisky.com"
    //登陆后获取Cookie
    val header = mapOf(
        "Cookie" to "bAR9_2132_saltkey=vQBb10OZ; " +
            "bAR9_2132_auth=c8c5aWZlIY5KDBzl0Ak7ZrzSFsYqzp1JqAiefBRuVa%2FKNAt2XX19zUrMi%2Bqm5Js69Yj1pvwWXx7wNTDRMysjyrsX; " +
            "bAR9_2132_security_cookiereport=1314ucX8%2F8wpb9CyViOoKXlbuJz9jz9Q3zwZm62heLu9amC9qyQI; "
    )
    val postFile = File("$ROOT//post.txt")
    const val publish = " 发表于 "
    val replyFile = File("$ROOT//reply.txt")
    //更新最近的多少篇文章与用户
    const val update = 20
    val userFile = File("$ROOT//user.txt")

    @JvmStatic
    fun main(args: Array<String>) {
      listOf(postFile, replyFile, userFile).forEach { it.createNewFile() }
      ArchiverSpider().apply {
        spiderUser()
        spiderPage()
      }
    }
  }

  /**
   * 翻页采集贴子
   * @date 19/02/2019
   * @author SunYiBo
   */
  fun spiderPage() {
    val pageNum = 20
    val salePage = 318
    val linkReg = "\\?tid-(\\d+)".toRegex()
    val linkSet = HashSet(getPost().map { it.id })
    repeat(2.takeIf { linkSet.isEmpty() } ?: 1) {
      mapOf(
          1 to 138,
          2 to 114,
          3 to 21,
          4 to 20,
          5 to 18,
          2 to salePage,
          41 to 53,
          54 to 37,
          43 to 11,
          56 to 2,
          44 to 1
      ).forEach type@{ type, max ->
        (1..max).forEach { index ->
          val url = if (type < 10 && max != salePage) {
            "40.html&filter=typeid&typeid=$type"
          } else {
            "$type.html"
          }.let { "$basicUrl/archiver/?fid-$it&page=$index" }
          doGet(url, header)
              ?.string()
              ?.let { linkReg.matchGroups(it) }
              ?.map { it.toInt() }
              ?.filter { linkSet.add(it) || index <= update / pageNum }
              ?.forEach { id ->
                spiderPost(id, type.takeIf { it != salePage } ?: -1)
              }
        }
      }
    }
    (1..(linkSet.max() ?: 1))
        .filter { linkSet.add(it) }
        .forEach { id ->
          spiderPost(id, -2)
        }
  }

  /**
   * 采集用户信息
   * @date 19/02/2019
   * @author SunYiBo
   */
  fun spiderUser() {
    var fail = 0
    val unSign = 8
    val noUser = "抱歉，您指定的用户空间不存在"
    val signReg = "个人签名.*?td>([^<>]*)<".toRegex()
    val header = mapOf(
        "User-Agent" to
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
    )
    val levelMap = mapOf(
        "管理员" to 1, "超级版主" to 2, "版主" to 3, "禁止发言" to 4, "禁止访问" to 5,
        "禁止 IP" to 6, "游客" to 7, "等待验证会员" to unSign, "限制会员" to 9, "小小麦芽" to 10, "清清流水" to 11,
        "泥煤少年" to 12, "糖化小哥" to 13, "发酵大叔" to 14, "蒸馏高手" to 15, "实习版主" to 16, "网站编辑" to 17,
        "信息监察员" to 18, "审核员" to 19, "QQ游客" to 20, "陈年大师" to 21, "调和师匠" to 22, "谷物隐者" to 23,
        "单麦巨匠" to 24, "麦芽狂人" to 25, "商人" to 26, "垃圾盗图贼" to 27, "荣誉会员" to 28
    )
    val users = getUsers().associateBy { it.id }
    userFile.writeText("")
    (1..99999).forEach { id ->
      if (fail > 17) {
        return
      }
      id.print()
      val html = doGet("$basicUrl/?$id", header)
          ?.string()
          ?.takeIf { noUser !in it || fail++ < 0 }
      val user = html?.let { Jsoup.parse(it) }
          ?.let { doc ->
            fail = 0
            val time = doc.getElementById("pbbs").text()
            val score = doc.getElementById("psts").text()
            val info = doc.getElementsByClass("pf_l cl").text()
            val count = doc.getElementsByClass("cl bbda pbm mbm").text()
            User(
                id,
                doc.title().before("的个人资料"),
                signReg.matchGroup(html),
                count.getInt("好友数 ", " |"),
                count.getInt("回帖数 ", " |"),
                count.getInt("主题数 ", " |"),
                count.getInt("分享数 "),
                info.getInfo("真实姓名"),
                when (info.getInfo("性别")) {
                  "男"  -> 1
                  "女"  -> 0
                  else -> -1
                },
                info.getInfo("生日"),
                info.getInfo("居住地"),
                levelMap[doc.getElementsByClass("xi2").last().text()] ?: -1,
                time.getInfo("在线时间")?.toInt() ?: 0,
                time.getInfo("注册时间", " 最后访问")?.toDate(),
                time.getInfo("最后访问", " 上次活动")?.toDate(),
                score.getInt("积分"),
                score.getInt("积点"),
                score.getInt("麦粒"),
                score.getInt("贡献")
            )
          } ?: users[id]
      user?.let { userFile.write(it) }
      sleep()
    }
  }

  private fun String.clean() =
      split("\n")
          .map { it.trim() }
          .filter { it.isNotEmpty() }
          .joinToString("\n")

  private fun String.getInfo(after: String, before: String = " ") =
      takeIf { after in it }
          ?.after(after)
          ?.before(before)

  private fun String.getInt(after: String, before: String = " ") = getInfo(after, before)?.toInt() ?: 0

  private fun getPost() =
      getPosts()
          .sortedByDescending { it.collect }
          .distinctBy { it.id }

  private fun sleep() = sleep((50 + Math.random() * 100).toLong())

  /**
   * 采集贴子
   * @param id 贴子id
   * @param type 板块id
   * @param fail 是否失败过
   * @date 25/02/2019
   * @author SunYiBo
   */
  private fun spiderPost(id: Int, type: Int = 1, fail: Boolean = false) {
    val del = "指定的主题不存在"
    val notFind = "没有找到帖子"
    val lastUpdate = " 本帖最后由"
    val auth = "本帖要求阅读权限高于"
    val userOnly = "本版块只有特定用户可以访问"
    id.print()
    var page = 1
    val replyList = ArrayList<Reply>()
    try {
      do {
        val html = doGet("$basicUrl/archiver/?tid-$id.html&page=$page", header)
            ?.string()
            ?.takeIf { auth !in it && del !in it && notFind !in it && userOnly !in it }
        html?.let { Jsoup.parse(it) }
            ?.let { doc ->
              var reply = Reply()
              doc.getElementById("content")
                  .childNodes()
                  .forEachIndexed { i, e ->
                    if (e is Element && e.className() == "author") {
                      if (reply.user != -1) {
                        if (publish in reply.text) {
                          val text = reply.text.before("\n \n").clean()
                          val author = userMap[text.before(publish)]
                          val time = text.getInfo(publish, "\n")?.toDate()
                          reply.reply = replyList.find { it.user == author && it.create == time }?.rid
                          reply.text = reply.text.after("\n \n")
                        }
                        replyList.add(reply)
                        reply = Reply()
                      }
                      val text = e.text()
                      reply.rid = "$id-$page-$i"
                      reply.user = userMap[text.before(publish)] ?: -2
                      reply.create = text.after(publish).toDate()
                    } else if (e is TextNode && reply.user != -1) {
                      val text = e.text()
                      if (text.startsWith(lastUpdate)) {
                        reply.update = text.getInfo("于 ", " 编辑")?.toDate()
                      } else {
                        reply.text += text + "\n"
                      }
                    }
                  }
              replyList.add(reply)
              replyList.forEach {
                it.text = it.text.clean()
                it.id = id
              }
              if (page == 1) {
                val title = doc.getElementById("nav")
                    .text()
                    .after(" › ", true)
                postFile.write(Post(replyList.first(), title, type))
                replyList.removeAt(0)
              }
            }
        sleep()
        if ("&page=${++page}" !in (html ?: "")) {
          break
        }
      } while (true)
      replyFile.write(replyList)
    } catch (e: Exception) {
      if (!fail) {
        spiderPost(id, type, true)
      }
    }
  }

  private fun String.toDate() =
      try {
        SF.parse(this)
      } catch (e: ParseException) {
        null
      }

  private fun File.write(t: Any, clear: Boolean = false) =
      if (t is List<*>) {
        if (clear) {
          writeText("")
        }
        appendText(t.joinToString("") { OBJECT_MAPPER.writeValueAsString(it) + "\n" })
      } else {
        appendText(OBJECT_MAPPER.writeValueAsString(t) + "\n")
      }

}
