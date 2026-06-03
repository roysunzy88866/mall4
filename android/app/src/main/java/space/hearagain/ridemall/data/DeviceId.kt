package space.hearagain.ridemall.data

import android.content.Context
import java.util.UUID

/** 设备号「无则生成、有则复用」逻辑(纯函数,可单测)。 */
fun ensureDeviceId(existing: String?, generate: () -> String): String =
    if (existing.isNullOrBlank()) generate() else existing

/**
 * 设备号本地持久化:framework SharedPreferences。本地只存这一项,不缓存任何业务数据(共识 §1 / 后端需求 D3)。
 * 首次启动随机生成 `car-<UUID>` 并写入;之后复用同一值。
 */
class DeviceIdStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        val current = prefs.getString(KEY, null)
        val id = ensureDeviceId(current) { "car-${UUID.randomUUID()}" }
        if (id != current) prefs.edit().putString(KEY, id).apply()
        id
    }

    private companion object {
        const val PREFS = "ridemall_prefs"
        const val KEY = "device_id"
    }
}
