package com.example.mylitertexperiment

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import com.example.mylitertexperiment.model.BaseModelExecutor
import com.example.mylitertexperiment.model.DetectedObject
import com.example.mylitertexperiment.repository.ModelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test

private class FakeClassificationExecutor : BaseModelExecutor<Bitmap, List<DetectedObject>>() {
    override val outputFlow: MutableSharedFlow<List<DetectedObject>> = MutableSharedFlow()
    override suspend fun runAsync(input: Bitmap) {
        outputFlow.emit(listOf(DetectedObject("cat", 1f, Rect())))
    }
}

private class FakeStyleExecutor : BaseModelExecutor<Bitmap, Bitmap>() {
    override val outputFlow: MutableSharedFlow<Bitmap> = MutableSharedFlow()
    override suspend fun runAsync(input: Bitmap) {
        outputFlow.emit(input)
    }
}

/** Simple unit test ensuring ModelRepository forwards frames to all executors. */
class ModelRepositoryTest {
    @Test
    fun emitsResultsFromAllExecutors() = runTest {
        val classification = FakeClassificationExecutor()
        val style = FakeStyleExecutor()
        val repo = ModelRepository(Application(), classification, style, style)

        val frame = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        repo.cameraFrameFlow.emit(frame)

        assertNotNull(classification.outputFlow.first())
        assertNotNull(style.outputFlow.first())
    }
}

