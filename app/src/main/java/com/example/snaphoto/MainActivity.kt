package com.example.snaphoto

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.snaphoto.camera.CameraCapture
import com.example.snaphoto.ui.theme.SnaphotoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.*
import okio.IOException

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SnaphotoTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainContent(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val emptyImageUri = Uri.parse("file://dev/null")
    var imageUri by remember { mutableStateOf(emptyImageUri) }
    if (imageUri != emptyImageUri) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(imageUri),
                contentDescription = "Captured image"
            )
            Button(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    imageUri = emptyImageUri
                }
            ) {
                Text("Remove image")
            }
        }
    } else {
        CameraCapture(
            modifier = modifier,
            onImageFile = { file ->
                imageUri = file.toUri()

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://192.168.1.25:8070/photo/kek")
                    .put(RequestBody.create(MediaType.parse("application/json"), file))
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            for ((name, value) in response.headers().toMultimap()) {
                                println("$name: $value")
                            }

                            println(response.body().toString())
                        }
                    }
                })

                Log.d("CAMERA!!!!", "size: ${file.length()/1024}, $imageUri")
            }
        )
    }
}