package com.mcc.g22

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.mcc.g22", appContext.packageName)
    }

    @Test
    fun uploadBitmap() {
        val bmpToUpload = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val attachmentsManager = AttachmentsManager("test")
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.uploadFile(bmpToUpload,
            {result = true; signal.countDown()},
            {result = false; signal.countDown()},
            "test_bitmap.jpg")
        signal.await()
        assertTrue(result)

        Thread.sleep(1000) // Give some time to update realtime database

        testRealtimeDatabase(attachmentsManager)
    }

    private fun testRealtimeDatabase(attachmentsManager: AttachmentsManager) {
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.listAllAttachments({
            result = it.contains("test_bitmap.jpg")

            it.forEach { element -> println(element) }

            signal.countDown()
        }, {
            result = false
            signal.countDown()
        })
        signal.await()
        assertTrue(result)
    }

    @Test
    fun downloadImage() {
        /*val signal = CountDownLatch(1)
        var result = false

        val attachmentsManager = AttachmentsManager("test")
        attachmentsManager.downloadFile("test_image.jpg",
            {result = it.exists(); signal.countDown()},
            {result = false; signal.countDown()})

        signal.await()
        assertTrue(result)*/
    }
}
