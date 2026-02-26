package com.example.ticketscanner

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder



class OobPayloadBuilder {
    fun createPayload(): String { //genererer text string med OOB informasjonen
        val text = "https://ultveit.no/"
        return text
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


