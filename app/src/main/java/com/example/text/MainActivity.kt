package com.example.text


import androidx.compose.ui.graphics.drawscope.drawIntoCanvas


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Highlights
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.text.ui.theme.TextTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.internal.mlkit_vision_text_common.zzsg
import com.google.mlkit.vision.text.Text
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
var isSheetOpen by mutableStateOf(false)
var textContent by mutableStateOf("")
var imageBitmap by mutableStateOf<Bitmap?>(null)
var bounding by mutableStateOf<Rect?>(null)
var detectText by mutableStateOf("")
//var detectedTextResult by mutableStateOf<Text>()
val textResult : Text = Text("", listOf<Any>(1))
var detectedTextResult by mutableStateOf<Text>(textResult)
val apiKey = "AIzaSyCnxU3Xrk1oPI3ZF-Nl260tXRiRwvDFa9w"

class MainActivity : ComponentActivity() {

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        // ... implementation for handling cropping actions
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(16,9).getIntent(this@MainActivity)
        }
        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }



    private val bitmapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            val bitmap: Bitmap? = selectedImageUri?.let { uri ->
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            if (bitmap != null) {
                // Handle the selected bitmap (e.g., display it)
                //handleSelectedBitmap(bitmap)
                imageBitmap = bitmap
            }
        } else {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            val cropImage = registerForActivityResult(CropImageContract()) { result ->
                if (result.isSuccessful) {
                    // Use the returned uri.
                    val uriContent = result.uriContent
                    val inputStream = contentResolver.openInputStream(uriContent!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    imageBitmap = bitmap
                    //detectFace(bitmap)
                    detectText(bitmap)

                    //val uriFilePath = result.getUriFilePath(context) // optional usage
                } else {
                    // An error occurred.
                    val exception = result.error
                }
            }

        var cropImageContractOptions = CropImageContractOptions(uri = null,CropImageOptions(imageSourceIncludeGallery = true,imageSourceIncludeCamera= false));

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts){
            it?.let {uri ->
                Log.d("getURi" , "uri : ${uri.toString()}")
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                imageBitmap = bitmap
                //detectFace(bitmap)
                detectText(bitmap)
            }
        }


        setContent {
            //var textContent by remember { mutableStateOf("") }
            TextTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var sheetState = rememberModalBottomSheetState()
//                    var isSheetOpen by rememberSaveable {
//                        mutableStateOf(false)
//                    }

                   // Greeting("Android")
                   // ImageViewWithBitmap(imageBitmap)


                    //HighlightText(imageBitmap)
                    ShowRequestResponseList()
                    // DetectTextAndHighLight()



                    Box() {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {


//                            Button(
//                                onClick = {
//
//
//                                    cropImage.launch(cropImageContractOptions)
//
//                                    // cropActivityResultLauncher.launch(null)
//
////                                bitmapLauncher.launch(
////                                    Intent(
////                                        Intent.ACTION_PICK,
////                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
////                                    )
////                                )
//                                    isSheetOpen = true
//
//
////                                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
////                                startActivityForResult(galleryIntent, 125)
//
//
////                                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////                                if (intent.resolveActivity(packageManager) != null) {
////                                    startActivityForResult(intent, 123)
////                                } else {
//////                                    Toast.makeText(
//////                                        MainActivity.this,
//////                                        "oops went worngin camera",
//////                                        Toast.LENGTH_SHORT
//////                                    ).show()
////                                }
//                                }, modifier = Modifier
//                                    .height(50.dp)
//                                    .width(100.dp)
//                            ) {
//                                Text(text = "Click Me!")
//                            }
//
//
                        }
                    }


                    if (isSheetOpen) {
                        ModalBottomSheet(
                            sheetState = sheetState,
                            onDismissRequest = {
                                isSheetOpen = false
                                isTextDetected = true


                            }) {
                            //DetectTextAndHighLight()
                            Text(text = textContent)

                        }
                    }
                    EditableTextDemo(cropImage,cropImageContractOptions)
                    //BoundingBoxImage(imageBitmap,bounding)

                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == RESULT_OK) {
            val extras = data?.extras
            val bitmap = extras?.get("data") as? Bitmap
            if (bitmap != null) {
                detectFace(bitmap)
//                val rotationDegree = getRotationDegree(data)
//                val rotatedBitmap = rotateBitmap(bitmap, rotationDegree)
//                Log.d("MainActivity", "Rotationdegree: $rotationDegree rotateBitmap : $rotatedBitmap")

            }
        }

        if (requestCode == 125 && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

//            val cropImage = registerForActivityResult(CropImageContract()) { result ->
//                if (result.isSuccessful) {
//                    // Use the returned uri.
//                    val uriContent = result.uriContent
//                    //val uriFilePath = result.getUriFilePath(context) // optional usage
//                } else {
//                    // An error occurred.
//                    val exception = result.error
//                }
//            }
//            cropImage.launch(
//                CropImageContractOptions(uri = selectedImageUri, cropImageOptions = CropImageOptions(
//                    guidelines = CropImageView.Guidelines.ON
//                ))
//            )


            val bitmap: Bitmap? = selectedImageUri?.let { uri ->
                // Use content resolver to open input stream and decode bitmap
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            if (bitmap != null) {
                detectFace(bitmap)
                detectText(bitmap)
            }


        }

    }
     private fun detectFace(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        val result = recognizer.process(image)
            .addOnSuccessListener { result ->
                // Task completed successfully
                val r = result
                val resultText = result.text
                detectText = resultText

              //  HighlightTextWithBoundingBox(result)

//                CoroutineScope(Dispatchers.IO).launch {
//                    textContent = geminiApi(resultText)
//                    // Use the fetched data on the UI thread (with Dispatchers.Main)
//                    withContext(Dispatchers.Main) {
//                        // Update UI with data
//                    }
//                }

               // textContent = resultText


                //Toast.makeText(this , "detect sucessfully"+resultText , Toast.LENGTH_SHORT).show()
                //Log.d("MainActivity", "Detected Text: $resultText")

                // ...

                for (block in result.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
//                    val blockCornerPoints = block.cornerPoints?.get(0)?.x
                    val blockFrame = block.boundingBox
                    bounding = blockFrame
//                    for (line in block.lines) {
//                        val lineText = line.text
//                        val lineCornerPoints = line.cornerPoints
//                        val lineFrame = line.boundingBox
//                        for (element in line.elements) {
//                            val elementText = element.text
//                            val elementCornerPoints = element.cornerPoints
//                            val elementFrame = element.boundingBox
//                        }
//                    }

                    Log.d("block", "blockText : $blockText blockCornerPoints : $blockCornerPoints blockFrame : $blockFrame")
                }



            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(this , "detect error" , Toast.LENGTH_SHORT).show()
                // ...
            }


    }
}
@Composable
fun ImageViewWithBitmap(bitmap: Bitmap?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
       // val bitmap = remember { imageBitmap }


        // Use the bitmap variable in your UI
        // For example, display the image using Image composable
        if (bitmap != null) {
            var Newbitmap = imageBitmap!!.asImageBitmap()
            Image(painter = BitmapPainter(Newbitmap), contentDescription = "Image",
                modifier = Modifier.fillMaxSize()
            )
        }else {
            Text(text = "no image")
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun BoundingBoxImage(
    bitmap: Bitmap?,
    boundingBox: Rect?,
    modifier: Modifier = Modifier
) {
    if(bitmap!=null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    if (boundingBox != null) {
                        drawRect(Color.Transparent)
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(boundingBox.top.toFloat(), boundingBox.left.toFloat()),
                            size = Size(
                                boundingBox
                                    .width()
                                    .toFloat(),
                                boundingBox
                                    .height()
                                    .toFloat()
                            )
                        )

                    }
                }
                ,
        )
    }
}

suspend fun geminiApi(prompt : String) : String{
    val generativeModel = GenerativeModel(
        // For text-only input, use the gemini-pro model
        modelName = "gemini-pro",
        // Access your API key as a Build Configuration variable (see "Set up your API key" above)
        apiKey = apiKey
    )

    val prompt = prompt
    val response = generativeModel.generateContent(prompt)
    print(response.text)
    val ans = response.text
    Log.d("geminii" , "response : $ans")

    return ans!!
}

@Composable
fun EditableTextDemo(cropImage:ActivityResultLauncher<CropImageContractOptions>,cropImageContractOptions: CropImageContractOptions) {
    // Remember the current text value
    val textState = remember { mutableStateOf("") }
    var showSendIcon by remember {
        mutableStateOf(true)
    }
    if(isTextDetected) {
        textState.value = detectText
    }
Box( ) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,

        ) {

//        Text(
//            text = "Input",
//            style = MaterialTheme.typography.bodyLarge
//        )
        // TextField for editing text
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)) {
//            TextField(
//                value = textState.value,
//                onValueChange = { newValue ->
//                    // Update the textState value when text changes
//                    textState.value = newValue
//                },
//                label = { MaterialTheme.typography.bodyLarge },
//                modifier = Modifier
//                    .padding(start = 10.dp)
//                    .fillMaxWidth(0.9f),
//
//                shape = MaterialTheme.shapes.extraLarge // Use medium rounded corners
//
//            )

            OutlinedTextField(
                value = textState.value, // Your text value here
                onValueChange = { newValue ->
//                    // Update the textState value when text changes
                  textState.value = newValue
                    isTextDetected = false
               },
                modifier = Modifier
                    .width(300.dp)
                    .padding(start = 10.dp)
                    .padding(top = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(8.dp), maxLines = 5,
                trailingIcon = {
                    if(textState.value.equals("")) {
                        Icon(Icons.Rounded.AddCircle, contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    cropImage.launch(cropImageContractOptions)
                        })
                    }
                }// Rounded corner shape
            )


//            Icon(
//                Icons.Rounded.Send,
//                contentDescription = "Send",
//                tint = Color.White, // Set the color of the icon
//                modifier = Modifier
//                    .size(60.dp)
//                    .padding(start = 10.dp,top = 22.dp)
//                    .fillMaxWidth(.1f)
//
//            )

//            Button(colors = buttonColors(Color.Transparent),
//                modifier = Modifier
//                    .width(70.dp)
//                    .height(70.dp)
//                    .padding(start = 10.dp, top = 22.dp) ,onClick = {
//                Log.d("button click" , "button")
//                CoroutineScope(Dispatchers.IO).launch {
//                    Log.d("string" , "${textState.value}")
//                    textContent = geminiApi(textState.value)
//                    // Use the fetched data on the UI thread (with Dispatchers.Main)
//                    withContext(Dispatchers.Main) {
//                        // Update UI with data
//                    }
//                }
//            }) {
                Icon(
                    imageVector = if (showSendIcon) Icons.Rounded.Send else Icons.Rounded.Close,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 10.dp, top = 22.dp)
                        .size(40.dp)
                        .clickable {
                            if (showSendIcon) {
                                Log.d("button click", "button")
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        showSendIcon = false
                                        Log.d("string", "${textState.value}")
                                        if(imageBitmap!=null) {
                                            dataList.add(
                                                RequestResponse(
                                                    true, textState.value,
                                                    imageBitmap
                                                )
                                            )
                                        } else{
                                            dataList.add(
                                                RequestResponse(
                                                    true, textState.value)
                                                )
                                        }
                                        textContent = geminiApi(textState.value)
                                        dataList.add(RequestResponse(false, textContent))
                                    } catch (e: Exception) {
                                        Log.e("gemmini error", "$e")
                                    }

                                    // Use the fetched data on the UI thread (with Dispatchers.Main)
                                    withContext(Dispatchers.Main) {
                                        // Update UI with data
                                        showSendIcon = true
//                                        dataList.add(RequestResponse(true,textState.value))
//                                        dataList.add(RequestResponse(false, textContent))
                                    }
                                }
                            }

                        }
                )
            }

        }


        // Display the current text value

    }
}
//}

@Composable
fun HighlightText(imageBitmap : Bitmap?) {


    if (imageBitmap!=null) {
        var Newbitmap = imageBitmap!!.asImageBitmap()
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawImage(Newbitmap) // Draw the original image

            //for (block in result.textBlocks) {
                val boundingBox = bounding
                val left = boundingBox?.left?.toFloat() ?: 0f
                val top = boundingBox?.top?.toFloat() ?: 0f
                val right = boundingBox?.right?.toFloat() ?: 0f
                val bottom = boundingBox?.bottom?.toFloat() ?: 0f


                val topLeft = Offset(
                    x = boundingBox?.left?.toFloat() ?: 0f,
                    y = boundingBox?.top?.toFloat() ?: 0f
                )

                val size = Size(width = right - left, height = bottom - top)

                drawRect(
                    color = Color.Yellow, // Change color as desired
                    alpha = 0.5f, // Set transparency
                    topLeft = topLeft,
                    size = size
                )
            //}
        }
    } else {
        //Image(bitmap = image, contentDescription = "Image with Text") // Display image without highlighting
        Text("no image found")
    }
}

@Composable
fun HighlightTextWithBoundingBox(result : Text) {

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {

            //for (block in result.textBlocks) {
            val boundingBox = bounding
            val left = boundingBox?.left?.toFloat() ?: 0f
            val top = boundingBox?.top?.toFloat() ?: 0f
            val right = boundingBox?.right?.toFloat() ?: 0f
            val bottom = boundingBox?.bottom?.toFloat() ?: 0f


            val topLeft = Offset(
                x = boundingBox?.left?.toFloat() ?: 0f,
                y = boundingBox?.top?.toFloat() ?: 0f
            )


            val size = Size(width = right - left, height = bottom - top)

            drawRect(
                color = Color.Yellow, // Change color as desired
                alpha = 0.5f, // Set transparency
                topLeft = topLeft,
                size = size
            )
            //}
        }

}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextTheme {
        Greeting("Android")
       // EditableTextDemo(isOpensheet)
    }
}