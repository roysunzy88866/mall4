package space.hearagain.ridemall.util

private val UNAVAILABLE_ID = Regex(""""id"\s*:\s*(\d+)""")

/**
 * 从下单 409「商品已下架」错误体里解析出不可用商品的 id 列表。
 * 后端体形如:{"error":"商品已下架","unavailable":[{"id":1,"name":"x"}]}。
 * 容错:null / 空 / 无匹配 → 空列表(纯函数,便于单测)。
 */
fun parseUnavailableIds(errorBody: String?): List<Int> {
    if (errorBody.isNullOrBlank()) return emptyList()
    return UNAVAILABLE_ID.findAll(errorBody).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
}
