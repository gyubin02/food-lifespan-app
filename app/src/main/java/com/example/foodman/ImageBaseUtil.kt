package com.example.foodman

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageBase64Util {

    /**
     * 이미지 Uri를 받아 base64 문자열로 변환 (jpg)
     * @param context Context
     * @param imageUri 이미지 파일 Uri
     * @param withPrefix true면 "data:image/jpeg;base64," prefix 붙임
     * @return base64 문자열, 실패시 null
     */
    fun uriToBase64(context: Context, imageUri: Uri, withPrefix: Boolean = false): String? {
        val bitmap = getBitmapFromUri(context, imageUri) ?: return null
        val jpegBytes = bitmapToJpegByteArray(bitmap)
        val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        return if (withPrefix) "data:image/jpeg;base64,$base64" else base64
    }

    fun base64FromAssetJpg(context: Context, fileName: String, withPrefix: Boolean = false): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val bytes = inputStream.readBytes()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            Log.d("GeminiTest_imageutil", "이미지변환성공")
            if (withPrefix) "data:image/jpeg;base64,$base64" else base64
        } catch (e: Exception) {
            Log.d("GeminiTest_imageutil", "이미지변환실패")
            e.printStackTrace()
            null
        }
    }
    // --- 내부 헬퍼 함수 ---

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun bitmapToJpegByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}