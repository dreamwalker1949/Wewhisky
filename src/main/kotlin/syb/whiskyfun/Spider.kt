package syb.whiskyfun

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import syb.util.*
import java.io.File
import kotlin.math.max

/**
 * Spider for whiskyfun
 * date 2019-10-12
 * @author SunYiBo
 */
class Spider {

  private companion object {

    val pointReg = "\\d\\d points.{0,37}?\\.".toRegex()
    const val basicUrl = "http://www.whiskyfun.com"
    val abvReg = "\\([^(]{0,37}%|ABV|proof|IB|OB|#\\d\\d".toRegex()
    val urlReg = "archive.*?.html".toRegex()
    val file = File("E://WhiskyFun//Whisky.txt")

    @JvmStatic
    fun main(args: Array<String>) {
      Spider().spiderPage("")
    }

  }

  fun spiderPage(url: String = "") {
    url.print()
    var data = ""
    val whiskies = ArrayList<Whisky>()
    val html = doGet("$basicUrl/$url")
        ?.string()
      ?: return
    val doc = Jsoup.parse(html.replace("<br>", "customSplit"))
        .getElementsByAttributeValue("rowspan", "2")
        .first()
    val dates = doc.getElementsByAttributeValue("size", "3")
        .map { it.text() }
        .filter { it.length > 9 }
    if (doc.getElementsByClass("TextenormalNEW").isNotEmpty()) {
      whiskies.addAll(getWhisky(doc, dates, url))
    } else {
      val children = doc.children()
      var step = 0
      children.forEachIndexed { i, e ->
        val text = e.text()
        dates.find { it in text }
            ?.let {
              data = it
              step = i
              return@forEachIndexed
            }
        if (data.isNotEmpty() && pointReg in text) {
          val s = children.subList(max(step + 1, i - 2), i + 1)
              .flatMap { it.getElementsByAttributeValue("size", "2") }
              .flatMap { it.text().split("customSplit") }
              .filter { it.isNotEmpty() }
              .dropWhile { abvReg !in it && it.length > 9 }
              .joinToString("\n")
              .replace("�", "...")
              .replace("TASTING - ", "")
              .replace("TASTING – ", "")
              .replace("And also", "")
          step = i
          val points = pointReg.findAll(s)
              .map { it.value }
              .toList()
          if (points.size > 1) {
            points.mapIndexed { index, value ->
              var str = s
              if (index > 0) {
                str = s.after(points[index - 1])
              }
              getNote(str.before(value) + value)
            }
          } else {
            listOf(getNote(s))
          }.forEach { l ->
            whiskies.add(Whisky(l[0], l[1], l[2], data, url))
          }
        }
      }
    }
    file.write(whiskies.filter { abvReg !in it.title })
    urlReg.matchGroup(html, 0)
        ?.let { spiderPage(it) }
  }

  private fun getWhisky(element: Element, dates: List<String>, url: String, dataS: String = ""): List<Whisky> {
    var step = 0
    var data = dataS
    val whiskies = ArrayList<Whisky>()
    val children = element.children()
    children.forEachIndexed { i, e ->
      val text = e.text()
      if (text.length > 9) {
        dates.find { it in text }
            ?.let {
              data = it
              step = i + 1
              return@forEachIndexed
            }
        if (data.isNotEmpty() && pointReg in text) {
          if (text.startsWith("Angus's Corner")) {
            var child = e
            while (child.children().count { pointReg in it.text() } < 3) {
              child = child.children().first { pointReg in it.text() }
            }
            whiskies.addAll(getWhisky(child, dates, url, data))
          } else {
            var l = getNoteNew(children.subList(i, i + 1))
            if (abvReg !in l[0]) {
              l = getNoteNew(children.subList(max(step + 1, i - 2), i + 1))
            }
            whiskies.add(Whisky(l[0], l[1], l[2], data, url))
          }
        }
      }
    }
    return whiskies
  }

  private fun getNote(s: String): List<String> {
    val tail = s.after(pointReg.matchGroup(s, 0) ?: " points", true)
    val score = s.dropLast(max(tail.length, 1))
        .takeLastWhile { it != '.' && it != '!' && it != ':' } + tail
    val title = s.before(")") + ")"
    return listOf(title.trim(), score.trim(), s.after(title).dropLast(score.length + 1).trim())
  }

  private fun getNoteNew(list: List<Element>) =
      listOf("textegrandfoncegras", "textenormalgras", "TextenormalNEW")
          .map { name ->
            list.flatMap { it.getElementsByAttributeValue("class", name) }
                .map { it.text() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
          }

}