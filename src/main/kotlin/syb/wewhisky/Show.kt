package syb.wewhisky

import syb.util.OBJECT_MAPPER
import syb.wewhisky.bean.Post
import syb.wewhisky.bean.Reply
import syb.wewhisky.bean.User
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
        .map { it.replace("data:image/jpeg;base64,/9j/[a-zA-z1-9/]+".toRegex(), "") }
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
  showUser()
}

/**
 * @return 是否为交易贴
 * @date 2019-04-18
 * @author SunYiBo
 */
private fun Post.notSale() =
    type in listOf(1, 2, 3, 4, 5, 44, 54)
        && "谨慎出价" !in text && "謹慎出價" !in text
        && "未成年人" !in text && "截止日期" !in text && "到付" !in text

/**
 * 展示用户的帖子
 * @date 2019-04-11
 * @author SunYiBo
 */
private fun showUser() {
  val users = getUsers()
  val userMap = users.associateBy { it.id }
  val replyMap = getReply().groupBy { it.id }
  val nameMap = users.associate { it.id to it.name }
  getPosts()
      .filter { it.notSale() }
      .groupBy { it.user }
      .filter { it.value.size > 29 }
      .forEach { userId, posts ->
        val user = userMap[userId] ?: return@forEach
        val path = "$ROOT//user//${user.name}"
        File(path).mkdirs()
        posts.forEach { post ->
          val title = post.title.replace("[<>$/?\":*.]".toRegex(), " ")
          File("$path//$title.txt")
              .writeText(
                  "标题：${post.title}\r\n" +
                      "链接：http://wewhisky.com/forum.php?mod=viewthread&tid=${post.id}\r\n" +
                      "发布时间：${SF.format(post.create)}\r\n" +
                      "正文：${post.text.replace("\n", "\r\n")}\r\n\r\n" +
                      "${replyMap[post.id]?.joinToString("\r\n\r\n") { reply ->
                        nameMap[reply.user] + "：" + reply.text.replace("\n", "\r\n")
                      }}"
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
  val users = getUsers()
  val replyList = getReply()
  val posts = getPosts().filter { it.notSale() }
  val postMap = posts.associateBy { it.id }
  val userMap = users.associateBy { it.id }
  val userPost = posts.groupBy { it.user }
  val file = File("$ROOT//UserStatistics.txt")
  file.writeText("帖子热帖top13\r\n\r\n")
  replyList.groupBy { postMap[it.id] }
      .toList()
      .sortedByDescending { it.second.size }
      .take(13)
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
                val post = userPost[user.id] ?: emptyList()
                if (post.size > 20) {
                  val pIds = post.map { it.id }
                  val reply = replyList.filter { it.id in pIds && it.user != user.id }
                  val write = post.sumByDouble { Math.pow(min(it.text.length, 1000).toDouble(), 2.0) }
                  val answer = replyList.filter { it.user == user.id }
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
}
