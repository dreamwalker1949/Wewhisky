package syb.wewhisky

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import syb.util.*
import java.io.File
import java.lang.Thread.sleep
import java.text.ParseException

/**
 * WeWhisky爬虫
 * Created by SunYiBo on 19/02/2019.
 * @user SunYiBo
 * @since 1.0-SNAPSHOT
 */
class WeSpider {

  //用户名与id对照
  private val userMap: Map<String, Int> by lazy {
    getUsers().associate { it.name to it.id }
  }

  private companion object {
    const val auth = "本帖要求阅读权限高于"
    const val basicUrl = "http://wewhisky.com"
    val failFile = File("$ROOT//fail.txt")
    //登陆后获取Cookie
    val header = mapOf(
        "Cookie" to "bAR9_2132_saltkey=vQBb10OZ; " +
            "bAR9_2132_auth=c8c5aWZlIY5KDBzl0Ak7ZrzSFsYqzp1JqAiefBRuVa%2FKNAt2XX19zUrMi%2Bqm5Js69Yj1pvwWXx7wNTDRMysjyrsX; " +
            "bAR9_2132_security_cookiereport=1314ucX8%2F8wpb9CyViOoKXlbuJz9jz9Q3zwZm62heLu9amC9qyQI; "
    )
    const val lastUpdate = " 本帖最后由"
    //最大用户id
    const val maxUserId = 9019
    val postFile = File("$ROOT//post.txt")
    const val publish = " 发表于 "
    val replyFile = File("$ROOT//reply.txt")
    //跟新最近的多少篇文章
    const val update = 20
    val userFile = File("$ROOT//user.txt")

    @JvmStatic
    fun main(args: Array<String>) {
      listOf(postFile, replyFile, userFile, failFile).forEach { file ->
        if (!file.exists()) {
          file.createNewFile()
        }
      }
      WeSpider().apply {
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
    val linkReg = "\\?tid-(\\d+)".toRegex()
    val linkSet = HashSet<Int>(getPost().map { it.id })
    mapOf(
        1 to 134,
        2 to 103,
        3 to 19,
        4 to 19,
        5 to 17,
        2 to 287,
        41 to 45,
        54 to 33,
        43 to 11,
        44 to 1
    ).forEach type@{ type, max ->
      (1..max).forEach { index ->
        val url = if (type < 10 && max < 280) {
          "40.html&filter=typeid&typeid=$type"
        } else {
          "$type.html"
        }.let { "$basicUrl/archiver/?fid-$it&page=$index" }//&orderby=dateline
        val ids = doGet(url, header)
            ?.string()
            ?.let { linkReg.matchGroups(it) }
            ?.map { it.toInt() }
            ?.filter { linkSet.add(it) || index <= update / 20 }
          ?: emptyList()
        if (ids.isEmpty()) {
          return@type
        }
        ids.forEach { id ->
          spiderPost(id, type.takeIf { it < 280 } ?: -1)
        }
      }
    }
    repeat(2) { spiderFail() }
    spiderAfter()
  }

  /**
   * 采集用户信息
   * @date 19/02/2019
   * @author SunYiBo
   */
  fun spiderUser() {
    val noUser = "抱歉，您指定的用户空间不存在"
    val signReg = "个人签名.*?td>([^<>]*)<".toRegex()
    val header = mapOf(
        "User-Agent" to
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
    )
    val levelMap = mapOf(
        "管理员" to 1, "超级版主" to 2, "版主" to 3, "禁止发言" to 4, "禁止访问" to 5,
        "禁止 IP" to 6, "游客" to 7, "等待验证会员" to 8, "限制会员" to 9, "小小麦芽" to 10, "清清流水" to 11,
        "泥煤少年" to 12, "糖化小哥" to 13, "发酵大叔" to 14, "蒸馏高手" to 15, "实习版主" to 16, "网站编辑" to 17,
        "信息监察员" to 18, "审核员" to 19, "QQ游客" to 20, "陈年大师" to 21, "调和师匠" to 22, "谷物隐者" to 23,
        "单麦巨匠" to 24, "麦芽狂人" to 25, "商人" to 26, "垃圾盗图贼" to 27, "荣誉会员" to 28
    )
    val users = getUsers()
    userFile.write(users.filter { it.level != 8 }, true)
    val start = users.map { it.id }.max()?.minus(50) ?: 1
    val todo = getUsers()
        .filter { it.level == 8 }
        .map { it.id }
    (todo + (start..maxUserId))
        .forEach { id ->
          id.print()
          val html = doGet("$basicUrl/?$id", header)
              ?.string()
              ?.takeIf { noUser !in it }
          html?.let { Jsoup.parse(it) }
              ?.let { doc ->
                val time = doc.getElementById("pbbs").text()
                val score = doc.getElementById("psts").text()
                val info = doc.getElementsByClass("pf_l cl").text()
                val count = doc.getElementsByClass("cl bbda pbm mbm").text()
                val user = User(
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
                userFile.write(user)
              }
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

  private fun sleep(second: Int = 1) = sleep((second * 200 + Math.random() * second * 333).toLong())

  /**
   * 采集后处理
   * @date 2019-04-16
   * @author SunYiBo
   */
  private fun spiderAfter() {
    val reply = replyFile.readLines()
        .map { OBJECT_MAPPER.readValue(it, Reply::class.java) }
        .distinctBy { it.rid }
    replyFile.write(reply, true)
    val ids = getUsers()
        .filter { it.name in setOf("jjkmlss", "Oneyunqi", "Ange", "moses5819007") }
        .map { it.id }
    val newPost = getPosts().filter { it.user in ids }
    val oldPost = File("$ROOT//备份//post.txt")
        .readLines()
        .map { OBJECT_MAPPER.readValue(it, Post::class.java) }
        .filter { it.user in ids }
        .associate { it.id to it.text }
    newPost.forEach { post ->
      if (post.text.length < 7) {
        post.text = oldPost[post.id]
            ?.takeIf { it.length > 7 }
          ?: post.text
      }
    }
    getPosts()
        .filter { it.user !in ids }
        .plus(newPost)
        .let { POST_FILE.write(it, true) }
  }

  /**
   * 采集失败过的帖子
   * @date 2019-04-11
   * @author SunYiBo
   */
  private fun spiderFail() {
    val done = getPost()
        .map { it.id }
        .toSet()
    val ids = failFile.readLines()
        .filter { it.isNotEmpty() }
        .map { it.toInt() }
        .distinct()
        .filter { it !in done }
    failFile.writeText("")
    ids.forEach { spiderPost(it) }
    postFile.write(getPost(), true)
  }

  /**
   * 采集贴子
   * @param id 贴子id
   * @param type 板块id
   * @param fail 是否失败过
   * @date 25/02/2019
   * @author SunYiBo
   */
  private fun spiderPost(id: Int, type: Int = 1, fail: Boolean = false) {
    id.print()
    var page = 1
    val replyList = ArrayList<Reply>()
    try {
      do {
        val html = doGet("$basicUrl/archiver/?tid-$id.html&page=$page", header)
            ?.string()
            ?.takeIf { auth !in it }
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
      if (fail) {
        failFile.write(id)
      } else {
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
