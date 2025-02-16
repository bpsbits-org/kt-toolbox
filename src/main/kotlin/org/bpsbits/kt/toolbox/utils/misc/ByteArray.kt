package org.bpsbits.kt.toolbox.utils.misc

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}
