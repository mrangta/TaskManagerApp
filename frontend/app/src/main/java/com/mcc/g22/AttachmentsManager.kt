package com.mcc.g22

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Manage attachments of the project - upload and download them
 *
 * @param projectId ID of the project to work on
 */
class AttachmentsManager(private var projectId: String) {
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Upload new attachment to the project. Register attachment in configuration of the project.
     *
     * @param uri URI to file to upload
     * @param onFileUploaded function to be called when the file is uploaded successfully
     * @param onFailure function to be called when the file could not be uploaded
     * @param fileName change name of the file in the storage to this name. If empty, filename is
     *                  not changed
     */
    fun uploadFile(
        uri: Uri, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = ""
    ) {

        var filenameInStorage = uri.lastPathSegment
        if (fileName != "") filenameInStorage = fileName

        val newFile = storage.reference.child("$projectId/$filenameInStorage")
        val uploadTask = newFile.putFile(uri)

        uploadTask.addOnFailureListener {
            onFailure()
        }.addOnSuccessListener {
            // TODO register uploaded file in the Realtime Database
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
     */
    fun uploadFile(
        bitmap: Bitmap, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String
    ) {

        val newFile = storage.reference.child("$projectId/$fileName")

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val uploadTask = newFile.putBytes(stream.toByteArray())

        uploadTask.addOnFailureListener {
            onFailure()
        }.addOnSuccessListener {
            // TODO register uploaded file in the Realtime Database
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
     */
    fun uploadFile(
        file: File, onFileUploaded: () -> Unit, onFailure: () -> Unit,
        fileName: String = ""
    ) {
        uploadFile(Uri.fromFile(file), onFileUploaded, onFailure, fileName)
    }

    /**
     * Download file to local device.
     *
     * @param fileName name of file to download
     * @param onFileDownloaded function to be called when the file is downloaded
     * @param onFailure function to be called when file cannot be downloaded
     * @param localFile local file where downloaded file should be saved
     *                  By default, temporary file is created (with name defined in @see fileName)
     */
    fun downloadFile(
        fileName: String, onFileDownloaded: (downloadedFile: File) -> Unit,
        onFailure: () -> Unit, localFile: File? = null
    ) {

        var dstFile = localFile
        if (dstFile == null) {
            dstFile = File.createTempFile(
                fileName.substringBeforeLast('.'),
                fileName.substringAfterLast('.')
            )
        }

        val f = storage.reference.child("$projectId/$fileName")
        f.getFile(dstFile!!).addOnSuccessListener {
            onFileDownloaded(dstFile)
        }.addOnFailureListener {
            onFailure()
        }
    }

    /**
     * Load image with the given filename to the given image view
     *
     * @param fileName name of the image to load
     * @param context application context
     * @param imageView view to show the image
     */
    fun loadImage(fileName: String, context: Context, imageView: ImageView) {

        val imageRef = storage.reference.child("$projectId/$fileName")
        Glide.with(context).load(imageRef).into(imageView)
    }
}