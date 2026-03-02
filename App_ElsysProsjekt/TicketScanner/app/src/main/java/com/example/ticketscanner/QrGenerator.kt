package com.example.ticketscanner

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID



class OobPayloadBuilder {

    private fun generateSessionKey(): String { //lager en randomised sessionkey string
        val random = SecureRandom() //SecureRandom for kryptering
        val bytes = ByteArray(16) //16 bytes array
        random.nextBytes(bytes) //fyller array med random bytes
        return bytes.joinToString("") { "%02x".format(it) } //gjør 128bits om til 32 hex tegn som kan konverteres til string
    }

    private fun generateTicketID(): String { //lager randomised ticket med logikk fra Uuid imports
        return "TICKET-" + UUID.randomUUID().toString().take(8)
    }


    fun buildPayload(): String {

        val customerName = "Ola Nordmann"
        val amountCostumers = 1
        val sessionKey = generateSessionKey()
        val ticketID = generateTicketID()

        val json = JSONObject().apply {
            put("ticketID", ticketID)
            put("amountCustomers", amountCostumers)
            put("customerName", customerName)
            put("sessionKey", sessionKey)
        }

        return json.toString()
    }
}


class QrGenerator {
    fun getBitmapFromString(text: String): Bitmap?{ //lager en string om til en Bitmap
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(
                text,
                BarcodeFormat.QR_CODE,
                300,
                300)

            BarcodeEncoder().createBitmap((bitMatrix))
        } catch (ex: java.lang.Exception) { //dersom noe feil skjer.
            null
        }
    }
}


