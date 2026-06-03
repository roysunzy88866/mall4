package space.hearagain.ridemall.data.repository

import space.hearagain.ridemall.data.api.ApiService
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.model.Product

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
}
