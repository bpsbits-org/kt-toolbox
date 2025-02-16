package org.bpsbits.kt.toolbox.utils.uuid

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

/**
 * A utility for generating and handling version 7 UUIDs.
 */
class UUIDv7 {

    companion object {

        /**
         * Generates a version 7 UUID based on the current timestamp and random values.
         *
         * This UUID uses the current time for its most significant bits, combined with
         * some extra precision for sub-millisecond accuracy. The least significant bits
         * are randomly generated to ensure uniqueness.
         *
         * @return A newly generated [java.util.UUID] version 7.
         */
        fun new(): UUID {
            val currentInstant = Instant.now()
            val epochMilliSeconds = currentInstant.toEpochMilli()
            val nanoAdjustmentWithinMilli = currentInstant.nano % 1_000_000
            val subMillisecondPrecision = (nanoAdjustmentWithinMilli / 100).toLong()
            var highPrecisionTimestamp = (epochMilliSeconds and 0xFFFFFFFFFFFFL shl 16) or
                    (subMillisecondPrecision and 0xFFF shl 4)
            highPrecisionTimestamp = (highPrecisionTimestamp and -0xF001L) or (7L shl 12)
            var random64BitValue = ThreadLocalRandom.current().nextLong()
            random64BitValue = (random64BitValue and 0x3FFFFFFFFFFFFFFFL) or (0x2L shl 62)
            return UUID(highPrecisionTimestamp, random64BitValue)
        }

        /**
         * Checks if the newly generated UUID is version 7.
         *
         * @return True if the UUID is version 7, false otherwise.
         */
        fun isV7(uuidToCheck: UUID): Boolean {
            if (uuidToCheck.version() != 7) return false
            return toDate(uuidToCheck) != null
        }

        fun isStringUUIDv7(uuidToCheck: String): Boolean {
            return try {
                val uuid = UUID.fromString(uuidToCheck)
                isV7(uuid)
            } catch (_: IllegalArgumentException) {
                false
            }
        }

        /**
         * Ensures the provided identifier is a version 7 UUID.
         *
         * This function checks if the given UUID is a version 7 identifier,
         * which includes a timestamp (date and time) and version information.
         * If the UUID is not version 7, it throws an error.
         *
         * @param uuidToValidate The identifier to check.
         * @return The same UUID if it passes the validation.
         * @throws IllegalArgumentException If the UUID is not version 7.
         */
        fun validateIsUUIDv7(uuidToValidate: UUID): UUID {
            require(isV7(uuidToValidate)) { "Not v7 UUID: $uuidToValidate. Should have datetime and version number." }
            return uuidToValidate
        }

        /**
         * Creates a new UUID as a string by calling the [new] function.
         *
         * @return A [UUID] in string format.
         */
        fun newString(): String {
            return new().toString()
        }

        /**
         * Converts a version 7 UUID to a date and time.
         *
         * @return The date and time from the [UUID], or `null` if the UUID is not version 7.
         */
        fun toDate(uuid: UUID): ZonedDateTime? {
            if (uuid.version() != 7) return null
            val mostSignificantBits = uuid.mostSignificantBits
            val timestamp = (mostSignificantBits shr 16) and 0xFFFFFFFFFFFFL
            return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC)
        }

    }

}