package com.pspgames.library.utils

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile

object ZipUtils {
    @Throws(IOException::class)
    fun unzip(sourceFile: File, destDirectory: String, filename: String, callback: (Boolean, File) -> Unit){
        File(destDirectory).run {
            if (!exists()) mkdir()
            if (!exists()) mkdirs()
        }
        ZipFile(sourceFile.absolutePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    if (!entry.isDirectory) {
                        val entryExtension = Utils.getExtension(entry.name)
                        extractFile(input, destDirectory, filename, entryExtension)
                        callback.invoke(true, File(destDirectory, filename + entryExtension))
                    } else {
                        File(destDirectory, entry.name).run {
                            if (!exists()) {
                                mkdir()
                            }
                            callback.invoke(true, this)
                        }
                    }
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destDirectory: String, filename: String, entryExtension: String) {
        val path = File(destDirectory, filename + entryExtension).absolutePath
        BufferedOutputStream(FileOutputStream(path)).use { bos ->
            var read: Int
            val bytesIn = ByteArray(4096)
            while (inputStream.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }

}