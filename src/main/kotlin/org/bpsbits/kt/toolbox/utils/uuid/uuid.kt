package org.bpsbits.kt.toolbox.utils.uuid

import java.util.UUID

/**
 * Checks if the current [UUID] is a valid version 7 UUID.
 *
 * @return `true` if is a version 7 [UUID], otherwise `false`.
 */
val UUID.isv7: Boolean
    get() = UUIDv7.isV7(this)

/**
 * Validates if this UUID is a version 7 UUID.
 *
 * This extension function checks if the current UUID instance meets the criteria
 * for being a version 7 UUID and performs the validation.
 */
fun UUID.validateIsUUIDv7() {
    UUIDv7.validateIsUUIDv7(this)
}