package tech.cuda.woden.service.utils

import java.security.MessageDigest

object Encoder {

    private fun ByteArray.toHex(): String {
        val builder = StringBuffer()
        this.forEach {
            val hexString = Integer.toHexString(it.toInt() and 0xff)
            if (hexString.length == 1) {
                builder.append("0")
            }
            builder.append(hexString)
        }
        return builder.toString()
    }

    fun md5(raw: String) = MessageDigest.getInstance("MD5").digest(raw.toByteArray()).toHex()

    fun sha1(raw: String) = MessageDigest.getInstance("SHA-1").digest(raw.toByteArray()).toHex()

    fun sha256(raw: String) = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray()).toHex()

}