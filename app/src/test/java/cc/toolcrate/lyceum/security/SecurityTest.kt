package cc.toolcrate.lyceum.security

import cc.toolcrate.lyceum.domain.PinRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test
    fun verifiesTheRightPinOnly() {
        val record = PinHasher.hash("2468", iterations = 1_000)
        assertTrue(PinHasher.verify("2468", record))
        assertFalse(PinHasher.verify("2469", record))
        assertFalse(PinHasher.verify("", record))
    }

    @Test
    fun saltsEachHash() {
        assertNotEquals(PinHasher.hash("1234", iterations = 1_000), PinHasher.hash("1234", iterations = 1_000))
    }

    @Test
    fun neverStoresThePin() {
        assertFalse(PinHasher.hash("13579", iterations = 1_000).contains("13579"))
    }

    @Test
    fun rejectsMangledRecords() {
        assertFalse(PinHasher.verify("1234", "garbage"))
        assertFalse(PinHasher.verify("1234", "v2:1000:AAAA:AAAA"))
        assertFalse(PinHasher.verify("1234", "v1:x:AAAA:AAAA"))
    }
}

class PinRulesTest {
    @Test
    fun shape() {
        assertTrue(PinRules.isValid("0000"))
        assertTrue(PinRules.isValid("12345678"))
        assertFalse(PinRules.isValid("123"))
        assertFalse(PinRules.isValid("123456789"))
        assertFalse(PinRules.isValid("12a4"))
    }

    @Test
    fun backoffIsFreeAtFirstThenDoublesToACap() {
        assertEquals(0, PinRules.backoffMillis(5))
        assertEquals(30_000, PinRules.backoffMillis(6))
        assertEquals(60_000, PinRules.backoffMillis(7))
        assertEquals(3_600_000, PinRules.backoffMillis(40))
    }

    @Test
    fun waitRemainingCountsDown() {
        assertEquals(20_000, PinRules.waitRemaining(6, lastFailureAt = 1_000, now = 11_000))
        assertEquals(0, PinRules.waitRemaining(6, lastFailureAt = 1_000, now = 100_000))
        assertEquals(0, PinRules.waitRemaining(2, lastFailureAt = 1_000, now = 1_000))
    }
}
