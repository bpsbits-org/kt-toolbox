package org.bpsbits.kt.toolbox.utils.string

import org.bpsbits.kt.toolbox.utils.uuid.UUIDv7
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

/**
 * Encodes the current string into its Base64 representation.
 *
 * @return The Base64-encoded representation of the current string.
 */
fun String.base64(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}

/**
 * Wraps the string with double quotes if it contains uppercase letters.
 *
 * This is useful when the string is being used as the name of an PostgreSQL column, table, or function.
 * The method also removes any existing double quotes from the string before evaluating it.
 *
 * @return The modified string wrapped in double quotes if it contains uppercase letters;
 * otherwise, the string without any double quotes.
 */
fun String.doubleQuoteIfContainsUppercase(): String {
    val sanitizedString = this.replace("\"", "")
    return if (sanitizedString.any { it.isUpperCase() }) {
        "\"$sanitizedString\""
    } else {
        sanitizedString
    }
}

fun String.doubleQuoteIfNotSafe(): String {
    if (this.firstOrNull()?.isDigit() == true || this.any { it.isUpperCase() || !it.isLetterOrDigit() }
    ) {
        return "\"$this\""
    }
    return this
}

/**
 * Converts the string into a PostgreSQL-compatible identifier.
 *
 * - Removes all double quotes (`"`).
 * - Replaces multiple dots (`.`) with a single dot.
 * - Quotes parts with uppercase or non-alphanumeric characters.
 *
 * @return A valid PostgreSQL identifier.
 */
fun String.toPgSqlIdentifier(): String {
    require(this.isNotBlank()) { "Identifier cannot be empty or null" }
    val result = this
        .replace("\"", "")
        .replace(Regex("\\.+"), ".")
        .split(".")
        .filter { it.isNotBlank() }
        .joinToString(".") { part ->
            if (part.any { it.isUpperCase() || (!it.isDigit() && !it.isLetter()) } ||
                part.firstOrNull()?.isDigit() == true) {
                "\"$part\""
            } else {
                part
            }
        }
    require(result.isNotBlank()) { "Invalid identifier: $result" }
    return result
}

/**
 * Validates if the current string matches the ISO date format (`yyyy-MM-dd`) and represents a valid calendar date.
 *
 * This method checks both:
 * 1. The format of the string (ISO date format).
 * 2. If the string can be successfully parsed as a valid date.
 *
 * @return `true` if the string is in the correct ISO format and represents a valid date; `false` otherwise.
 */
fun String.isValidISODate(): Boolean {
    if (!this.matchesISODateFormat) return false
    return try {
        LocalDate.parse(this)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}

/**
 * Checks if the current string matches the ISO date format (yyyy-MM-dd).
 *
 * Note: This method only validates the format, not whether the string represents a valid date.
 *
 * @return `true` if the string matches the ISO date format; `false` otherwise.
 */
val String.matchesISODateFormat
    get():Boolean {
        val isoDateRegex = Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])$""")
        return isoDateRegex.matches(this)
    }

/**
 * Checks if the current string matches the classic JSESSIONID pattern.
 *
 * @return `true` if the string matches the JSESSIONID pattern; `false` otherwise.
 *
 * Note: The pattern uses a wider range `[A-Z0-9]` for compatibility,
 * as the exact character requirements may vary across implementations of Tomcat.
 * Might be applied as an initial test prior to carrying out any more tasks.
 */
val String.matchesJSessID
    get(): Boolean {
        val regexPattern = Regex("^[A-Z0-9]{32}$", RegexOption.IGNORE_CASE)
        return regexPattern.matches(this.toString())
    }

/**
 * Determines if the current string is a valid UUID.
 *
 * @return `true` if the string matches the UUID format; `false` otherwise.
 */
val String.matchesUUID
    get(): Boolean {
        val uuidRegex = Regex(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            RegexOption.IGNORE_CASE
        )
        return uuidRegex.matches(this)
    }

/**
 * Computes the MD5 hash of the current string and returns it as a 32-character hexadecimal string.
 *
 * @return The MD5 hash of the string, represented in hexadecimal format and zero-padded to 32 characters.
 */
fun String.md5(): String {
    val md5Digest = MessageDigest.getInstance("MD5")
    val hashBytes = md5Digest.digest(this.toByteArray())
    return BigInteger(1, hashBytes).toString(16).padStart(32, '0')
}

/**
 * Converts the current string into an MD5 hash and encodes it as a Base64 string.
 *
 * @return The MD5 hash of the string, represented in Base64 format.
 */
fun String.md5AsBase64(): String {
    val md = MessageDigest.getInstance("MD5")
    return Base64.getEncoder().encodeToString(md.digest(this.toByteArray()))
}

/**
 * Converts the current string into an MD5 hash and formats it as a [UUID].
 *
 * Please note that this method does not provide absolutely valid UUID,
 * instead it formats md5 hash as a UUID string and then converts it to UUID.
 *
 * @return The MD5 hash of the string represented as a [UUID].
 */
fun String.md5AsUUID(): UUID {
    val uuidString = this.md5()
        .replaceRange(20, 20, "-")
        .replaceRange(16, 16, "-")
        .replaceRange(12, 12, "-")
        .replaceRange(8, 8, "-")
    return UUID.fromString(uuidString)
}

/**
 * If string contains valid ISO date, then returns a [LocalDate].
 * @see [LocalDate]
 */
fun String.parseISODate(): LocalDate? {
    return try {
        return LocalDate.parse(this, DateTimeFormatter.ISO_DATE).takeIf { this.matchesISODateFormat }
    } catch (_: Exception) {
        null
    }
}

/**
 * Validates if the current text is a valid ISO date.
 * @throws [IllegalArgumentException] if the text is not in the correct date format.
 */
fun String.validateISODate() {
    require(this.matchesISODateFormat) {
        "Invalid date format! Expected format: yyyy-MM-dd (e.g., 2012-01-01)."
    }
    require(this.isValidISODate()) {
        "Invalid date: `$this`."
    }
}

/**
 * Checks if the string is a valid version 7 UUID.
 *
 * @return `true` if the string is a version 7 [UUID], otherwise `false`.
 */
val String.isUUIDv7: Boolean
    get() = try {
        UUIDv7.isStringUUIDv7(this)
    } catch (_: Throwable) {
        false
    }

/**
 * Sanitizes the given string, so it is safer to use in an SQL query as function name.
 * For example, converts `api.getUserNames` into `api."getUserNames"`.
 */
fun String.toSanitizedPgFuncName(): String {
    val sanitized = this.replace(Regex("\\.+"), ".")
    val parts = sanitized.split('.', limit = 2)
    return parts.joinToString(".") { it.doubleQuoteIfNotSafe() }
}

/**
 * Converts the given string into a valid PostgreSQL function query with placeholders for parameters.
 * For example, the string `api.getUserNames` could be converted to `select api."getUserNames"($1);`.
 * @param argsCount Count of arguments to be used in the query.
 * @param suffix String to be added to the end of the query.
 * You can use this to add conversion of a function result to a specific data type.
 */
fun String.toPgFunctionQuery(argsCount: Int = 0, suffix: String = ""): String {
    require(this.isNotBlank()) { "Function name cannot be blank." }
    val fnName = this.toSanitizedPgFuncName()
    require(fnName.isNotBlank()) { "Function name cannot be blank." }
    val varPlaceHolders = (1..argsCount).joinToString { "$$it" }
    val sanSuffix = suffix.trim()
    return "select $fnName($varPlaceHolders)$sanSuffix;"
}

/**
 * Converts the given string into a valid PostgreSQL procedure call query with placeholders for parameters.
 * For example, the string `api.markFileExpired` could be converted to `call api."markFileExpired"($1);`.
 * @param argsCount Count of arguments to be used in the query.
 */
fun String.toPgProcedureQuery(argsCount: Int = 0): String {
    require(this.isNotBlank()) { "Procedure name cannot be blank." }
    val fnName = this.toSanitizedPgFuncName()
    require(fnName.isNotBlank()) { "Procedure name cannot be blank." }
    val varPlaceHolders = (1..argsCount).joinToString { "$$it" }
    return "call $fnName($varPlaceHolders);"
}