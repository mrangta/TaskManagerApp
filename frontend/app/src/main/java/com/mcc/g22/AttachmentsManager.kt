package com.mcc.g22

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mcc.g22.AttachmentsManager.ImageSize.*
import java.io.ByteArrayOutputStream
import java.io.File


/**
 * Manage attachments of the project - upload and download them
 *
 * @param projectId ID of the project to work on
 */
class AttachmentsManager(private var projectId: String) {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        private const val LOW_WIDTH_RESOLUTION: Int = 640
        private const val LOW_HEIGHT_RESOLUTION: Int = 480
        private const val HIGH_WIDTH_RESOLUTION: Int = 1280
        private const val HIGH_HEIGHT_RESOLUTION: Int = 960
    }

    /**
     * This class lists all possible settings of image size
     */
    enum class ImageSize(val size: String) {
        LOW("_" + LOW_WIDTH_RESOLUTION + "x$LOW_HEIGHT_RESOLUTION"),
        HIGH("_" + HIGH_WIDTH_RESOLUTION + "x$HIGH_HEIGHT_RESOLUTION"),
        FULL(""),
    }

    /**
     * Change name of file to key which can be accepted in database
     * @param fileName
     * @return key which can be used in Realtime Storage to assigned the file with the project
     */
    private fun filenameToDatabaseKey(fileName: String): String {
        return fileName.replace('.', '?')
    }
    /**
     * Change database key to name of file saved in Firebase Storage
     * @param key
     * @return name of file saved in Firebase Storage and associated with the key
     */
    private fun databaseKeyToFilename(key: String): String {
        return key.replace('?', '.')
    }
    /**
     * @param basicFilename basic filename of the image - as it was saved in the database
     * @param size requested size
     * @return filename of the image in the given size
     */
    private fun getFilenameOfImageInSize(basicFilename: String, size: ImageSize): String {
        return if (size == FULL) basicFilename
        else basicFilename.substringBeforeLast('.') + size.size + "." +
                basicFilename.substringAfterLast('.')
    }

    /**
     * Resize bitmap to the requested size.
     * @param bitmap bitmap in full size
     * @param imageSize requested image size
     * @return resized bitmap
     */
    private fun resizeBitmap(bitmap: Bitmap, imageSize: ImageSize): Bitmap {
        when (imageSize) {
            LOW -> {
                Log.e("MCCC", "$LOW_WIDTH_RESOLUTION, $LOW_HEIGHT_RESOLUTION, ${bitmap.width}, ${bitmap.height}")
                val targetSize = calculateSize(bitmap.width, bitmap.height,
                    LOW_WIDTH_RESOLUTION, LOW_HEIGHT_RESOLUTION)
                return Bitmap.createScaledBitmap(bitmap,
                    targetSize.first ,
                    targetSize.second,
                    true)
            }
            HIGH -> {
                val targetSize = calculateSize(bitmap.width, bitmap.height,
                    HIGH_WIDTH_RESOLUTION, HIGH_HEIGHT_RESOLUTION)
                return Bitmap.createScaledBitmap(bitmap,
                    targetSize.first ,
                    targetSize.second,
                    true)
            }
            FULL -> return bitmap
        }
    }
    /**
     * Return smaller number
     */
    private fun min(i1: Int, i2: Int): Int {
        return if (i1 > i2) i2
        else i1
    }
    /**
     * Calculate new size of image.
     * @param currentWidth
     * @param currentHeight
     * @param targetMaxWidth
     * @param targetMaxHeight
     * @param keepRatio
     * @return new width and height
     */
    private fun calculateSize(currentWidth: Int, currentHeight: Int,
                              targetMaxWidth: Int, targetMaxHeight: Int,
                              keepRatio: Boolean = true): Pair<Int, Int> {

        return if (keepRatio) {

            val ratio = currentWidth.toFloat() / currentHeight.toFloat()
            if (currentHeight > currentWidth) {
                val h = min(targetMaxHeight, currentHeight)
                Pair((h * ratio).toInt(), h)
            } else {
                val w = min(targetMaxWidth, currentWidth)
                Pair(w, (w / ratio).toInt())
            }
        } else {
            Pair(targetMaxWidth, targetMaxHeight)
        }
    }

    /**
     * Check if given file is an image or not
     * @param name name of file
     * @return true if file is an image
     */
    @SuppressLint("DefaultLocale")
    private fun isFileImage(name: String): Boolean {

        val extension = name.substringAfterLast('.').toLowerCase()
        return setOf("jpg", "png").contains(extension)
    }

    /**
     * Upload new attachment to the project. Register attachment in configuration of the project.
     *
     * @param context application context
     * @param uri URI to file to upload
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName change name of the file in the storage to this name. If empty, filename is
     *                  not changed
     * @param imageSize If file to send is an image, it will be resized to the given size before
     *                  uploading it to the storage.
     *                  If file is not an image, this parameter ignored
     */
    fun uploadFile(context: Context,
        uri: Uri, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = "", imageSize: ImageSize = FULL
    ) {
        var requestedSize = imageSize
        var filenameInStorage: String = uri.lastPathSegment!!.substringAfterLast('/')
        if (fileName != "") filenameInStorage = fileName

        // Every file which is not an image has FULL requested size
        // (because they are treated in the same way as full-size images)
        if (!isFileImage(filenameInStorage)) requestedSize = FULL

        val newFile = storage.reference.child("$projectId/$filenameInStorage")
        if (requestedSize == FULL) {
            val uploadTask = newFile.putFile(uri)

            uploadTask.addOnFailureListener {
                onFailure()
            }.addOnSuccessListener {

                // Register uploaded file in the Realtime Database
                database.reference.child("projects")
                    .child(projectId)
                    .child("attachments")
                    .child(filenameToDatabaseKey(filenameInStorage)).setValue(true)

                onFileUploaded()
            }
        } else {
            uploadFile(BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri)),
                onFileUploaded,
                onFailure,
                fileName,
                requestedSize)
        }
    }

    /**
     * Upload new attachment to the project. Register attachment in configuration of the project.
     *
     * @param bitmap bitmap which will be saved as JPG file
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName name of the file
     * @param imageSize If file to send is an image, it will be resized to the given size before
     *                  uploading it to the storage.
     *                  If file is not an image, this parameter ignored
     */
    fun uploadFile(
        bitmap: Bitmap, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String, imageSize: ImageSize = FULL
    ) {

        val newFile = storage.reference.child("$projectId/$fileName")

        val stream = ByteArrayOutputStream()
        val resizedBitmap = resizeBitmap(bitmap, imageSize)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val uploadTask = newFile.putBytes(stream.toByteArray())

        uploadTask.addOnFailureListener {
            onFailure()
        }.addOnSuccessListener {

            // Register uploaded file in the Realtime Database
            database.reference.child("projects")
                .child(projectId)
                .child("attachments")
                .child(filenameToDatabaseKey(fileName)).setValue(true)

            onFileUploaded()
        }
    }

    /**
     * Upload new attachment to the project. Register attachment in configuration of the project.
     *
     * @param context application context
     * @param file file to upload
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName change name of the file in the storage to this name. If empty, filename is
     *                  not changed
     * @param imageSize If file to send is an image, it will be resized to the given size before
     *                  uploading it to the storage.
     *                  If file is not an image, this parameter ignored
     */
    fun uploadFile(context: Context,
        file: File, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = "", imageSize: ImageSize = FULL
    ) {
        uploadFile(context, Uri.fromFile(file), onFileUploaded, onFailure, fileName, imageSize)
    }

    /**
     * Download file to local device.
     *
     * @param fileName name of file to download
     * @param onFileDownloaded function to be called when the file is downloaded
     * @param onFailure function to be called when file cannot be downloaded
     * @param localFile local file where downloaded file should be saved
     *                  By default, temporary file is created (with name defined in @see fileName)
     * @param imageSize If file to download is an image, it will be downloaded in the given size.
     *                  If image is not available in the given size, the closest size
     *                  to the requested is downloaded
     */
    fun downloadFile(
        fileName: String, onFileDownloaded: (downloadedFile: File) -> Unit,
        onFailure: () -> Unit, localFile: File? = null, imageSize: ImageSize = FULL
    ) {
        var nameOfFileToDownload = fileName
        var dstFile = localFile
        if (dstFile == null) {
            dstFile = File.createTempFile(
                fileName.substringBeforeLast('.'),
                "." + fileName.substringAfterLast('.')
            )
        }

        if (isFileImage(dstFile!!.name)) {
            nameOfFileToDownload = getFilenameOfImageInSize(fileName, imageSize)
        }

        val f = storage.reference.child("$projectId/$nameOfFileToDownload")
        f.getFile(dstFile).addOnSuccessListener {
            onFileDownloaded(dstFile)
        }.addOnFailureListener {
            onFailure()
        }
    }

    /**
     * Get list of all attachments uploaded for the project
     * @param onListReady function to call when list of attachments has been gotten successfully
     * @param onFailure function to call in case of error
     */
    fun listAllAttachments(onListReady: (attachments: Set<String>) -> Unit,
                           onFailure: (databaseError: DatabaseError) -> Unit) {

        database.reference.child("projects")
            .child(projectId)
            .child("attachments")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(databaseError)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val attachments = mutableSetOf<String>()
                    for (a in dataSnapshot.children) {
                        val fileName = databaseKeyToFilename(a.key as String)
                        if (fileName.endsWith("$LOW.jpg") ||
                                fileName.endsWith("$HIGH.jpg") ||
                                fileName.endsWith("$LOW.png") ||
                                fileName.endsWith("$HIGH.png")) {
                            continue
                        }
                        attachments.add(fileName)
                    }
                    onListReady(attachments)
                }
            })
    }

    /**
     * Load image with the given filename to the given image view
     *
     * @param fileName name of the image to load
     * @param context application context
     * @param imageView view to show the image
     * @param imageSize If file to download is an image, it will be downloaded in the given size.
     *                  If image is not available in the given size, the closest size
     *                  to the requested is downloaded
     */
    fun loadImage(fileName: String, context: Context, imageView: ImageView,
                  imageSize: ImageSize = FULL
    ) {
        val imageFilename = getFilenameOfImageInSize(fileName, imageSize)
        val imageRef = storage.reference.child("$projectId/$imageFilename")
        Glide.with(context).load(imageRef).into(imageView)
    }
}