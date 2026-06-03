package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import space.hearagain.ridemall.data.ensureDeviceId

class DeviceIdTest {
    @Test fun generates_when_absent() {
        val id = ensureDeviceId(null) { "car-generated" }
        assertEquals("car-generated", id)
    }

    @Test fun generates_when_blank() {
        assertEquals("car-generated", ensureDeviceId("") { "car-generated" })
        assertEquals("car-generated", ensureDeviceId("   ") { "car-generated" })
    }

    @Test fun reuses_when_present() {
        var called = false
        val id = ensureDeviceId("car-existing") { called = true; "car-new" }
        assertEquals("car-existing", id)
        assertTrue("有值时不应再生成", !called)
    }
}
