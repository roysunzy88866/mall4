package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Test
import space.hearagain.ridemall.util.clampBannerIndex
import space.hearagain.ridemall.util.nextBannerIndex

class CarouselTest {
    @Test fun advances_and_wraps_to_first() {
        assertEquals(1, nextBannerIndex(0, 4))
        assertEquals(2, nextBannerIndex(1, 4))
        assertEquals(3, nextBannerIndex(2, 4))
        assertEquals(0, nextBannerIndex(3, 4)) // 末张回首张
    }

    @Test fun single_or_empty_is_safe() {
        assertEquals(0, nextBannerIndex(0, 1))
        assertEquals(0, nextBannerIndex(0, 0))
    }

    @Test fun clamp_into_range() {
        assertEquals(0, clampBannerIndex(-3, 4))
        assertEquals(3, clampBannerIndex(9, 4))
        assertEquals(2, clampBannerIndex(2, 4))
        assertEquals(0, clampBannerIndex(2, 0))
    }
}
