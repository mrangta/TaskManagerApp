package com.mcc.g22

import android.util.Log
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.time.Instant
import java.util.*
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
    fun createTaskSimple() {
        val t = Task.createTask("projectId", "Desc", Date.from(Instant.ofEpochMilli(100)))
        assertEquals(t.projectId, "projectId")
        assertEquals(t.description, "Desc")
        assertEquals(t.deadline, Date.from(Instant.ofEpochMilli(100)))
        assertEquals(t.status, Task.TaskStatus.PENDING)

        val u = User("test user")
        t.assignUser(u)
        assertEquals(t.status, Task.TaskStatus.ON_GOING)
        assertEquals(t.getAssignedUsers().size, 1)
        t.removeUser(u)
        assertEquals(t.status, Task.TaskStatus.PENDING)
        assertEquals(t.getAssignedUsers().size, 0)
    }

    @Test
    fun convertImageToTask() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val countDownLatch = CountDownLatch(1)
        var result = false
        val t = Task.createTask(appContext, "projectId",
            File("/storage/emulated/0/Download/wvx1u36r3xn21.jpg"),
            {
                Log.e("MCC", "Recognized text: " + it.description)
                result = it.description.contains("hey") &&
                        it.description.contains("don't") &&
                        it.description.contains("give") &&
                        it.description.contains("up") &&
                        it.description.contains("you") &&
                        it.description.contains("already") &&
                        it.description.contains("made") &&
                        it.description.contains("it") &&
                        it.description.contains("this") &&
                        it.description.contains("far")
                countDownLatch.countDown()
            }, {
                result = false
                countDownLatch.countDown()
            })
        countDownLatch.await()
        assertTrue(result)
    }

    fun uploadBitmap() {
        val bmpToUpload = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val testBitmapName = "test_bitmap.jpg"
        val attachmentsManager = AttachmentsManager("test")
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.uploadFile(bmpToUpload,
            {result = true; signal.countDown()},
            {result = false; signal.countDown()},
            testBitmapName)
        signal.await()
        assertTrue(result)

        Thread.sleep(1000) // Give some time to update realtime database

        testRealtimeDatabase(attachmentsManager, testBitmapName)
        testDownloadingInSize(attachmentsManager, testBitmapName)
    }

    private fun testRealtimeDatabase(attachmentsManager: AttachmentsManager, bmpName: String) {
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.listAllAttachments({
            result = it.contains(bmpName)

            it.forEach { element -> println(element) }

            signal.countDown()
        }, {
            result = false
            signal.countDown()
        })
        signal.await()
        assertTrue(result)
    }

    private fun testDownloadingInSize(attachmentsManager: AttachmentsManager, bmpName: String) {
        val signal = CountDownLatch(1)
        var result = false

        attachmentsManager.downloadFile(bmpName,
            {result = it.exists(); signal.countDown()},
            {result = false; signal.countDown()},
            imageSize = AttachmentsManager.ImageSize.LOW)

        signal.await()
        assertTrue(result)
    }

    @Test
    fun uploadHighResBitmap() {
        val bmpToUpload = Bitmap.createBitmap(1600, 1600, Bitmap.Config.ARGB_8888)
        val testBitmapName = "test_bitmap_high.jpg"
        val attachmentsManager = AttachmentsManager("test")
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.uploadFile(bmpToUpload,
            {result = true; signal.countDown()},
            {result = false; signal.countDown()},
            testBitmapName,
            imageSize = AttachmentsManager.ImageSize.HIGH)
        signal.await()
        assertTrue(result)
    }

    @Test
    fun uploadLowResBitmap() {
        val bmpToUpload = Bitmap.createBitmap(1600, 1600, Bitmap.Config.ARGB_8888)
        val testBitmapName = "test_bitmap_low.jpg"
        val attachmentsManager = AttachmentsManager("test")
        val signal = CountDownLatch(1)
        var result = false
        attachmentsManager.uploadFile(bmpToUpload,
            {result = true; signal.countDown()},
            {result = false; signal.countDown()},
            testBitmapName,
            imageSize = AttachmentsManager.ImageSize.LOW)
        signal.await()
        assertTrue(result)
    }

    @Test
    fun downloadImage() {
        val signal = CountDownLatch(1)
        var result = false

        val attachmentsManager = AttachmentsManager("test")
        attachmentsManager.downloadFile("test_image.jpg",
            { result = it.exists(); signal.countDown() },
            { result = false; signal.countDown() })

        signal.await()
        assertTrue(result)
    }
}
