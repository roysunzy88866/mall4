package space.hearagain.ridemall.data.repository

import space.hearagain.ridemall.data.api.BannerDto
import space.hearagain.ridemall.data.api.CategoryDto
import space.hearagain.ridemall.data.api.HomeDto
import space.hearagain.ridemall.data.api.ProductDto
import space.hearagain.ridemall.model.Banner
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.util.priceLabel
import space.hearagain.ridemall.util.resolveImageUrl

/**
 * DTO → UI Model 映射(纯函数,可单测)。在此完成价格加 ¥、图 URL 补全两条呈现规则。
 */
fun ProductDto.toModel(): Product = Product(
    id = id,
    name = name,
    priceLabel = priceLabel(price),
    priceCents = priceCents,
    description = description ?: "",
    categoryId = categoryId,
)

fun BannerDto.toModel(baseUrl: String): Banner = Banner(
    productId = productId,
    title = title,
    description = description ?: "",
    priceLabel = priceLabel(price),
    priceCents = priceCents,
    imageUrl = resolveImageUrl(image, baseUrl),
)

fun CategoryDto.toModel(): NavCategory = NavCategory(id = id, name = name)

fun HomeDto.toModel(baseUrl: String): HomeData = HomeData(
    banners = banners.map { it.toModel(baseUrl) },
    recommended = recommended.map { it.toModel() },
)
