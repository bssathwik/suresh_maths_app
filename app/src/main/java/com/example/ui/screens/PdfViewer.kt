package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun LocalPdfViewer(pdfData: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pdfData) {
        withContext(Dispatchers.IO) {
            try {
                val file = if (pdfData.startsWith("local_pdf:")) {
                    File(pdfData.removePrefix("local_pdf:"))
                } else if (pdfData.startsWith("http")) {
                    val tempFile = File(context.cacheDir, "temp_viewer_${System.currentTimeMillis()}.pdf")
                    val connection = java.net.URL(pdfData).openConnection() as java.net.HttpURLConnection
                    connection.connect()
                    connection.inputStream.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } else {
                    val b64 = pdfData.substringAfter("base64,")
                    val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                    val tempFile = File(context.cacheDir, "temp_viewer.pdf")
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    tempFile
                }

                if (file.exists()) {
                    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    pdfRenderer = PdfRenderer(fileDescriptor)
                    pageCount = pdfRenderer?.pageCount ?: 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pdfRenderer?.close()
        }
    }

    if (isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Text("Loading PDF...", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().background(Color.Gray),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pageCount) { index ->
                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                
                LaunchedEffect(index) {
                    withContext(Dispatchers.IO) {
                        pdfRenderer?.let { renderer ->
                            val page = renderer.openPage(index)
                            // Render at 2x resolution for better quality
                            val w = page.width * 2
                            val h = page.height * 2
                            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            // White background
                            val canvas = android.graphics.Canvas(bmp)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            bitmap = bmp
                        }
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.White), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
