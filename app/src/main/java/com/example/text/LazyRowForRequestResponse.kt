package com.example.text

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


var dataList = mutableStateListOf<RequestResponse>(RequestResponse(true,"hi i am gemmini a genAi")) // Replace with your actual data source
//var updateList mutable
//val data = listOf(
//    RequestResponse(true, "hi gemini how are you"),
//    RequestResponse(false, "i am good whats about you"),
//    RequestResponse(true, "i am also fine")
//)


//@Composable
//fun ShowRequestResponseList() {
//    Column(modifier = Modifier.fillMaxSize()) {
////        TextField(
////            value = userInput,
////            onValueChange = { userInput = it },
////            label = { Text("Enter your request:") },
////            modifier = Modifier.fillMaxWidth().padding(16.dp)
////        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Button(onClick = { /* Add user input to dataList */ }) {
//            Text("Submit")
//        }
//        if (currentItem != null) {
//            RequestResponseItem(request = currentItem!!.request, response = currentItem!!.response)
//        }
//    }
//}


//@Composable
//fun ShowRequestResponseList() {
//    LazyColumn(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        items(dataList.size) { item ->
//            RequestResponseItem(text = dataList.get(item).text, isRequest = dataList.get(item).isRequest)
//        }
//    }
//}

@Composable
fun ShowRequestResponseList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(dataList) { item ->  // Use item.text as a unique key
            RequestResponseItem(text = item.text, isRequest = item.isRequest , bitmap = item.bitmap)
        }
    }
}
@Composable
fun RequestResponseItem(text: String, isRequest: Boolean, bitmap: Bitmap? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        if(isRequest){
            Text(fontWeight = FontWeight.Bold,
                text = "You",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            if(bitmap!=null){
                Image(bitmap = bitmap!!.asImageBitmap(),contentDescription = null,
                    modifier = Modifier.width(200.dp).height(100.dp))
            }
            Text(text = text)
        } else{
           // Spacer(modifier = Modifier.height(8.dp))
            Text(fontWeight = FontWeight.Bold,
                text = "Gen Ai",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(text = text)
        }

    }
}
