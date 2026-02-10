package com.baijum.ukufretboard.domain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility for sharing chord diagrams as PNG images via the Android share sheet.
 *
 * Converts a Compose [ImageBitmap] to a PNG file in the app's cache directory,
 * generates a content URI through [FileProvider], and launches an
 * [Intent.ACTION_SEND] with `image/png` MIME type so the user can share
 * to WhatsApp, Telegram, or any other app.
 */
object ChordImageSharer {

    private const val SHARED_DIR = "shared_chords"
    private const val FILE_NAME = "chord.png"

    /**
     * Shares a chord diagram image via the Android share sheet.
     *
     * @param context Android context used for file operations and starting the share intent.
     * @param imageBitmap The captured chord diagram as an [ImageBitmap].
     * @param chordName The chord name (e.g., "Am7") used as the share subject.
     */
    fun share(context: Context, imageBitmap: ImageBitmap, chordName: String) {
        val file = saveBitmapToCache(context, imageBitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, chordName)
            putExtra(Intent.EXTRA_TEXT, "$chordName — Ukulele Companion")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $chordName"))
    }

    /**
     * Saves the [ImageBitmap] as a PNG file in the cache directory.
     *
     * @return The [File] containing the saved PNG image.
     */
    private fun saveBitmapToCache(context: Context, imageBitmap: ImageBitmap): File {
        val dir = File(context.cacheDir, SHARED_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, FILE_NAME)
        file.outputStream().use { out ->
            imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
