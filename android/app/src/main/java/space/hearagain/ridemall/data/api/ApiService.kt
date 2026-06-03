package space.hearagain.ridemall.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 车机只读接口(本刀仅浏览侧三个)。base URL 末尾带 "/",故路径不带前导斜杠。
 * X-Device-Id 由 OkHttp 拦截器统一注入,不在此声明。
 */
interface ApiService {
    @GET("api/home")
    suspend fun home(): HomeDto

    @GET("api/categories")
    suspend fun categories(): List<CategoryDto>

    @GET("api/categories/{id}/products")
    suspend fun categoryProducts(@Path("id") categoryId: Int): List<ProductDto>
}
