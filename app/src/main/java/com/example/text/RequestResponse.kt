package com.example.text

import android.graphics.Bitmap

data class RequestResponse(
    val isRequest : Boolean,
    val text: String,
    val bitmap : Bitmap? = null

)
