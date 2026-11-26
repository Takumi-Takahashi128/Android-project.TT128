package com.example.contact.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.example.contactsapp.model.Contact
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun Contact.toVCard(): String = buildString {
    appendLine("BEGIN:VCARD")
    appendLine("VERSION:3.0")
    appendLine("FN:$name")
    if (company.isNotBlank() || title.isNotBlank()) {
        appendLine("ORG:${company.ifBlank { title }}")
    }
    if (title.isNotBlank()) {
        appendLine("TITLE:$title")
    }
    if (phone.isNotBlank()) {
        appendLine("TEL;TYPE=CELL:$phone")
    }
    if (email.isNotBlank()) {
        appendLine("EMAIL:$email")
    }
    if (address.isNotBlank()) {
        appendLine("ADR:$address")
    }
    if (notes.isNotBlank()) {
        appendLine("NOTE:$notes")
    }
    append("END:VCARD")
}

fun generateQrCodeBitmap(
    content: String,
    size: Int = 512,
    foregroundColor: Int = Color(0xFF004D40).toArgb(),
    backgroundColor: Int = Color.White.toArgb()
): ImageBitmap? {
    if (content.isBlank()) return null
    return try {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
            }
        }
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

fun loadImageBitmapFromUri(context: Context, uriString: String?): ImageBitmap? {
    if (uriString.isNullOrBlank()) return null
    return try {
        val uri = Uri.parse(uriString)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}