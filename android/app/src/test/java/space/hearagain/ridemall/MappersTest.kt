package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import space.hearagain.ridemall.data.api.BannerDto
import space.hearagain.ridemall.data.api.CategoryDto
import space.hearagain.ridemall.data.api.HomeDto
import space.hearagain.ridemall.data.api.ProductDto
import space.hearagain.ridemall.data.repository.toModel

class MappersTest {
    private val base = "http://10.0.2.2:8000/"

    @Test fun product_dto_maps_with_price_label() {
        val dto = ProductDto(19, "车载垃圾桶 带盖", "29.00", 2900, "磁吸开合", 5)
        val m = dto.toModel()
        assertEquals(19, m.id)
        assertEquals("车载垃圾桶 带盖", m.name)
        assertEquals("¥29.00", m.priceLabel)
        assertEquals(2900, m.priceCents)
        assertEquals(5, m.categoryId)
    }

    @Test fun product_null_description_becomes_empty() {
        val m = ProductDto(1, "x", "1.00", 100, null, 1).toModel()
        assertEquals("", m.description)
    }

    @Test fun banner_dto_maps_and_resolves_image() {
        val dto = BannerDto(7, "便携充电枪 7kW 家用", "7kW 家用便携充电", "459.00", 45900, "/uploads/placeholder.png")
        val m = dto.toModel(base)
        assertEquals(7, m.productId)
        assertEquals("¥459.00", m.priceLabel)
        assertEquals("http://10.0.2.2:8000/uploads/placeholder.png", m.imageUrl)
    }

    @Test fun banner_null_image_resolves_null() {
        val m = BannerDto(1, "t", "d", "1.00", 100, null).toModel(base)
        assertNull(m.imageUrl)
    }

    @Test fun category_dto_maps() {
        val m = CategoryDto(1, "车载用品").toModel()
        assertEquals(1, m.id)
        assertEquals("车载用品", m.name)
    }

    @Test fun home_dto_maps_lists() {
        val home = HomeDto(
            banners = listOf(BannerDto(7, "t", "d", "459.00", 45900, "/uploads/p.png")),
            recommended = listOf(ProductDto(19, "n", "29.00", 2900, "d", 5)),
        ).toModel(base)
        assertEquals(1, home.banners.size)
        assertEquals(1, home.recommended.size)
        assertEquals("¥459.00", home.banners[0].priceLabel)
        assertEquals("¥29.00", home.recommended[0].priceLabel)
    }
}
