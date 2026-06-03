package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Test
import space.hearagain.ridemall.util.orderStatusLabel
import space.hearagain.ridemall.util.secondsToClock

class OrderFormatTest {
    @Test fun seconds_to_clock() {
        assertEquals("02:59", secondsToClock(179))
        assertEquals("00:00", secondsToClock(0))
        assertEquals("01:00", secondsToClock(60))
        assertEquals("00:09", secondsToClock(9))
        assertEquals("00:00", secondsToClock(-5)) // 不出现负数
    }

    @Test fun status_label_maps_known_and_passes_unknown() {
        assertEquals("待发货", orderStatusLabel("paid"))
        assertEquals("已签收", orderStatusLabel("delivered"))
        assertEquals("已退款", orderStatusLabel("refunded"))
        assertEquals("某新状态", orderStatusLabel("某新状态")) // 未知原样返回
    }
}
