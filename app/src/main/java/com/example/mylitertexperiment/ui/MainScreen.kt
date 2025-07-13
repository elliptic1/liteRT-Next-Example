package com.example.mylitertexperiment.ui

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.example.mylitertexperiment.camera.CameraXManager
import com.example.mylitertexperiment.model.DetectedObject
import com.example.mylitertexperiment.viewmodel.MainViewModel
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(viewModel: MainViewModel, context: Context, lifecycleOwner: LifecycleOwner) {
    val ctx = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorFlow.collectAsState(initial = "")

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        hasCameraPermission = granted
    }

    if (hasCameraPermission) {
        LaunchedEffect(uiState) {
            Log.d("ModelInput", "Input to style transfer: ${uiState.originalFrame?.width}x${uiState.originalFrame?.height}")
            Log.d("ModelInput", "Input to classification: ${uiState.originalFrame?.width}x${uiState.originalFrame?.height}")
            Log.d("ModelInput", "Input to OCR: ${uiState.originalFrame?.width}x${uiState.originalFrame?.height}")
            Log.d("ModelInput", "Input to speech-to-text: [not shown, handled via audio]")
            Log.d("ModelOutput", "styledFrame: ${uiState.styledFrame != null}")
            Log.d("ModelOutput", "detectedObjects: ${uiState.detectedObjects}")
            Log.d("ModelOutput", "ocrText: ${uiState.ocrText}")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(context, lifecycleOwner)
            DrawStyleFrameOverlay(uiState.styledFrame)
            DrawBoxOverlay(uiState.detectedObjects)
            DrawOcrOverlay(uiState.ocrText)
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Camera permission required")
        }
    }
}

@Composable
fun CameraPreview(context: Context, lifecycleOwner: LifecycleOwner) {
    val previewView = remember { PreviewView(context) }
    val repository = remember { com.example.mylitertexperiment.repository.ModelRepository(context) }
    LaunchedEffect(Unit) {
        val cameraManager = com.example.mylitertexperiment.camera.CameraXManager(
            context,
            lifecycleOwner,
            repository.cameraFrameFlow
        )
        cameraManager.startCamera(previewView)
        // Optionally collect cameraManager.cameraFrameFlow here if needed
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
fun DrawBoxOverlay(objects: List<DetectedObject>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        objects.forEach { obj ->
            val rect = obj.boundingBox
            // Map Android Rect to Compose coordinates (assume preview is full size for now)
            drawRect(
                color = Color.Red,
                topLeft = androidx.compose.ui.geometry.Offset(rect.left.toFloat(), rect.top.toFloat()),
                size = androidx.compose.ui.geometry.Size(
                    (rect.right - rect.left).toFloat(),
                    (rect.bottom - rect.top).toFloat()
                ),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
    // Draw labels above boxes using Box and offset instead of Popup
    objects.forEach { obj ->
        Box(
            modifier = Modifier
                .offset { IntOffset(obj.boundingBox.left, obj.boundingBox.top - 30) }
        ) {
            Text(
                text = "${obj.label} ${(obj.confidence * 100).toInt()}%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DrawOcrOverlay(text: String) {
    if (text.isNotBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Text(
                text = text,
                color = Color.Yellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun DrawStyleFrameOverlay(styledFrame: android.graphics.Bitmap?) {
    if (styledFrame != null) {
        Image(
            bitmap = styledFrame.asImageBitmap(),
            contentDescription = "Styled Frame",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.5f), // semi-transparent overlay
            contentScale = ContentScale.Crop
        )
    }
}