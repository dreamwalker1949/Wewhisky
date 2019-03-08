package syb.util


/**
 * @param s 字符串
 * @param i 匹配组
 * @return 第一个匹配结果
 */
fun Regex.matchGroup(s: String, i: Int = 1) = find(s)?.groups?.get(i)?.value

/**
 * @param s 字符串
 * @param i 匹配组
 * @return 所有匹配结果
 */
fun Regex.matchGroups(s: String, i: Int = 1) = findAll(s).mapNotNull { it.groups[i]?.value }.toList()
