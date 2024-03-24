package com.example.text

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


var isTextDetected by mutableStateOf(false)
fun detectText(bitmap: Bitmap){
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)



    val result = recognizer.process(image)
        .addOnSuccessListener { result ->
            // Task completed successfully
            detectedTextResult = result
            val resultText = result.text
            detectText = resultText
            isTextDetected = true
            isSheetOpen = true
        }
        .addOnFailureListener { e ->
        }
}

@Composable
fun DetectTextAndHighLight() {
    val textResult = detectedTextResult
    if (!textResult.text.equals("")) {
        if (imageBitmap != null) {
            var Newbitmap = imageBitmap!!.asImageBitmap()
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawImage(Newbitmap)



                for (block in textResult.textBlocks) {
                    for (line in block.lines) {
                        val lineFrame = line.boundingBox
                        val left = lineFrame?.left?.toFloat() ?: 0f
                        val top = lineFrame?.top?.toFloat() ?: 0f
                        val right = lineFrame?.right?.toFloat() ?: 0f
                        val bottom = lineFrame?.bottom?.toFloat() ?: 0f


                        val topLeft = Offset(
                            x = lineFrame?.left?.toFloat() ?: 0f,
                            y = lineFrame?.top?.toFloat() ?: 0f
                        )

                        val size = Size(width = right - left, height = bottom - top)
//
                        drawRect(
                            color = Color.Yellow, // Change color as desired
                            alpha = 0.5f, // Set transparency
                            topLeft = topLeft,
                            size = size

                        )


//                        drawRoundRect(
//                            color = Color.Yellow, // Change color as desired
//                            alpha = 0.5f, // Set transparency
//                            topLeft = topLeft,
//                            size = size
//                        )
                    }

//                        ClickableRectangle(
//                            topLeft = topLeft,
//                            size = size,
//                            onClick = {
//                                // Handle click event here (e.g., log text, navigate)
//                                Log.d("RectangleClick", "Clicked on Text Block: ${line.text}")
//                            }
//                        )

                }

                    //Log.d("block", "blockText : $blockText blockCornerPoints : $blockCornerPoints blockFrame : $blockFrame")
                }


        }

    }

}





@Composable
fun ClickableRectangle(
    modifier: Modifier = Modifier,
    color: Color = Color.Yellow.copy(alpha = 0.5f), // Highlight color
    topLeft: Offset,
    size: Size,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(height = 1.dp, width = 1.dp)
            .clickable(onClick = onClick)
//            .background(color = Color.Red)// Transparent background
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(50.dp)
            //  .size(height = size.height.dp , width = size.width.dp)
        ) {

            drawRoundRect(
                // Draw rounded rectangle highlight
                color = color,
                alpha = 0.5f, // Set transparency
                topLeft = topLeft,
                size = size,
                //cornerRadius = 5.dp // Rounded corner radius
            )
        }
    }
}




