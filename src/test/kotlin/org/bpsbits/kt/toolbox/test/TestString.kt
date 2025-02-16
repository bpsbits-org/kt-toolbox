package org.bpsbits.kt.toolbox.test

import org.junit.jupiter.api.Assertions.*
import org.bpsbits.kt.toolbox.utils.string.base64
import org.bpsbits.kt.toolbox.utils.string.doubleQuoteIfContainsUppercase
import org.bpsbits.kt.toolbox.utils.string.matchesJSessID
import org.bpsbits.kt.toolbox.utils.string.matchesUUID
import org.bpsbits.kt.toolbox.utils.string.md5AsBase64
import org.bpsbits.kt.toolbox.utils.string.md5AsUUID
import org.bpsbits.kt.toolbox.utils.string.parseISODate
import org.bpsbits.kt.toolbox.utils.string.toPgFunctionQuery
import org.bpsbits.kt.toolbox.utils.string.toPgProcedureQuery
import org.bpsbits.kt.toolbox.utils.string.toPgSqlIdentifier
import org.bpsbits.kt.toolbox.utils.string.validateISODate
import java.util.Base64
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.text.toByteArray

class TestString {

    @Test
    fun `base64 should encode string to Base64 correctly`() {
        val input = "Hello, World!"
        val expected = Base64.getEncoder().encodeToString(input.toByteArray())
        val result = input.base64()
        assertEquals(expected, result)
    }

    @Test
    fun `base64 should encode empty string to Base64`() {
        val input = ""
        val expected = Base64.getEncoder().encodeToString(input.toByteArray())
        val result = input.base64()
        assertEquals(expected, result)
    }

    @Test
    fun `base64 should encode non-ASCII characters correctly`() {
        val input = "Привет, мир!" // "Hello, World!" in Russian
        val expected = Base64.getEncoder().encodeToString(input.toByteArray())
        val result = input.base64()
        assertEquals(expected, result)
    }

    @Test
    fun `doubleQuoteIfContainsUppercase should double-quote string if it contains uppercase characters`() {
        val input = "hello.World"
        val expected = "\"hello.World\""
        val result = input.doubleQuoteIfContainsUppercase()
        assertEquals(expected, result)
    }

    @Test
    fun `doubleQuoteIfContainsUppercase should not double-quote string if it does not contain uppercase characters`() {
        val input = "super.bowl"
        val expected = "super.bowl"
        val result = input.doubleQuoteIfContainsUppercase()
        assertEquals(expected, result)
    }

    @Test
    fun `doubleQuoteIfContainsUppercase should remove existing double quotes before checking for uppercase`() {
        val input = "\"HelloWorld\""
        val expected = "\"HelloWorld\""
        val result = input.doubleQuoteIfContainsUppercase()
        assertEquals(expected, result)
    }

    @Test
    fun `doubleQuoteIfContainsUppercase should handle empty string correctly`() {
        val input = ""
        val expected = ""
        val result = input.doubleQuoteIfContainsUppercase()
        assertEquals(expected, result)
    }

    @Test
    fun `doubleQuoteIfContainsUppercase should handle string with only lowercase and quotes correctly`() {
        val input = "\"superbowl\""
        val expected = "superbowl"
        val result = input.doubleQuoteIfContainsUppercase()
        assertEquals(expected, result)
    }

    @Test
    fun `toPgSqlIdentifier should wrap parts starting with numbers in quotes`() {
        val input = "123abc.456.validPart.VALID.7invalidPart"
        val expected = "\"123abc\".\"456\".\"validPart\".\"VALID\".\"7invalidPart\""
        val result = input.toPgSqlIdentifier()
        assertEquals(expected, result)
        val input2 = "api.getProductsByCategoryId"
        val expected2 = "api.\"getProductsByCategoryId\""
        val result2 = input2.toPgSqlIdentifier()
        assertEquals(expected2, result2)
    }

    @Test
    fun `toPgSqlIdentifier should throw an exception for invalid input`() {
        val input = " .  .   . ......."
        assertThrows(IllegalArgumentException::class.java) {
            input.toPgSqlIdentifier()
        }
        val input2 = "    "
        assertThrows(IllegalArgumentException::class.java) {
            input2.toPgSqlIdentifier()
        }
        val input3 = ""
        assertThrows(IllegalArgumentException::class.java) {
            input3.toPgSqlIdentifier()
        }
        val input4 = "."
        assertThrows(IllegalArgumentException::class.java) {
            input4.toPgSqlIdentifier()
        }
    }

    @Test
    fun `matchesUUID should return true for valid UUIDs`() {
        val validUUID1 = "123e4567-e89b-12d3-a456-426614174000"
        val validUUID2 = "550E8400-E29B-41D4-A716-446655440000"
        val validUUID3 = "a8098c1a-f86e-11da-bd1a-00112444be1e"
        assertTrue(validUUID1.matchesUUID)
        assertTrue(validUUID2.matchesUUID)
        assertTrue(validUUID3.matchesUUID)
    }

    @Test
    fun `matchesUUID should return false for invalid UUIDs`() {
        val invalidUUID1 = "123e4567-e89b-12d3-a456-42661417400"  // Too short
        val invalidUUID2 = "123e4567-e89b-12d3-a456-4266141740000" // Too long
        val invalidUUID3 = "g23e4567-e89b-12d3-a456-426614174000"  // Invalid character ('g')
        val invalidUUID4 = "123e4567e89b12d3a456426614174000"      // Missing dashes
        val invalidUUID5 = ""                                      // Empty string
        val invalidUUID6 = "550e8400-e29b-41d4-a716-44665544000z"  // Ends with invalid char
        assertFalse(invalidUUID1.matchesUUID)
        assertFalse(invalidUUID2.matchesUUID)
        assertFalse(invalidUUID3.matchesUUID)
        assertFalse(invalidUUID4.matchesUUID)
        assertFalse(invalidUUID5.matchesUUID)
        assertFalse(invalidUUID6.matchesUUID)
    }

    @Test
    fun `matchesUUID should handle UUID format case insensitivity`() {
        val lowercaseUUID = "550e8400-e29b-41d4-a716-446655440000"
        val uppercaseUUID = "550E8400-E29B-41D4-A716-446655440000"
        assertTrue(lowercaseUUID.matchesUUID)
        assertTrue(uppercaseUUID.matchesUUID)
    }

    @Test
    fun `parseISODate should parse valid ISO date strings`() {
        val validDate1 = "2023-10-18"
        val validDate2 = "2000-01-01"
        val validDate3 = "1999-12-31"
        assertEquals(LocalDate.of(2023, 10, 18), validDate1.parseISODate())
        assertEquals(LocalDate.of(2000, 1, 1), validDate2.parseISODate())
        assertEquals(LocalDate.of(1999, 12, 31), validDate3.parseISODate())
    }

    @Test
    fun `parseISODate should return null for invalid ISO date strings`() {
        val invalidDate1 = "2023-02-30"     // Invalid day
        val invalidDate2 = "2023-13-01"     // Invalid month
        val invalidDate3 = "2023/10/18"     // Invalid format (slashes instead of dashes)
        val invalidDate4 = "18-10-2023"     // Invalid format (wrong order)
        val invalidDate5 = "20231018"       // Missing delimiters
        val invalidDate6 = ""               // Empty string
        val invalidDate7 = "not-a-date"     // Non-date string
        assertNull(invalidDate1.parseISODate())
        assertNull(invalidDate2.parseISODate())
        assertNull(invalidDate3.parseISODate())
        assertNull(invalidDate4.parseISODate())
        assertNull(invalidDate5.parseISODate())
        assertNull(invalidDate6.parseISODate())
        assertNull(invalidDate7.parseISODate())
    }

    @Test
    fun `parseISODate should handle edge cases for valid dates`() {
        val validDate1 = "0001-01-01"     // Earliest date supported
        val validDate2 = "9999-12-31"     // Latest date supported
        assertEquals(LocalDate.of(1, 1, 1), validDate1.parseISODate())
        assertEquals(LocalDate.of(9999, 12, 31), validDate2.parseISODate())
    }

    @Test
    fun `parseISODate should handle leap years`() {
        val validLeapDate = "2020-02-29"  // Valid leap year date
        val invalidLeapDate = "2021-02-29" // Invalid leap year date
        assertEquals(LocalDate.of(2020, 2, 29), validLeapDate.parseISODate()) // Parsed successfully
        assertNull(invalidLeapDate.parseISODate()) // Should return null
    }

    @Test
    fun `validateISODate does not throw an Exception`() {
        assertDoesNotThrow {
            "2023-10-25".validateISODate() // Should not throw any exception
        }
    }

    @Test
    fun `validateISODate throws an error if invalid ISO date format`() {
        assertThrows(Exception::class.java) {
            "25-10-2023".validateISODate()
        }
        assertThrows(Exception::class.java) {
            "2023-02-30".validateISODate()
        }
        assertThrows(Exception::class.java) {
            "".validateISODate()
        }
        assertThrows(Exception::class.java) {
            "superbowl".validateISODate()
        }
    }

    @Test
    fun `md5AsUUID generates correct UUID`() {
        val input = "test-string"
        val expectedMd5 = "661f8009fa8e56a9d0e94a0a644397d7"
        val result = input.md5AsUUID()
        val uuidString = expectedMd5
            .replaceRange(20, 20, "-")
            .replaceRange(16, 16, "-")
            .replaceRange(12, 12, "-")
            .replaceRange(8, 8, "-")
        assertEquals(UUID.fromString(uuidString), result, "Generated UUID does not match expected value")
    }

    @Test
    fun `md5AsUUID generates valid UUID from empty string`() {
        val input = ""
        val expectedMd5 = "d41d8cd98f00b204e9800998ecf8427e" // MD5 hash of empty string
        val result = input.md5AsUUID()
        val uuidString = expectedMd5
            .replaceRange(20, 20, "-")
            .replaceRange(16, 16, "-")
            .replaceRange(12, 12, "-")
            .replaceRange(8, 8, "-")
        assertEquals(
            UUID.fromString(uuidString),
            result,
            "Generated UUID for empty input does not match expected value"
        )
    }

    @Test
    fun `matchesJSessID passes various JSESSIONID scenarios`() {
        val testCases = listOf(
            // Valid cases
            "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6" to true, // Valid uppercase
            "a1b2C3d4E5F6g7H8i9j0K1l2M3n4o5P6" to true, // Valid mixed-case
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" to true,  // Lowercase only
            // Invalid cases
            "A1B2C3D4E5F6G7H8" to false,                          // Too short
            "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6789" to false,      // Too long
            "A1B2C3D4E5F6G7H8I!J#L2M3N4O5P6" to false,           // Contains special chars
            "A1B2 C3D4E5F6G7H8I9J0K1L2M3N4O5P6" to false,        // Contains spaces
            "" to false                                           // Empty string
        )
        for ((sessionId, expectedResult) in testCases) {
            val result = sessionId.matchesJSessID
            if (expectedResult) {
                assertTrue(result, "Expected `$sessionId` to match the regex but it didn't.")
            } else {
                assertFalse(result, "Expected `$sessionId` to NOT match the regex but it did.")
            }
        }
    }

    @Test
    fun `md5AsBase64 passes various scenarios`() {
        assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", "".md5AsBase64())
        assertEquals("8FgeYne0jNeM2hQgYqGy9g==", "Hello Robert!".md5AsBase64())
    }

    @Test
    fun `toPgProcedureQuery sanitizes given data`() {
        assertEquals("call api.do();", "api.do".toPgProcedureQuery(0))
        assertEquals("call api.do();", "api.do".toPgProcedureQuery())
        assertEquals("call \"someProcedure\"();", "someProcedure".toPgProcedureQuery(0))
        assertEquals("call \"someProcedure\"($1);", "someProcedure".toPgProcedureQuery(1))
        assertEquals("call api.\"someProcedure\"($1, $2);", "api.someProcedure".toPgProcedureQuery(2))
        assertEquals("call api.\"  someProcedure \"($1, $2);", "api.  someProcedure ".toPgProcedureQuery(2))
        assertEquals("call api.\"  someProcedure \"($1, $2, $3);", "api.  someProcedure ".toPgProcedureQuery(3))
    }

    @Test
    fun `toPgFunctionQuery sanitizes given data`() {
        assertEquals("select api.version();", "api.version".toPgFunctionQuery(0))
        assertEquals("select api.version();", "api.version".toPgFunctionQuery())
        assertEquals("select \"someFunction\"()::varchar;", "someFunction".toPgFunctionQuery(0, "::varchar"))
        assertEquals("select \"someFunction\"($1);", "someFunction".toPgFunctionQuery(1))
        assertEquals("select api.\"someFunction\"($1, $2);", "api.someFunction".toPgFunctionQuery(2))
        assertEquals("select api.\"  someFunction \"($1, $2);", "api.  someFunction ".toPgFunctionQuery(2))
        assertEquals("select api.\"  someFunction \"($1, $2, $3);", "api.  someFunction ".toPgFunctionQuery(3))
    }

    @Test
    fun `toPgProcedureQuery throws an Exception`() {
        assertThrows(Exception::class.java) {
            "".toPgProcedureQuery(5)
        }
        assertThrows(Exception::class.java) {
            " ".toPgProcedureQuery(5)
        }
    }

}