package com.example.text


import android.annotation.SuppressLint
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
            TextTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var sheetState = rememberModalBottomSheetState()
                    var scaffoledSheetState = rememberBottomSheetScaffoldState()
//                    var isSheetOpen by rememberSaveable {
//                        mutableStateOf(false)
//                    }
                    var scope = rememberCoroutineScope()
                    Column(modifier = Modifier.fillMaxSize()) {
//                        Button(onClick = { isSheetOpen = true
//                                scope.launch {
//                                    drawerState.open()
//                                }
//                            Log.d("Button clicked","icon button")
//                        }) {
//
//                        }
                        Scaffold(modifier = Modifier.fillMaxWidth(), topBar = { TopAppBarUi() }) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                ShowRequestResponseList(it)
                                EditableTextDemo(cropImage, cropImageContractOptions)
                            }

                        }


                    }

//                if(isSheetOpen){
//                    BottomSheetScaffold(sheetContent = {
//                        Text("LOREMOAJOFJOASHDFIOJAOSJFALSDF" +
//                                    "ASHDJFLKASDFJLASDJFASJDFLKAPSDLFJA;SDLKFJ;ASDFLA;SDFKKLFKLJAHSFGAJSDFH" +
//                                    "IASJKLDF;LAKSJDF;LAKSD;FJA;LKSDLFKASDF" +
//                                    "ASODJF;LKASJFKHASDFK;ASDKLFASKJLDFHKASHFKASHFASHFNASF" +
//                                    "IASJDFJIAOSJHFLKASDLKFHASLKDJFHSADFJFHLASDFHLKASDF" +
//                                    "AISDHFLKASDLFJSA;DLFKAS;LF;ASDKDF;ASKFKASHFJ" +
//                                    "ASHLFKAJSKLJASFKJLASDJKLJKLASDKJLSDAFKJLADSJKLFDSAJLKFSAD" +
//                                    "AKHSDHLASLJKDLKJASDJLKASLKJ")
//                    }, scaffoldState = scaffoledSheetState ) {
//
//                    }
//                }
                    if (isSheetOpen) {
                        ModalBottomSheet(modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize(),
                            sheetState = sheetState,
                            onDismissRequest = {
                                isSheetOpen = false
                                //isTextDetected = true


                            }) {
                            Column(
                                modifier = Modifier
                                    .size(500.dp)
                                    .padding(0.dp)
                            ) {
                                DetectTextAndHighLight()
                            }
                            //  Text(text = detectText)


//                            Text("LOREMOAJOFJOASHDFIOJAOSJFALSDF" +
//                                    "ASHDJFLKASDFJLASDJFASJDFLKAPSDLFJA;SDLKFJ;ASDFLA;SDFKKLFKLJAHSFGAJSDFH" +
//                                    "IASJKLDF;LAKSJDF;LAKSD;FJA;LKSDLFKASDF" +
//                                    "ASODJF;LKASJFKHASDFK;ASDKLFASKJLDFHKASHFKASHFASHFNASF" +
//                                    "IASJDFJIAOSJHFLKASDLKFHASLKDJFHSADFJFHLASDFHLKASDF" +
//                                    "AISDHFLKASDLFJSA;DLFKAS;LF;ASDKDF;ASKFKASHFJ" +
//                                    "ASHLFKAJSKLJASFKJLASDJKLJKLASDKJLSDAFKJLADSJKLFDSAJLKFSAD" +
//                                    "AKHSDHLASLJKDLKJASDJLKASLKJ")

                        }
                    }

                }
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
        modifier = Modifier.fillMaxSize()
        //,verticalArrangement = Arrangement.Bottom,

    ) {

//        Text(
//            text = "Input",
//            style = MaterialTheme.typography.bodyLarge
//        )
        // TextField for editing text
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
                shape = RoundedCornerShape(25.dp), maxLines = 5,
                trailingIcon = {
                    if (textState.value.equals("")) {
                        Icon(Icons.Rounded.AddCircle, contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    cropImage.launch(cropImageContractOptions)

                                })
                    }
                }
            )
                Icon(
                    imageVector = if (showSendIcon) Icons.Rounded.Send else Icons.Rounded.Close,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.surfaceTint,
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
                                        if (imageBitmap != null) {
                                            dataList.add(
                                                RequestResponse(
                                                    true, textState.value,
                                                    imageBitmap
                                                )
                                            )
                                        } else {
                                            dataList.add(
                                                RequestResponse(
                                                    true, textState.value
                                                )
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

    }
}
}