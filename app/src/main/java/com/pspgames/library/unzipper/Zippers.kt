package com.pspgames.library.unzipper

import com.pspgames.library.App
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile

class Zippers private constructor(builder: Builder) {
    var source: File
    val destination: File
    val filename: String
    val listener: ZippersListener?

    init {
        if(builder.source == null) throw Exception("Please call Builder.source()")
        if(builder.destination == null) throw Exception("Please call Builder.destination()")
        if(builder.filename == null) throw Exception("Please call Builder.filename()")
        this.source = builder.source!!
        this.destination = builder.destination!!
        this.filename = builder.filename!!
        this.listener = builder.listener
    }

    fun extract(){
        var resultFile = File("")
        try {
            listener?.onStart()
            createDirs(destination)
            ZipFile(source.absolutePath).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        if (!entry.isDirectory) {
                            val fileExtension = getExtension(filename)
                            val entryExtension = getExtension(entry.name)
                            val newFilename = filename.replace(fileExtension, entryExtension)
                            resultFile = File(destination, newFilename)
                            extractFile(input, resultFile)
                        } else {
                            resultFile = File(destination, entry.name)
                            createDir(resultFile)
                        }
                    }
                }
            }
            listener?.onComplete(resultFile)
        } catch (e: Exception) {
            App.log(e.message!!)
            listener?.onError(e)
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: File) {
        BufferedOutputStream(FileOutputStream(destFilePath.absolutePath)).use { bos ->
            var read: Int
            val bytesIn = ByteArray(4096)
            while (inputStream.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }

    @Throws(IOException::class)
    fun createDirs(destFile: File){
        destFile.run {
            if(isDirectory){
                if (!exists()) {
                    mkdirs()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun createDir(destFile: File){
        destFile.run {
            if(isDirectory){
                if (!exists()) {
                    mkdir()
                }
            }
        }
    }

    private fun getExtension(input: String) : String{
        return input.substring(input.lastIndexOf("."))
    }

    class Builder {
        var source: File? = null
        var destination: File? = null
        var filename: String? = null
        var listener: ZippersListener? = null
        fun source(source: File) = apply { this.source = source }
        fun destination(destination: File) = apply { this.destination = destination }
        fun filename(filename: String) = apply { this.filename = filename }
        fun listener(listener: ZippersListener) = apply { this.listener = listener }
        fun extract() = Zippers(this).extract()
    }
}