package com.github.sumimakito.awesomeqr

import android.graphics.*
import android.util.Log
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.BlendBackground
import com.github.sumimakito.awesomeqr.option.background.GifBackground
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.util.RectUtils
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import java.lang.Math.max
import java.util.*

class AwesomeQrRenderer {
    companion object {
        /**
         * @Brief: Check out this qrCode explanation before doing anything:
         * [tutorial](https://www.thonky.com/qr-code-tutorial/data-encoding)
         * [table](https://www.thonky.com/qr-code-tutorial/character-capacities)
         * [wikipedia] (https://en.wikipedia.org/wiki/QR_code)
         *
         * your qecode input size depends on QRCODE_VERSION and ERROR_CORRECTION_LEVEL. See link 2 for details.
         * For example, qr version = 6, ecl = H can hold string with size (58 - 1) = 57
         *
         * I am setting verison = 6, ecl = H after some trial and errors
         *
         * Higher versions take more time on scanners, has more capacity
         * Logo size max = 40 % of generated qrCode's height
         *
         * */

        val TAG: String = AwesomeQrRenderer::class.java.simpleName
        /**
         * For more information about QR code, refer to: https://en.wikipedia.org/wiki/QR_code
         * BYTE_EPT: Empty block
         * BYTE_DTA: Data block
         * BYTE_POS: Position block
         * BYTE_AGN: Align block
         * BYTE_TMG: Timing block
         * BYTE_PTC: Protector block, translucent layer (custom block, this is not included in QR code's standards)
         */
        private const val BYTE_EPT = 0x0.toByte()
        private const val BYTE_DTA = 0x1.toByte()
        private const val BYTE_POS = 0x2.toByte()
        private const val BYTE_AGN = 0x3.toByte()
        private const val BYTE_TMG = 0x4.toByte()
        private const val BYTE_PTC = 0x5.toByte()

        @JvmStatic
        @Throws(Exception::class)
        fun render(renderOptions: RenderOption): RenderResult {
            if (renderOptions.background is GifBackground) {
                val background = renderOptions.background as GifBackground
                if (background.outputFile == null) {
                    throw Exception("Output file has not yet been set. It is required under GIF background mode.")
                }
                val gifPipeline = GifPipeline()
                if (!gifPipeline.init(background.inputFile!!)) {
                    throw Exception("GifPipeline failed to init: " + gifPipeline.errorInfo)
                }
                gifPipeline.clippingRect = background.clippingRectF
                gifPipeline.outputFile = background.outputFile
                var frame: Bitmap?
                var renderedFrame: Bitmap
                var firstRenderedFrame: Bitmap? = null
                frame = gifPipeline.nextFrame()
                while (frame != null) {
                    renderedFrame = renderFrame(renderOptions, frame)
                    gifPipeline.pushRendered(renderedFrame)
                    if (firstRenderedFrame == null) {
                        firstRenderedFrame = renderedFrame.copy(Bitmap.Config.ARGB_8888, true)
                    }
                    frame = gifPipeline.nextFrame()
                }
                if (gifPipeline.errorInfo != null) {
                    throw Exception("GifPipeline failed to render frames: " + gifPipeline.errorInfo)
                }
                if (!gifPipeline.postRender()) {
                    throw Exception("GifPipeline failed to do post render works: " + gifPipeline.errorInfo)
                }
                return RenderResult(firstRenderedFrame, background.outputFile, RenderResult.OutputType.GIF)
            } else if (renderOptions.background is BlendBackground && renderOptions.background!!.bitmap != null) {
                val background = renderOptions.background as BlendBackground
                var clippedBackground: Bitmap? = null
                if (background.clippingRect != null) {
                    clippedBackground = Bitmap.createBitmap(
                        background.bitmap!!,
                        Math.round(background.clippingRect!!.left.toFloat()),
                        Math.round(background.clippingRect!!.top.toFloat()),
                        Math.round(background.clippingRect!!.width().toFloat()),
                        Math.round(background.clippingRect!!.height().toFloat())
                    )
                }
                val rendered = renderFrame(renderOptions, clippedBackground ?: background.bitmap)
                clippedBackground?.recycle()
                val scaledBoundingRects = scaleImageBoundingRectByClippingRect(background.bitmap!!, renderOptions.size, background.clippingRect)
                val fullRendered = Bitmap.createScaledBitmap(background.bitmap!!, scaledBoundingRects[0].width(), scaledBoundingRects[0].height(), true)
                val fullCanvas = Canvas(fullRendered)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = renderOptions.color.background
                paint.isFilterBitmap = true
                // What a weird fix... Hope I can find the culprit...
                fullCanvas.drawBitmap(rendered, Rect(0, 0, rendered.width, rendered.height), scaledBoundingRects[1], paint)
                return RenderResult(fullRendered, null, RenderResult.OutputType.Blend)
            } else if (renderOptions.background is StillBackground) {
                val background = renderOptions.background as StillBackground
                var clippedBackground: Bitmap? = null
                if (background.clippingRect != null) {
                    clippedBackground = Bitmap.createBitmap(
                        background.bitmap!!,
                        Math.round(background.clippingRect!!.left.toFloat()),
                        Math.round(background.clippingRect!!.top.toFloat()),
                        Math.round(background.clippingRect!!.width().toFloat()),
                        Math.round(background.clippingRect!!.height().toFloat())
                    )
                }
                val rendered = renderFrame(renderOptions, clippedBackground ?: background.bitmap)
                clippedBackground?.recycle()
                return RenderResult(rendered, null, RenderResult.OutputType.Still)
            } else {
                return RenderResult(renderFrame(renderOptions, null), null, RenderResult.OutputType.Still)
            }
        }

        @JvmStatic
        fun renderAsync(renderOptions: RenderOption, resultCallback: ((RenderResult) -> Unit)?, errorCallback: ((Exception) -> Unit)?) {
            Thread {
                try {
                    val renderResult = render(renderOptions)
                    resultCallback?.invoke(renderResult)
                } catch (e: Exception) {
                    errorCallback?.invoke(e)
                }
            }.start()
        }

        /**
         * The general render function.
         *
         * @return a Bitmap if success when under still background image mode
         * otherwise will return null if success
         * @throws Exception ONLY thrown when an error occurred
         */
        @Throws(Exception::class)
        private fun renderFrame(renderOptions: RenderOption, backgroundFrame: Bitmap?): Bitmap {
            var backgroundFrameTemp = backgroundFrame
            if (renderOptions.content.isEmpty()) {
                throw IllegalArgumentException("Error: content is empty. (content.isEmpty())")
            }
            if (renderOptions.size < 0) {
                throw IllegalArgumentException("Error: a negative size is given. (size < 0)")
            }
            if (renderOptions.borderWidth < 0) {
                throw IllegalArgumentException("Error: a negative borderWidth is given. (borderWidth < 0)")
            }
            if (renderOptions.size - 2 * renderOptions.borderWidth <= 0) {
                throw IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * borderWidth <= 0)")
            }
            val byteMatrix = getByteMatrix(renderOptions.content, renderOptions.ecl, renderOptions.qrCodeVersion)
                ?: throw NullPointerException("Error: ByteMatrix based on content is null. (getByteMatrix(content, ecl) == null)")
            val innerRenderedSize = renderOptions.size - 2 * renderOptions.borderWidth
            val nCount = byteMatrix.width
            val nSize = Math.round(innerRenderedSize.toFloat() / nCount) // Avoid non-integer values
            val unscaledInnerRenderSize = nSize * nCount // Draw on unscaled Bitmap first
            val unscaledFullRenderSize = unscaledInnerRenderSize + 2 * renderOptions.borderWidth // Draw on unscaled Bitmap first

            if (renderOptions.size - 2 * renderOptions.borderWidth < byteMatrix.width) {
                throw IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * borderWidth < " + byteMatrix.width + ")")
            }
            if (renderOptions.patternScale <= 0 || renderOptions.patternScale > 1) {
                throw IllegalArgumentException("Error: an illegal pattern scale is given. (patternScale <= 0 || patternScale > 1)")
            }
            if (renderOptions.logo != null && renderOptions.logo!!.bitmap != null) {
                val logo = renderOptions.logo!!
                if (logo.scale <= 0 || logo.scale > 0.5) {
                    throw IllegalArgumentException("Error: an illegal logo scale is given. (logo.scale <= 0 || logo.scale > 0.5)")
                }
                if (logo.borderWidth < 0 || logo.borderWidth * 2 >= unscaledInnerRenderSize) {
                    throw IllegalArgumentException("Error: an illegal logo border width is given. (logo.borderWidth < 0 || logo.borderWidth * 2 >= $unscaledInnerRenderSize)")
                }
                if (logo.borderRadius < 0) {
                    throw IllegalArgumentException("Error: a negative logo border radius is given. (logo.borderRadius < 0)")
                }
                val logoScaledSize = (unscaledInnerRenderSize * logo.scale).toInt()
                if (logo.borderRadius * 2 > logoScaledSize) {
                    throw IllegalArgumentException("Error: an illegal logo border radius is given. (logo.borderRadius * 2 > $logoScaledSize)")
                }
            }

            val backgroundDrawingRect = Rect(
                if (!renderOptions.clearBorder) 0 else renderOptions.borderWidth,
                if (!renderOptions.clearBorder) 0 else renderOptions.borderWidth,
                unscaledFullRenderSize - renderOptions.borderWidth * if (renderOptions.clearBorder) 1 else 0,
                unscaledFullRenderSize - renderOptions.borderWidth * if (renderOptions.clearBorder) 1 else 0
            )

            if (backgroundFrameTemp == null) {
                if (renderOptions.background is StillBackground
                    || renderOptions.background is BlendBackground
                ) {
                    backgroundFrameTemp = renderOptions.background!!.bitmap
                }
            }

            val unscaledFullRenderedBitmap = Bitmap.createBitmap(unscaledFullRenderSize, unscaledFullRenderSize, Bitmap.Config.ARGB_8888)

            if (renderOptions.color.auto && backgroundFrame != null) {
                renderOptions.color.light = -0x1
                renderOptions.color.dark = getDominantColor(backgroundFrame)
            }

            val paint = Paint()
            paint.isAntiAlias = true
            val paintBackground = Paint()
            paintBackground.isAntiAlias = true
            paintBackground.color = renderOptions.color.background
            paintBackground.style = Paint.Style.FILL
            val paintDark = Paint()
            paintDark.color = renderOptions.color.dark
            paintDark.isAntiAlias = true
            paintDark.style = Paint.Style.FILL
            val paintLight = Paint()
            paintLight.color = renderOptions.color.light
            paintLight.isAntiAlias = true
            paintLight.style = Paint.Style.FILL

            val paintProtector = Paint()
            paintProtector.color = Color.argb(120, 255, 255, 255)
            paintProtector.isAntiAlias = true
            paintProtector.style = Paint.Style.FILL

            val unscaledCanvas = Canvas(unscaledFullRenderedBitmap)
            unscaledCanvas.drawColor(Color.WHITE)
            unscaledCanvas.drawRect(
                (if (renderOptions.clearBorder) renderOptions.borderWidth else 0).toFloat(),
                (if (renderOptions.clearBorder) renderOptions.borderWidth else 0).toFloat(),
                (unscaledInnerRenderSize + if (renderOptions.clearBorder) renderOptions.borderWidth else renderOptions.borderWidth * 2).toFloat(),
                (unscaledInnerRenderSize + if (renderOptions.clearBorder) renderOptions.borderWidth else renderOptions.borderWidth * 2).toFloat(),
                paintBackground
            )
            if (backgroundFrame != null && renderOptions.background != null) {
                paint.alpha = Math.round(255 * renderOptions.background!!.alpha)
                unscaledCanvas.drawBitmap(
                    backgroundFrame, null,
                    backgroundDrawingRect,
                    paint
                )
            }
            paint.alpha = 255

/*            Log.e(TAG, "byteMatrix =\n${byteMatrix}")

            var str = "\n\n------------\n"
            for (row in 0 until byteMatrix.height) {
                for (col in 0 until byteMatrix.width) {
                    str += byteMatrix.get(col, row)
                }
                str += "\n"
            }
            Log.e(TAG, str)*/
            for (row in 0 until byteMatrix.height) {
                for (col in 0 until byteMatrix.width) {
                    when (byteMatrix.get(col, row)) {
                        BYTE_POS -> {
                            if(!renderOptions.isCustomPositions) {
                                unscaledCanvas.drawRect(
                                    (renderOptions.borderWidth + col * nSize).toFloat(),
                                    (renderOptions.borderWidth + row * nSize).toFloat(),
                                    (renderOptions.borderWidth + (col + 1) * nSize).toFloat(),
                                    (renderOptions.borderWidth + (row + 1) * nSize).toFloat(),
                                    paintDark
                                )

                            }
                        }
                        BYTE_TMG, BYTE_AGN -> unscaledCanvas.drawCircle(
                            renderOptions.borderWidth + (col + 0.5f) * nSize,
                            renderOptions.borderWidth + (row + 0.5f) * nSize,
                            renderOptions.patternScale * nSize.toFloat() * 0.5f,
                            paintDark
                        )
                        BYTE_DTA -> if (renderOptions.roundedPatterns) {
                            unscaledCanvas.drawCircle(
                                renderOptions.borderWidth + (col + 0.5f) * nSize,
                                renderOptions.borderWidth + (row + 0.5f) * nSize,
                                renderOptions.patternScale * nSize.toFloat() * 0.5f,
                                paintDark
                            )
                        } else {
                            unscaledCanvas.drawRect(
                                renderOptions.borderWidth + (col + 0.5f * (1 - renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (row + 0.5f * (1 - renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (col + 0.5f * (1 + renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (row + 0.5f * (1 + renderOptions.patternScale)) * nSize,
                                paintDark
                            )
                        }
                        BYTE_PTC -> unscaledCanvas.drawRect(
                            (renderOptions.borderWidth + col * nSize).toFloat(),
                            (renderOptions.borderWidth + row * nSize).toFloat(),
                            (renderOptions.borderWidth + (col + 1) * nSize).toFloat(),
                            (renderOptions.borderWidth + (row + 1) * nSize).toFloat(),
                            paintProtector
                        )
                        BYTE_EPT -> if (renderOptions.roundedPatterns) {
                            unscaledCanvas.drawCircle(
                                renderOptions.borderWidth + (col + 0.5f) * nSize,
                                renderOptions.borderWidth + (row + 0.5f) * nSize,
                                renderOptions.patternScale * nSize.toFloat() * 0.5f,
                                paintLight
                            )
                        } else {
                            unscaledCanvas.drawRect(
                                renderOptions.borderWidth + (col + 0.5f * (1 - renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (row + 0.5f * (1 - renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (col + 0.5f * (1 + renderOptions.patternScale)) * nSize,
                                renderOptions.borderWidth + (row + 0.5f * (1 + renderOptions.patternScale)) * nSize,
                                paintLight
                            )
                        }
                    }
                }
            }

            // for the circles
            var maximus = 0
            for (row in 0 until byteMatrix.height) {
                var continuousTwoCount = 0
                for (col in 0 until byteMatrix.width) {
                    if(byteMatrix.get(col, row) == BYTE_POS) {
                        continuousTwoCount++
                        maximus = max(maximus, continuousTwoCount)
                    }else {
                        continuousTwoCount = 0
                    }
                }
            }


            if(renderOptions.isCustomPositions) {
                roundedRectangularPositionFinder(unscaledCanvas, byteMatrix, renderOptions, nSize, maximus, paintDark, paintBackground)
            }
            //circlePositionFinder(unscaledCanvas, byteMatrix, renderOptions, nSize, maximus, paintDark, paintBackground)

            if (renderOptions.logo != null && renderOptions.logo!!.bitmap != null) {
                val logo = renderOptions.logo!!
                val logoScaledSize = (unscaledInnerRenderSize * logo.scale).toInt()
                val logoScaled = Bitmap.createScaledBitmap(logo.bitmap!!, logoScaledSize, logoScaledSize, true)
                val logoOpt = Bitmap.createBitmap(logoScaled.width, logoScaled.height, Bitmap.Config.ARGB_8888)
                val logoCanvas = Canvas(logoOpt)
                val logoRect = Rect(0, 0, logoScaled.width, logoScaled.height)
                val logoRectF = RectF(logoRect)
                val logoPaint = Paint()
                logoPaint.isAntiAlias = true
                logoPaint.color = -0x1
                logoPaint.style = Paint.Style.FILL
                logoCanvas.drawARGB(0, 0, 0, 0)
                logoCanvas.drawRoundRect(logoRectF, logo.borderRadius.toFloat(), logo.borderRadius.toFloat(), logoPaint)
                logoPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                logoCanvas.drawBitmap(logoScaled, logoRect, logoRect, logoPaint)
                logoPaint.color = renderOptions.color.light
                logoPaint.style = Paint.Style.STROKE
                logoPaint.strokeWidth = logo.borderWidth.toFloat()
                logoCanvas.drawRoundRect(logoRectF, logo.borderRadius.toFloat(), logo.borderRadius.toFloat(), logoPaint)
                unscaledCanvas.drawBitmap(logoOpt, (0.5 * (unscaledFullRenderedBitmap.width - logoOpt.width)).toInt().toFloat(),
                    (0.5 * (unscaledFullRenderedBitmap.height - logoOpt.height)).toInt().toFloat(), paint)
            }

            val renderedScaledBitmap = Bitmap.createBitmap(
                renderOptions.size,
                renderOptions.size,
                Bitmap.Config.ARGB_8888
            )

            val scaledCanvas = Canvas(renderedScaledBitmap)
            scaledCanvas.drawBitmap(unscaledFullRenderedBitmap, null, Rect(0, 0, renderedScaledBitmap.width, renderedScaledBitmap.height), paint)

            val renderedResultBitmap: Bitmap
            if (renderOptions.background is BlendBackground) {
                renderedResultBitmap = Bitmap.createBitmap(renderedScaledBitmap.width, renderedScaledBitmap.height, Bitmap.Config.ARGB_8888)
                val finalRenderedCanvas = Canvas(renderedResultBitmap)
                val finalClippingRect = Rect(0, 0, renderedScaledBitmap.width, renderedScaledBitmap.height)
                val finalClippingRectF = RectF(finalClippingRect)
                val finalClippingPaint = Paint()
                finalClippingPaint.isAntiAlias = true
                finalClippingPaint.color = -0x1
                finalClippingPaint.style = Paint.Style.FILL
                finalRenderedCanvas.drawARGB(0, 0, 0, 0)
                finalRenderedCanvas.drawRoundRect(finalClippingRectF, (renderOptions.background as BlendBackground).borderRadius.toFloat(), (renderOptions.background as BlendBackground).borderRadius.toFloat(), finalClippingPaint)
                finalClippingPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                finalRenderedCanvas.drawBitmap(renderedScaledBitmap, null, finalClippingRect, finalClippingPaint)

                renderedScaledBitmap.recycle()
            } else {
                renderedResultBitmap = renderedScaledBitmap
            }
            unscaledFullRenderedBitmap.recycle()

            return renderedResultBitmap
        }

        private fun circlePositionFinder(unscaledCanvas: Canvas, byteMatrix: ByteMatrix,
                                         renderOptions: RenderOption, nSize: Int,
                                         maximus: Int, paintDark: Paint, paintBackground: Paint) {
            val paintGreenAccent = Paint()
            paintGreenAccent.color = 0xFF145553.toInt()
            paintGreenAccent.isAntiAlias = true
            paintGreenAccent.style = Paint.Style.FILL

            val paintRedAccent = Paint()
            paintRedAccent.color = 0xFFD12725.toInt()
            paintRedAccent.isAntiAlias = true
            paintRedAccent.style = Paint.Style.FILL

            // for the circles
            val heightPx = byteMatrix.height * nSize
            val widthPx = byteMatrix.width * nSize

            val radiusPx: Float = (maximus / 2 * nSize).toFloat()
            val cx1 = renderOptions.borderWidth + radiusPx
            val cy1 = renderOptions.borderWidth + radiusPx

            val cx2 = renderOptions.borderWidth + radiusPx
            val cy2 = renderOptions.borderWidth + (heightPx - radiusPx)

            val cx3 = renderOptions.borderWidth + (widthPx - radiusPx)
            val cy3 = renderOptions.borderWidth + radiusPx

            unscaledCanvas.drawCircle(cx1, cy1, radiusPx, paintDark)
            unscaledCanvas.drawCircle(cx2, cy2, radiusPx, paintDark)
            unscaledCanvas.drawCircle(cx3, cy3, radiusPx, paintDark)

            unscaledCanvas.drawCircle(cx1, cy1, radiusPx - nSize, paintBackground)
            unscaledCanvas.drawCircle(cx2, cy2, radiusPx - nSize, paintBackground)
            unscaledCanvas.drawCircle(cx3, cy3, radiusPx - nSize, paintBackground)

            unscaledCanvas.drawCircle(cx1, cy1, radiusPx - 2*nSize, paintDark)
            unscaledCanvas.drawCircle(cx2, cy2, radiusPx - 2*nSize, paintDark)
            unscaledCanvas.drawCircle(cx3, cy3, radiusPx - 2*nSize, paintDark)
        }

        private fun roundedRectangularPositionFinder(unscaledCanvas: Canvas, byteMatrix: ByteMatrix,
                                                     renderOptions: RenderOption, nSize: Int,
                                                     maximus: Int, paintDark: Paint, paintBackground: Paint) {
            Log.e(TAG, "maximus = ${maximus}")
            val heightPx = byteMatrix.height * nSize
            val widthPx = byteMatrix.width * nSize

            val robWidthPx: Float = renderOptions.borderWidth.toFloat()
            val sideLengthPx: Float = (maximus * nSize).toFloat()
            val dx: Float = 0.7f*nSize.toFloat() // renderOptions.patternScale * 0.7F * nSize.toFloat() // sideLengthPx / 10
            val dxx = 2*nSize.toFloat() // dx + sideLengthPx / 70 * 13
            val cornerRadiusPx = 12F

            val topLeftPaint = Paint()
            topLeftPaint.color = renderOptions.color.topLeftColor
            topLeftPaint.isAntiAlias = true
            topLeftPaint.style = Paint.Style.FILL

            val topRightPaint = Paint()
            topRightPaint.color = renderOptions.color.topRigntColor
            topRightPaint.isAntiAlias = true
            topRightPaint.style = Paint.Style.FILL

            val bottomLeft = Paint()
            bottomLeft.color = renderOptions.color.bottomLeftColor
            bottomLeft.isAntiAlias = true
            bottomLeft.style = Paint.Style.FILL

            // top left
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx, 0F+robWidthPx,
                robWidthPx + sideLengthPx, robWidthPx + sideLengthPx),
                cornerRadiusPx, cornerRadiusPx, paintDark)
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx+dx, 0F+robWidthPx+dx,
                robWidthPx + sideLengthPx - dx, robWidthPx + sideLengthPx - dx),
                cornerRadiusPx, cornerRadiusPx, paintBackground)
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx + dxx, 0F+robWidthPx + dxx,
                robWidthPx + sideLengthPx - dxx, robWidthPx + sideLengthPx - dxx),
                cornerRadiusPx, cornerRadiusPx, topLeftPaint)

            // top right
            unscaledCanvas.drawRoundRect(RectF(widthPx - sideLengthPx + robWidthPx, 0+robWidthPx, widthPx+robWidthPx, robWidthPx+sideLengthPx),
                cornerRadiusPx, cornerRadiusPx, paintDark)
            unscaledCanvas.drawRoundRect(RectF(widthPx - sideLengthPx + robWidthPx+dx, 0+robWidthPx+dx, widthPx+robWidthPx-dx, robWidthPx+sideLengthPx-dx),
                cornerRadiusPx, cornerRadiusPx, paintBackground)
            unscaledCanvas.drawRoundRect(RectF(widthPx - sideLengthPx + robWidthPx+dxx, 0+robWidthPx+dxx, widthPx+robWidthPx-dxx, robWidthPx+sideLengthPx-dxx),
                cornerRadiusPx, cornerRadiusPx, topLeftPaint)

            // bottom left
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx, heightPx-sideLengthPx+robWidthPx, robWidthPx+sideLengthPx, robWidthPx+heightPx),
                cornerRadiusPx, cornerRadiusPx, paintDark)
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx+dx, heightPx-sideLengthPx+robWidthPx+dx, robWidthPx+sideLengthPx-dx, robWidthPx+heightPx-dx),
                cornerRadiusPx, cornerRadiusPx, paintBackground)
            unscaledCanvas.drawRoundRect(RectF(0F+robWidthPx+dxx, heightPx-sideLengthPx+robWidthPx+dxx, robWidthPx+sideLengthPx-dxx, robWidthPx+heightPx-dxx),
                cornerRadiusPx, cornerRadiusPx, bottomLeft)
        }

        private fun getByteMatrix(contents: String, errorCorrectionLevel: ErrorCorrectionLevel, qrCodeVersion: Int): ByteMatrix? {
            try {
                val qrCode = getProtoQrCode(contents, errorCorrectionLevel, qrCodeVersion)
                val agnCenter = qrCode.version.alignmentPatternCenters
                val byteMatrix = qrCode.matrix
                val matSize = byteMatrix.width
                for (row in 0 until matSize) {
                    for (col in 0 until matSize) {
                        if (isTypeAGN(col, row, agnCenter, true)) {
                            if (byteMatrix.get(col, row) != BYTE_EPT) {
                                byteMatrix.set(col, row, BYTE_AGN)
                            } else {
                                byteMatrix.set(col, row, BYTE_PTC)
                            }
                        } else if (isTypePOS(col, row, matSize, true)) {
                            if (byteMatrix.get(col, row) != BYTE_EPT) {
                                byteMatrix.set(col, row, BYTE_POS)
                            } else {
                                byteMatrix.set(col, row, BYTE_PTC)
                            }
                        } else if (isTypeTMG(col, row, matSize)) {
                            if (byteMatrix.get(col, row) != BYTE_EPT) {
                                byteMatrix.set(col, row, BYTE_TMG)
                            } else {
                                byteMatrix.set(col, row, BYTE_PTC)
                            }
                        }

                        if (isTypePOS(col, row, matSize, false)) {
                            if (byteMatrix.get(col, row) == BYTE_EPT) {
                                byteMatrix.set(col, row, BYTE_PTC)
                            }
                        }
                    }
                }
                return byteMatrix
            } catch (e: WriterException) {
                e.printStackTrace()
            }

            return null
        }

        /**
         * @param contents             Contents to encode.
         * @param errorCorrectionLevel ErrorCorrectionLevel
         * @return QR code object.
         * @throws WriterException Refer to the messages below.
         */
        @Throws(WriterException::class)
        private fun getProtoQrCode(contents: String, errorCorrectionLevel: ErrorCorrectionLevel,
                                   version: Int): QRCode {
            if (contents.isEmpty()) {
                throw IllegalArgumentException("Found empty content.")
            }
            val hintMap = Hashtable<EncodeHintType, Any>()
            hintMap[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hintMap[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
            hintMap[EncodeHintType.QR_VERSION] = version
            return Encoder.encode(contents, errorCorrectionLevel, hintMap)
        }

        private fun isTypeAGN(x: Int, y: Int, agnCenter: IntArray, edgeOnly: Boolean): Boolean {
            if (agnCenter.isEmpty()) return false
            val edgeCenter = agnCenter[agnCenter.size - 1]
            for (agnY in agnCenter) {
                for (agnX in agnCenter) {
                    if (edgeOnly && agnX != 6 && agnY != 6 && agnX != edgeCenter && agnY != edgeCenter)
                        continue
                    if (agnX == 6 && agnY == 6 || agnX == 6 && agnY == edgeCenter || agnY == 6 && agnX == edgeCenter)
                        continue
                    if (x >= agnX - 2 && x <= agnX + 2 && y >= agnY - 2 && y <= agnY + 2)
                        return true
                }
            }
            return false
        }

        private fun isTypePOS(x: Int, y: Int, size: Int, inner: Boolean): Boolean {
            return if (inner) {
                x < 7 && (y < 7 || y >= size - 7) || x >= size - 7 && y < 7
            } else {
                x <= 7 && (y <= 7 || y >= size - 8) || x >= size - 8 && y <= 7
            }
        }

        private fun isTypeTMG(x: Int, y: Int, size: Int): Boolean {
            return y == 6 && x >= 8 && x < size - 8 || x == 6 && y >= 8 && y < size - 8
        }

        private fun scaleBitmap(src: Bitmap, dst: Bitmap) {
            val cPaint = Paint()
            cPaint.isAntiAlias = true
            cPaint.isDither = true
            cPaint.isFilterBitmap = true

            val ratioX = dst.width / src.width.toFloat()
            val ratioY = dst.height / src.height.toFloat()
            val middleX = dst.width * 0.5f
            val middleY = dst.height * 0.5f

            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(dst)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(src, middleX - src.width / 2,
                    middleY - src.height / 2, cPaint)
        }

        private fun getDominantColor(bitmap: Bitmap): Int {
            val newBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
            var red = 0
            var green = 0
            var blue = 0
            var c = 0
            var r: Int
            var g: Int
            var b: Int
            for (y in 0 until newBitmap.height) {
                for (x in 0 until newBitmap.height) {
                    val color = newBitmap.getPixel(x, y)
                    r = color shr 16 and 0xFF
                    g = color shr 8 and 0xFF
                    b = color and 0xFF
                    if (r > 200 || g > 200 || b > 200) continue
                    red += r
                    green += g
                    blue += b
                    c++
                }
            }
            newBitmap.recycle()
            if (c == 0) {
                // got a bitmap with no pixels in it
                // avoid the "divide by zero" error
                // but WHO DARES GIMME AN EMPTY BITMAP?
                return -0x1000000
            } else {
                red = Math.max(0, Math.min(0xFF, red / c))
                green = Math.max(0, Math.min(0xFF, green / c))
                blue = Math.max(0, Math.min(0xFF, blue / c))

                val hsv = FloatArray(3)
                Color.RGBToHSV(red, green, blue, hsv)
                hsv[2] = Math.max(hsv[2], 0.7f)

                return 0xFF shl 24 or Color.HSVToColor(hsv) // (0xFF << 24) | (red << 16) | (green << 8) | blue;
            }
        }

        // returns [finalBoundingRect, newClippingRect]
        private fun scaleImageBoundingRectByClippingRect(bitmap: Bitmap, size: Int, clippingRect: Rect?): Array<Rect> {
            if (clippingRect == null) return scaleImageBoundingRectByClippingRect(bitmap, size, Rect(0, 0, bitmap.width, bitmap.height))
            if (clippingRect.width() != clippingRect.height() || clippingRect.width() <= size) {
                return arrayOf(Rect(0, 0, bitmap.width, bitmap.height), clippingRect)
            }
            val clippingSize = clippingRect.width().toFloat()
            val scalingRatio = size / clippingSize
            return arrayOf(
                RectUtils.round(RectF(
                    0f, 0f,
                    bitmap.width * scalingRatio, bitmap.height * scalingRatio)
                ),
                RectUtils.round(RectF(
                    clippingRect.left * scalingRatio, clippingRect.top * scalingRatio,
                    clippingRect.right * scalingRatio, clippingRect.bottom * scalingRatio)
                )
            )
        }
    }
}