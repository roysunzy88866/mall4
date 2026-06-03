package space.hearagain.ridemall.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 车机接口。base URL 末尾带 "/",故路径不带前导斜杠。
 * X-Device-Id 由 OkHttp 拦截器统一注入,不在此声明。
 */
interface ApiService {
    @GET("api/home")
    suspend fun home(): HomeDto

    @GET("api/categories")
    suspend fun categories(): List<CategoryDto>

    @GET("api/categories/{id}/products")
    suspend fun categoryProducts(@Path("id") categoryId: Int): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun productDetail(@Path("id") productId: Int): ProductDetailDto

    @POST("api/orders")
    suspend fun createOrder(@Body body: CreateOrderBody): OrderDto

    @GET("api/orders")
    suspend fun orders(): List<OrderDto>

    @GET("api/orders/{id}")
    suspend fun orderDetail(@Path("id") orderId: Int): OrderDto
}
