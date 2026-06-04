package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Test
import space.hearagain.ridemall.util.parseUnavailableIds

class OrderErrorTest {

    @Test fun parses_unavailable_ids_from_409_body() {
        val body = """{"error":"商品已下架","unavailable":[{"id":1,"name":"a"},{"id":7,"name":"b"}]}"""
        assertEquals(listOf(1, 7), parseUnavailableIds(body))
    }

    @Test fun null_blank_or_no_match_is_empty() {
        assertEquals(emptyList<Int>(), parseUnavailableIds(null))
        assertEquals(emptyList<Int>(), parseUnavailableIds(""))
        assertEquals(emptyList<Int>(), parseUnavailableIds("""{"error":"商品已下架"}"""))
    }
}
