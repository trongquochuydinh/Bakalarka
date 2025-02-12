package com.example.testing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testing.ui.theme.TestingTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.SurfaceView
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CameraAlt

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestCameraPermission()

        setContent {
            TestingTheme {
                CameraScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                title = { Text("") },
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        IconButton(
                            onClick = { /* Handle button action */ },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary),
                content = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { /* Handle button action */ },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Capture", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as ComponentActivity, cameraSelector, preview
                    )
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    TestingTheme {
        CameraScreen()
    }
}