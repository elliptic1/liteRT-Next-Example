package com.example.mylitertexperiment

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.mylitertexperiment.viewmodel.MainViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import com.example.mylitertexperiment.ui.MainScreen
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current.applicationContext
            val lifecycleOwner = LocalLifecycleOwner.current
            val viewModelStoreOwner = LocalViewModelStoreOwner.current ?: this
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
            val viewModel = ViewModelProvider(viewModelStoreOwner, factory)[MainViewModel::class.java]
            MainScreen(viewModel, context, lifecycleOwner)
        }
    }
}