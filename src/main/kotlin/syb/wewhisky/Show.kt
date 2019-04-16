package syb.wewhisky

import syb.util.OBJECT_MAPPER
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

//文件夹
const val ROOT = "E://download//we"

val POST_FILE = File("$ROOT//post.txt")

val USER_FILE = File("$ROOT//user.txt")

val REPLY_FILE = File("$ROOT//reply.txt")

val SF = SimpleDateFormat("yyyy-MM-dd HH:mm")

/**
 * @return 帖子
 * @date 2019-04-12
 * @author SunYiBo
 */
fun getPosts() =
    POST_FILE.readLines()
        .map { OBJECT_MAPPER.readValue(it, Post::class.java) }

/**
 * @return 回復
 * @date 2019-04-12
 * @author SunYiBo
 */
fun getReply() =
    REPLY_FILE.readLines()
        .map { OBJECT_MAPPER.readValue(it, Reply::class.java) }

/**
 * @return 用户
 * @date 2019-04-12
 * @author SunYiBo
 */
fun getUsers() =
    USER_FILE.readLines()
        .map { OBJECT_MAPPER.readValue(it, User::class.java) }
        .distinctBy { it.id }

/**
 * Created by SunYiBo on 2019-04-12.
 * @author SunYiBo
 */
fun main() {
  showUserStatistics()
  showUser(setOf("鬼束石燕", "世间一双眼", "jjkmlss", "LVSOR", "Oneyunqi", "Ange", "moses5819007"))
}

/**
 * 展示用户的帖子
 * @param names 用户名
 * @date 2019-04-11
 * @author SunYiBo
 */
private fun showUser(names: Set<String>) {
  val posts = getPosts()
  getUsers()
      .filter { it.name in names }
      .forEach { user ->
        File("$ROOT//${user.name}").mkdir()
        posts.filter { it.user == user.id }
            .forEach { post ->
              val title = post.title.replace("[<>$/?\"]".toRegex(), " ")
              File("$ROOT//${user.name}//$title.txt")
                  .writeText(
                      "标题：${post.title}\r\n" +
                          "链接：http://wewhisky.com/forum.php?mod=viewthread&tid=${post.id}\r\n" +
                          "发布时间：${SF.format(post.create)}\r\n" +
                          "正文：${post.text.replace("\n", "\r\n")}"
                  )
            }
      }
  System.exit(0)
}

/**
 * @return 展示用戶統計信息
 * @date 2019-04-12
 * @author SunYiBo
 */
private fun showUserStatistics() {
  val file = File("$ROOT//UserStatistics.txt")
  val posts = getPosts()
  val users = getUsers()
  val replys = getReply()
  val postMap = posts.associateBy { it.id }
  val userMap = users.associateBy { it.id }
  val userPost = posts.groupBy { it.user }
  file.writeText("帖子回复数top10\r\n\r\n")
  replys.groupBy { postMap[it.id] }
      .toList()
      .sortedByDescending { it.second.size }
      .take(10)
      .forEach { (post, _) ->
        file.appendText(
            "标题：${post?.title}\r\n" +
                "作者：${userMap[post?.user]?.name}\r\n" +
                "链接：http://wewhisky.com/forum.php?mod=viewthread&tid=${post?.id}\r\n\r\n"
        )
      }
  listOf(listOf("id", "用户", "月发帖", "每帖他人回复", "回帖", "发贴字数平方和", "回帖字数平方和"))
      .plus(
          users.asSequence()
              .filter { it.post > 20 }
              .mapNotNull { user ->
                val post = userPost[user.id]
                    ?.filter {
                      "谨慎出价" !in it.text
                          && "謹慎出價" !in it.text
                          && "未成年人" !in it.text
                    }
                  ?: emptyList()
                if (post.size > 20) {
                  val pIds = post.map { it.id }
                  val reply = replys.filter { it.id in pIds && it.user != user.id }
                  val write = post.sumByDouble { Math.pow(min(it.text.length, 1000).toDouble(), 2.0) }
                  val answer = replys.filter { it.user == user.id }
                      .sumByDouble { Math.pow(min(it.text.length, 1000).toDouble(), 2.0) }
                  val registerDay = user.register?.time?.let { (Date().time - it) / (24 * 3600 * 1000) } ?: 9999
                  write to listOf(
                      user.id,
                      user.name,
                      (post.size * 305 / registerDay / 10.0).takeIf { it >= 4.5 } ?: "-",
                      (reply.size / user.post).takeIf { it > 28 } ?: "-",
                      user.reply.takeIf { it > 4000 } ?: "-",
                      write,
                      answer.takeIf { it > 6000000 } ?: "-"
                  )
                } else {
                  null
                }
              }
              .sortedByDescending { it.first }
              .map { it.second }
              .take(50)
      )
      .joinToString("\r\n") { l ->
        l.joinToString(",")
      }.let { file.appendText(it) }
  System.exit(0)
}
