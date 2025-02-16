package org.bpsbits.kt.toolbox.test

import org.bpsbits.kt.toolbox.utils.string.isUUIDv7
import org.bpsbits.kt.toolbox.utils.uuid.UUIDv7
import org.bpsbits.kt.toolbox.utils.uuid.isv7
import org.bpsbits.kt.toolbox.utils.uuid.validateIsUUIDv7
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import java.time.ZoneOffset

class TestUUIDv7 {

    @Test
    fun `UUIDV7 new should generate version 7 UUID`() {
        val iterationCount = 250_000
        repeat(iterationCount) {
            val uuid = UUIDv7.new()
            assertTrue(UUIDv7.new().isv7, "Generated UUID is not a valid version 7 UUID: $uuid")
        }
    }

    @Test
    fun `UUIDV7 newString should generate string version of UUUD v7`() {
        val iterationCount = 250_000
        repeat(iterationCount) {
            val uuidString = UUIDv7.newString()
            assertTrue(uuidString.isUUIDv7, "Generated string is not a valid version 7 UUID: $uuidString")
        }
    }

    @Test
    fun `UUIDV7 toDate should parse correct time from UUIDv7`() {
        // 2025-02-14 23:16:33.595
        var uuid = UUID.fromString("019506bf-e2bb-79c0-8951-a604b83ebd55")
        var expectedDate = ZonedDateTime.of(2025, 2, 14, 23, 16, 33, 595000000, ZoneOffset.UTC)
        var actualDate = UUIDv7.toDate(uuid)
        assertEquals(expectedDate, actualDate)
        // 2024-06-14 10:14:09.300
        uuid = UUID.fromString("0190163d-8694-739b-aea5-966c26f8ad91")
        expectedDate = ZonedDateTime.of(2024, 6, 14, 10, 14, 9, 300000000, ZoneOffset.UTC)
        actualDate = UUIDv7.toDate(uuid)
        assertEquals(expectedDate, actualDate)
    }

    @Test
    fun `UUIDV7 toDate should return null for non UUIDv7`() {
        val nonUUIDv7 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        assertNull(UUIDv7.toDate(nonUUIDv7), "Expected null for non-UUIDv7 input")
    }

    @Test
    fun `UUIDV7 toDate should return null for random invalid UUID`() {
        val result = UUIDv7.toDate(UUID.randomUUID())
        assertEquals(result != null, UUID.randomUUID().version() == 7, "Unexpected result for UUID version check")
    }

    @Test
    fun `UUID validateIsUUIDv7 should not throw an Exception`() {
        val iterationCount = 25_000
        repeat(iterationCount) {
            assertDoesNotThrow {
                UUIDv7.new().validateIsUUIDv7()
            }
        }
    }

    @Test
    fun `UUID validateIsUUIDv7 should throw an Exception`() {
        val iterationCount = 25_000
        repeat(iterationCount) {
            assertThrows(IllegalArgumentException::class.java) {
                UUID.randomUUID().validateIsUUIDv7()
            }
        }
    }

}