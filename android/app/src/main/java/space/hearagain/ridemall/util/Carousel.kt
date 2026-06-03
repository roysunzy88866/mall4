package space.hearagain.ridemall.util

/**
 * 轮播索引推进(纯函数)。到末张回到首张;空/单张安全。
 * 自动切换间隔与手动后静默时长见 需求共识 §3 锚点 F4b / F4c,不在此重抄数值。
 */
fun nextBannerIndex(current: Int, count: Int): Int =
    if (count <= 0) 0 else (current + 1) % count

/** 把任意目标索引夹到 [0,count) 范围(圆点点击跳转用)。 */
fun clampBannerIndex(target: Int, count: Int): Int =
    if (count <= 0) 0 else target.coerceIn(0, count - 1)
