package com.github.sumimakito.awesomeqr

import android.graphics.Bitmap
import android.graphics.RectF
import com.waynejo.androidndkgif.GifDecoder
import com.waynejo.androidndkgif.GifEncoder
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class GifPipeline {
    var errorInfo: String? = null
    private var gifDecoder: GifDecoder? = null
    private var frameSequence = LinkedList<Bitmap>()
    private var currentFrame = 0
    private var outputFile: File? = null
    private var cropRect: RectF? = null

    fun init(file: File): Boolean {
        if (!file.exists()) {
            errorInfo = "ENOENT: File does not exist."
            return false
        } else if (file.isDirectory) {
            errorInfo = "EISDIR: Target is a directory."
            return false
        }
        gifDecoder = GifDecoder()
        val isSucceeded = gifDecoder!!.load(file.absolutePath)
        if (!isSucceeded) {
            errorInfo = "Failed to decode input file as GIF."
            return false
        }
        return true
    }

    fun setOutputFile(outputFile: File) {
        this.outputFile = outputFile
    }

    fun setCropRect(cropRect: RectF) {
        this.cropRect = cropRect
    }

    fun nextFrame(): Bitmap? {
        if (gifDecoder!!.frameNum() == 0) {
            errorInfo = "GIF contains zero frames."
            return null
        }
        if (currentFrame < gifDecoder!!.frameNum()) {
            val frame = gifDecoder!!.frame(currentFrame)
            currentFrame++
            if (cropRect != null) {
                val cropped = Bitmap.createBitmap(frame, Math.round(cropRect!!.left), Math.round(cropRect!!.top),
                        Math.round(cropRect!!.width()), Math.round(cropRect!!.height()))
                frame.recycle()
                return cropped
            }
            return frame
        } else
            return null
    }

    fun pushRendered(bitmap: Bitmap) {
        frameSequence.addLast(bitmap)
    }

    fun postRender(): Boolean {
        if (outputFile == null) {
            errorInfo = "Output file is not yet set."
            return false
        }

        if (frameSequence.size == 0) {
            errorInfo = "Zero frames in the sequence."
            return false
        }

        try {
            val gifEncoder = GifEncoder()
            gifEncoder.init(frameSequence.first.width, frameSequence.first.height, outputFile!!.absolutePath, GifEncoder.EncodingType.ENCODING_TYPE_FAST)
            val frameIndex = 0
            while (!frameSequence.isEmpty()) {
                gifEncoder.encodeFrame(frameSequence.removeFirst(), gifDecoder!!.delay(frameIndex))
            }
            gifEncoder.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            errorInfo = "FileNotFoundException. See stacktrace for more information."
            return false
        }

        return true
    }

    fun release(): Boolean {
        return true
    }
}
