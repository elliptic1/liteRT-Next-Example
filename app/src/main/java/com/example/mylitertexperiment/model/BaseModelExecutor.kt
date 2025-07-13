package com.example.mylitertexperiment.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseModelExecutor<in I, out O> {
    // TODO: Hold reference to LiteRT Next CompiledModel
    abstract val outputFlow: SharedFlow<O>
    protected val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow
    abstract suspend fun runAsync(input: I)
    // TODO: Implement model loading, input/output conversion
}