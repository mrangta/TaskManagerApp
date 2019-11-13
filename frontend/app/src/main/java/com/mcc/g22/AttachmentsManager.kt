package com.mcc.g22

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import java.nio.channels.spi.AbstractSelectionKey


/**
 * Manage attachments of the project - upload and download them
 *
 * @param projectId ID of the project to work on
 */
class AttachmentsManager(private var projectId: String) {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    init {
        // Enable persistence to be sure that realtime database will be updated
        database.setPersistenceEnabled(true)
    }

    /**
     * This class lists all possible settings of image size
     */
    enum class ImageSize(val size: String) {
        LOW("_640x480"),
        HIGH("_1280x960"),
        FULL(""),
    }

    private fun filenameToDatabaseKey(fileName: String): String {
        return fileName.replace('.', '?')
    }

    private fun databaseKeyToFilename(key: String): String {
        return key.replace('?', '.')
    }

    private fun getFilenameOfImageInSize(basicFilename: String, size: ImageSize): String {
        return basicFilename.substringBeforeLast('.') + size.toString() +
                basicFilename.substringAfterLast('.')
    }

    /**
     * Upload new attachment to the project. Register attachment in configuration of the project.
     *
     * @param uri URI to file to upload
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName change name of the file in the storage to this name. If empty, filename is
     *                  not changed
     * @param imageSize If file to send is an image, it will be resized to the given size before
     *                  uploading it to the storage.
     *                  If file is not an image, this parameter ignored
     */
    fun uploadFile(
        uri: Uri, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = "", imageSize: ImageSize = FULL
    ) {

        var filenameInStorage: String = uri.lastPathSegment.toString()
        if (fileName != "") filenameInStorage = fileName

        val newFile = storage.reference.child("$projectId/$filenameInStorage")
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
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
     * @param file file to upload
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName change name of the file in the storage to this name. If empty, filename is
     *                  not changed
     * @param imageSize If file to send is an image, it will be resized to the given size before
     *                  uploading it to the storage.
     *                  If file is not an image, this parameter ignored
     */
    fun uploadFile(
        file: File, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = "", imageSize: ImageSize = FULL
    ) {
        uploadFile(Uri.fromFile(file), onFileUploaded, onFailure, fileName, imageSize)
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
                fileName.substringAfterLast('.')
            )
        }
        if (setOf("jpg", "png").contains(dstFile?.extension))
            nameOfFileToDownload = getFilenameOfImageInSize(fileName, imageSize)

        val f = storage.reference.child("$projectId/$nameOfFileToDownload")
        f.getFile(dstFile!!).addOnSuccessListener {
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