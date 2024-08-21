package com.example.barcodeqrscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.barcodeqrscanner.ui.theme.BarcodeqrscannerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import okhttp3.*
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var resultadoEscanner by remember { mutableStateOf("") }
            val scanLauncher = rememberLauncherForActivityResult(contract = ScanContract(),
                onResult = { result -> resultadoEscanner = result.contents?: "Sin Resultados"
                    if (resultadoEscanner.isNotEmpty()) {
                        enviarCodigoAlServidor(resultadoEscanner)
                    }
                }
            )
            BarcodeqrscannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                         verticalArrangement = Arrangement.Bottom,
                         horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "resultado: ${ resultadoEscanner }")
                        Button(
                            onClick ={
                            val scanOptions = ScanOptions()
                                scanOptions.setBeepEnabled(true)
                                scanOptions.setCaptureActivity(CaptureActivityPorTrait::class.java)
                                scanOptions.setOrientationLocked(false)
                                scanLauncher.launch(scanOptions)
                            }
                        ) {
                            Text(text = "Escanear")
                        }
                    }
                }
            }
        }
    }
    private fun enviarCodigoAlServidor(resultadoEscanner: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("scannedData", resultadoEscanner)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.21:3000/receive-scan") // Asegúrate de usar la IP correcta
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HttpError", "Error en la solicitud", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("HttpError", "Código de respuesta no exitoso: ${response.code}")
                }
                Log.d("HttpResponse", "Respuesta: ${response.body?.string()}")
            }
        })
    }
}

