package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import space.hearagain.ridemall.util.priceLabel
import space.hearagain.ridemall.util.resolveImageUrl

class FormatTest {
    @Test fun price_prepends_yuan_symbol() {
        assertEquals("¥459.00", priceLabel("459.00"))
        assertEquals("¥29.00", priceLabel("29.00"))
        assertEquals("¥0.00", priceLabel("0.00"))
    }

    @Test fun relative_image_path_resolved_against_base() {
        assertEquals(
            "http://10.0.2.2:8000/uploads/x.png",
            resolveImageUrl("/uploads/x.png", "http://10.0.2.2:8000/"),
        )
    }

    @Test fun relative_without_leading_slash_resolved() {
        assertEquals(
            "http://10.0.2.2:8000/uploads/x.png",
            resolveImageUrl("uploads/x.png", "http://10.0.2.2:8000"),
        )
    }

    @Test fun absolute_url_returned_as_is() {
        assertEquals(
            "https://cdn.example.com/a.png",
            resolveImageUrl("https://cdn.example.com/a.png", "http://10.0.2.2:8000/"),
        )
    }

    @Test fun null_or_blank_image_returns_null() {
        assertNull(resolveImageUrl(null, "http://10.0.2.2:8000/"))
        assertNull(resolveImageUrl("", "http://10.0.2.2:8000/"))
        assertNull(resolveImageUrl("   ", "http://10.0.2.2:8000/"))
    }
}
