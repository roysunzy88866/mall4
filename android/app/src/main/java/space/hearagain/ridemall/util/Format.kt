package space.hearagain.ridemall.util

/**
 * 呈现规则纯函数(无 IO,100% 可单测)。
 */

/** 后端 price 为不含符号的两位小数串(如 "459.00"),车机加 `¥`(共识 A5)。 */
fun priceLabel(rawPrice: String): String = "¥$rawPrice"

/**
 * 把后端返回的图片路径补全为可加载的绝对 URL。
 * - 已是 http(s) 绝对地址 → 原样返回。
 * - 相对路径(如 "/uploads/x.png")→ 用 baseUrl 补全。
 * - 空/null → 返回 null(上层用占位)。
 */
fun resolveImageUrl(path: String?, baseUrl: String): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = baseUrl.trimEnd('/')
    val rel = if (path.startsWith("/")) path else "/$path"
    return "$base$rel"
}
