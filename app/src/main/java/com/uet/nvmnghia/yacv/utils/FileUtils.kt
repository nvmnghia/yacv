package com.uet.nvmnghia.yacv.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*


class FileUtils {
    companion object {

        private val IMAGE_FORMATS = setOf("jpg", "jpeg", "bmp", "gif", "png", "webp")

        /**
         * Given a file name, check if it is an image, simply by extension.
         * This method does not validate whatsoever the file integrity.
         *
         * @param fileName File name of the image
         * @return Whether the file is an image or not
         */
        fun isImage(fileName: String): Boolean {
            return IMAGE_FORMATS.contains(
                fileName.substringAfterLast('.').toLowerCase(Locale.ROOT))
        }


        //================================================================================
        // Resolving URI
        // https://stackoverflow.com/a/60642994/5959593
        // Because fuck SAF
        // This code is still here for further evaluation
        //================================================================================

        const val RAW_SCHEME = "raw:"

        val MATCH_RAW_SCHEME          = "^raw:".toRegex()
        val MATCH_DOCUMENT_RAW_SCHEME = "^/document/raw:".toRegex()

        /**
         * Given a URI, convert it to a canonical native path if possible.
         */
        fun getCanonicalPath(uri: Uri, context: Context): String? {
            val path = getPath(uri, context)
            return if (path == null) {
                null
            } else {
                File(path).canonicalPath
            }
        }

        /**
         * Given a URI, convert it to a native path if possible.
         */
        private fun getPath(uri: Uri, context: Context): String? {
            var contentUri: Uri? = null
            val selection: String?
            val selectionArgs: Array<String>?

            // Weird: For folders inside Download
            // - Android 11:
            //   content://com.android.externalstorage.documents/tree/primary:Download/...
            // - The rest:
            //   content://com.android.providers.downloads.documents/tree/raw:/storage/emulated/0/Download/...

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val (scheme, path) = docId.split(':')
                val fullPath = getPathFromExtSD(scheme, path)
                return if (fullPath !== "") {
                    fullPath
                } else {
                    null
                }
            }

            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var cursor: Cursor? = null

                    try {
                        cursor = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                            null, null, null)

                        if (cursor != null && cursor.moveToFirst()) {
                            val fileName: String = cursor.getString(0)
                            val path = "${Environment.getExternalStorageDirectory()}/Download/$fileName"
                            if (!TextUtils.isEmpty(path)) {
                                return path
                            }
                        }
                    } finally {
                        cursor?.close()
                    }

                    val id: String = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith(RAW_SCHEME)) {
                            return id.replaceFirst(RAW_SCHEME, "")
                        }

                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        )
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            return try {
                                contentUri = ContentUris.withAppendedId(
                                    Uri.parse(contentUriPrefix), id.toLong())
                                getDataColumn(context, contentUri, null, null)
                            } catch (nfe: NumberFormatException) {
                                // In Android 8 and Android P the id is not a number
                                uri.path
                                    ?.replaceFirst(MATCH_DOCUMENT_RAW_SCHEME, "")
                                    ?.replaceFirst(MATCH_RAW_SCHEME, "")
                            }
                        }
                    }
                } else {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith(RAW_SCHEME)) {
                        return id.replaceFirst(RAW_SCHEME, "")
                    }

                    try {
                        contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong())
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    }

                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null)
                    }
                }
            }

            // MediaProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(':')
                val type = split[0]
                contentUri = null

                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }

                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context)
            }

            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri, context)
            }

            if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }

                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }

                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // return getFilePathFromURI(context,uri);
                    copyFileToInternalStorage(uri, "userfiles", context)
                    // return getRealPathFromURI(context,uri);
                } else {
                    getDataColumn(context, uri, null, null)
                }
            }

            if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }

            return null
        }

        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(scheme: String, path: String): String {
            val type = scheme
            val relativePath = "/$path"
            var fullPath: String

            // On my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage.
            // Don't know any API that can get the root path of that storage
            // based on its id.
            // So no "primary" type, but let the check here for other devices.
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = "${Environment.getExternalStorageDirectory()}/$relativePath"
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            // Environment.isExternalStorageRemovable() returns `true` for both
            // external and internal storage so we cannot rely on it.
            // Instead, for each possible path, check if file exists.
            // We'll start with secondary storage as this could be our (physically)
            // removable sd card.
            fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }

            fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        private fun getDriveFilePath(uri: Uri, context: Context): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = context.contentResolver.query(
                returnUri, null, null, null, null)!!
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()

            val name: String = returnCursor.getString(nameIndex)
//            val size = returnCursor.getLong(sizeIndex).toString()
            returnCursor.close()

            val file = File(context.cacheDir, name)

            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val outputStream = FileOutputStream(file)

//                val maxBufferSize = 1 shl 20
//                val bytesAvailable: Int = inputStream.available()
//                val buffers = ByteArray(min(bytesAvailable, maxBufferSize))
//                var read: Int
//                while (inputStream.read(buffers).also { read = it } != -1) {
//                    outputStream.write(buffers, 0, read)
//                }
                inputStream.copyTo(outputStream)

                Log.i("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.i("File Path", "Path " + file.path)
                Log.i("File Size", "Size " + file.length())
            } catch (e: Exception) {
                e.message?.let { Log.e("Exception", it) }
            }

            return file.path
        }

        /***
         * Used for Android Q+.
         *
         * @param uri
         * @param newDirName if you want to create a directory, you can set this variable
         * @return
         */
        private fun copyFileToInternalStorage(uri: Uri, newDirName: String, context: Context): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = context.contentResolver.query(returnUri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null, null, null)!!

            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             *
             */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()

            val name: String = returnCursor.getString(nameIndex)
//            val size = returnCursor.getLong(sizeIndex).toString()
            returnCursor.close()

            val output = if (newDirName != "") {
                val dir = File("${context.filesDir}/$newDirName")
                if (!dir.exists()) {
                    dir.mkdir()
                }
                File("${context.filesDir}/$newDirName/$name")
            } else {
                File("${context.filesDir}/$name")
            }

            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val outputStream = FileOutputStream(output)

//                var read: Int
//                val bufferSize = 1024
//                val buffers = ByteArray(bufferSize)
//                while (inputStream.read(buffers).also { read = it } != -1) {
//                    outputStream.write(buffers, 0, read)
//                }
                inputStream.copyTo(outputStream)

                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
                e.message?.let { Log.e("Exception", it) }
            }
            return output.path
        }

        private fun getFilePathForWhatsApp(uri: Uri, context: Context): String? {
            return copyFileToInternalStorage(uri, "whatsapp", context)
        }

        private fun getDataColumn(context: Context?, uri: Uri?,
            selection: String?, selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context!!.contentResolver.query(uri!!, projection,
                    selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return uri.authority == "com.android.externalstorage.documents"
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return uri.authority == "com.android.providers.downloads.documents"
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return uri.authority == "com.android.providers.media.documents"
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return uri.authority == "com.google.android.apps.photos.content"
        }

        private fun isWhatsAppFile(uri: Uri): Boolean {
            return uri.authority == "com.whatsapp.provider.media"
        }

        private fun isGoogleDriveUri(uri: Uri): Boolean {
            return uri.authority == "com.google.android.apps.docs.storage" ||
                    uri.authority == "com.google.android.apps.docs.storage.legacy"
        }

    }
}
