package com.dinsoft.notes.data

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private val key = SecretKeySpec("Kahilapan@2026!!".toByteArray(), "AES")
    private val cipher = Cipher.getInstance("AES")

    fun enc(text: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.NO_WRAP)
    }

    fun dec(text: String): String {
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(Base64.decode(text, Base64.NO_WRAP)))
    }
}