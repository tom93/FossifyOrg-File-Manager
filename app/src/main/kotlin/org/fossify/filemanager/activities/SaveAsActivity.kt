package org.fossify.filemanager.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import org.fossify.commons.dialogs.FilePickerDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.filemanager.R
import org.fossify.filemanager.databinding.ActivitySaveAsBinding
import org.fossify.filemanager.extensions.config
import java.io.File

class SaveAsActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySaveAsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (intent.action == Intent.ACTION_SEND && intent.extras?.containsKey(Intent.EXTRA_STREAM) == true) {
            FilePickerDialog(this, pickFile = false, showHidden = config.shouldShowHidden(), showFAB = true, showFavoritesButton = true) {
                val destination = it
                handleSAFDialog(destination) {
                    toast(R.string.saving)
                    ensureBackgroundThread {
                        try {
                            if (!getDoesFilePathExist(destination)) {
                                if (needsStupidWritePermissions(destination)) {
                                    val document = getDocumentFile(destination)
                                    document!!.createDirectory(destination.getFilenameFromPath())
                                } else {
                                    File(destination).mkdirs()
                                }
                            }

                            val source = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
                            val filename = getFilenameFromContentUri(source) ?: source.toString().getFilenameFromPath()
                            val mimeType = contentResolver.getType(source) ?: intent.getType()?.takeIf { it != "*/*" } ?: filename.getMimeType()
                            val inputStream = contentResolver.openInputStream(source)

                            System.err.println("XXX source.toString() = \"${source.toString()}\"")
                            System.err.println("XXX source.toString().getFilenameFromPath() = \"${source.toString().getFilenameFromPath()}\"")
                            System.err.println("XXX getFilenameFromContentUri(source) = \"${getFilenameFromContentUri(source)}\"")
                            System.err.println("XXX contentResolver.getType(source) = \"${contentResolver.getType(source)}\"")
                            System.err.println("XXX intent.getType() = \"${intent.getType()}\"")
                            System.err.println("XXX filename.getMimeType() = \"${filename.getMimeType()}\"")

                            val destinationPath = "$destination/$filename"
                            val outputStream = getFileOutputStreamSync(destinationPath, mimeType, null)!!
                            inputStream!!.copyTo(outputStream)
                            rescanPaths(arrayListOf(destinationPath))
                            toast(R.string.file_saved)
                            finish()
                        } catch (e: Exception) {
                            showErrorToast(e)
                            finish()
                        }
                    }
                }
            }
        } else {
            toast(R.string.unknown_error_occurred)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.activitySaveAsToolbar, NavigationIcon.Arrow)
    }
}
