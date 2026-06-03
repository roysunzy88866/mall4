package space.hearagain.ridemall.data.repository

import space.hearagain.ridemall.data.api.ApiService
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.model.OrderItem
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.model.ProductDetail

/**
 * 数据层:调接口 + DTO→Model 映射。不缓存(共识 §1:车机不本地存业务数据,每次实时拉)。
 * 失败(网络/解析)直接抛出,由 ViewModel 捕获转「网络异常」态。
 */
class StoreRepository(
    private val api: ApiService,
    private val baseUrl: String,
) {
    suspend fun home(): HomeData = api.home().toModel(baseUrl)

    suspend fun categories(): List<NavCategory> = api.categories().map { it.toModel() }

    suspend fun categoryProducts(categoryId: Int): List<Product> =
        api.categoryProducts(categoryId).map { it.toModel() }

    suspend fun productDetail(productId: Int): ProductDetail =
        api.productDetail(productId).toModel(baseUrl)

    /** 先落库再成功(D4):成功返回订单,失败抛出。 */
    suspend fun createOrder(items: List<OrderItem>): Order =
        api.createOrder(items.toCreateBody()).toModel(baseUrl)

    suspend fun orders(): List<Order> = api.orders().map { it.toModel(baseUrl) }
}
