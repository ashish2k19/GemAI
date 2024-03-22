package com.example.text

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context


class PickImageContract : ActivityResultContract<Unit, Bitmap?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        TODO("Not yet implemented")
    }

//    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
//        if (resultCode == Activity.RESULT_OK && intent != null) {
//            val selectedImageUri = intent.data
//            val contentResolver =
//            return selectedImageUri?.let { uri ->
//                contentResolver.openInputStream(uri)?.use { inputStream ->
//                    BitmapFactory.decodeStream(inputStream)
//                }
//            }
//        }
        //return null
    //}
}
