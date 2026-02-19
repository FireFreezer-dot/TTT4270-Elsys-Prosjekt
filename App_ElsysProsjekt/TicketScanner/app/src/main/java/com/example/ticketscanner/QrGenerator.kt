package com.example.ticketscanner

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder


class QrGenerator {


    fun getBitmapFromString(text: String): Bitmap?{
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(
                text,
                BarcodeFormat.QR_CODE,
                300,
                200)

            BarcodeEncoder().createBitmap((bitMatrix))
        } catch (ex: java.lang.Exception) {
            null
        }
    }

    //binding.emptyImage.setImageBitmap(getBitmapFromString(text: ))
}