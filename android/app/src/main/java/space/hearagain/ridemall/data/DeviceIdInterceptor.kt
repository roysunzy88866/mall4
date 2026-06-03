package space.hearagain.ridemall.data

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 给每个后端请求统一注入 `X-Device-Id` 头(共识 A9 / 后端需求 §2)。
 * 设备号只读自 [DeviceIdStore],不在各调用点散落。
 */
class DeviceIdInterceptor(private val store: DeviceIdStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-Device-Id", store.deviceId)
            .build()
        return chain.proceed(request)
    }
}
