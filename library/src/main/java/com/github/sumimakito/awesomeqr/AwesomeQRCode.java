package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class AwesomeQRCode {
    /**
     * For more information about QR code, refer to: https://en.wikipedia.org/wiki/QR_code
     * BYTE_EPT: Empty block
     * BYTE_DTA: Data block
     * BYTE_POS: Position block
     * BYTE_AGN: Align block
     * BYTE_TMG: Timing block
     * BYTE_PTC: Protector block, translucent layer (custom block, this is not included in QR code's standards)
     */
    private static final int BYTE_EPT = 0x0;
    private static final int BYTE_DTA = 0x1;
    private static final int BYTE_POS = 0x2;
    private static final int BYTE_AGN = 0x3;
    private static final int BYTE_TMG = 0x4;
    private static final int BYTE_PTC = 0x5;

    private static float DEFAULT_DTA_DOT_SCALE = 0.3f;
    private static float DEFAULT_LOGO_SCALE = 0.2f;
    private static int DEFAULT_MARGIN = 20;
    private static int DEFAULT_LOGO_MARGIN = 10;
    private static int DEFAULT_LOGO_RADIUS = 8;
    private static int DEFAULT_BINARIZING_THRESHOLD = 128;

    /**
     * Create a QR matrix and render it use given configs.
     *
     * @param contents          Contents to encode.
     * @param size              Width as well as the height of the output QR code, includes margin.
     * @param margin            Margin to add around the QR code.
     * @param dataDotScale      Scale the data blocks and makes them appear smaller.
     * @param colorDark         Color of blocks. Will be OVERRIDE by autoColor. (BYTE_DTA, BYTE_POS, BYTE_AGN, BYTE_TMG)
     * @param colorLight        Color of empty space. Will be OVERRIDE by autoColor. (BYTE_EPT)
     * @param backgroundImage   The background image to embed in the QR code. If null, no background image will be embedded.
     * @param whiteMargin       If true, background image will not be drawn on the margin area.
     * @param autoColor         If true, colorDark will be set to the dominant color of backgroundImage.
     * @param binarize          If true, all images will be binarized while rendering. Default is false.
     * @param binarizeThreshold Threshold value used while binarizing. Default is 128. 0 < threshold < 255.
     * @param roundedDataDots   If true, data blocks will appear as filled circles. Default is false.
     * @param logoImage         The logo image which appears at the center of the QR code. Null to disable.
     * @param logoMargin        The margin around the logo image. 0 to disable.
     * @param logoCornerRadius  The radius of logo image's corners. 0 to disable.
     * @param logoScale         Logo's size = logoScale * innerRenderSize
     * @return Bitmap of QR code
     * @throws IllegalArgumentException Refer to the messages below.
     */
    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark,
                                int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor,
                                boolean binarize, int binarizeThreshold, boolean roundedDataDots,
                                Bitmap logoImage, int logoMargin, int logoCornerRadius, float logoScale) throws IllegalArgumentException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Error: contents is empty. (contents.isEmpty())");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Error: a negative size is given. (size < 0)");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("Error: a negative margin is given. (margin < 0)");
        }
        if (size - 2 * margin <= 0) {
            throw new IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * margin <= 0)");
        }
        ByteMatrix byteMatrix = getBitMatrix(contents);
        if (size - 2 * margin < byteMatrix.getWidth()) {
            throw new IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * margin < " + byteMatrix.getWidth() + ")");
        }
        if (dataDotScale < 0 || dataDotScale > 1) {
            throw new IllegalArgumentException("Error: an illegal data dot scale is given. (dataDotScale < 0 || dataDotScale > 1)");
        }
        return render(byteMatrix, size - 2 * margin, margin, dataDotScale, colorDark, colorLight, backgroundImage,
                whiteMargin, autoColor, binarize, binarizeThreshold, roundedDataDots, logoImage, logoMargin,
                logoCornerRadius, logoScale);
    }

    private static Bitmap render(ByteMatrix byteMatrix, int innerRenderedSize, int margin, float dataDotScale,
                                 int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin,
                                 boolean autoColor, boolean binarize, int binarizeThreshold, boolean roundedDataDots,
                                 Bitmap logoImage, int logoMargin, int logoCornerRadius, float logoScale) {
        int nCount = byteMatrix.getWidth();
        float nWidth = (float) innerRenderedSize / nCount;
        float nHeight = (float) innerRenderedSize / nCount;

        Bitmap backgroundImageScaled = Bitmap.createBitmap(
                innerRenderedSize + (whiteMargin ? 0 : margin * 2),
                innerRenderedSize + (whiteMargin ? 0 : margin * 2),
                Bitmap.Config.ARGB_8888);
        if (backgroundImage != null) {
            scaleBitmap(backgroundImage, backgroundImageScaled);
        }

        Bitmap renderedBitmap = Bitmap.createBitmap(innerRenderedSize + margin * 2, innerRenderedSize + margin * 2, Bitmap.Config.ARGB_8888);

        if (autoColor && backgroundImage != null) {
            colorDark = getDominantColor(backgroundImage);
        }

        int binThreshold = DEFAULT_BINARIZING_THRESHOLD;
        if (binarize) {
            if (binarizeThreshold > 0 && binarizeThreshold < 255) {
                binThreshold = binarizeThreshold;
            }
            colorDark = Color.BLACK;
            colorLight = Color.WHITE;
            if (backgroundImage != null)
                binarize(backgroundImageScaled, binThreshold);
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Paint paintDark = new Paint();
        paintDark.setColor(colorDark);
        paintDark.setAntiAlias(true);
        paintDark.setStyle(Paint.Style.FILL);
        Paint paintLight = new Paint();
        paintLight.setColor(colorLight);
        paintLight.setAntiAlias(true);
        paintLight.setStyle(Paint.Style.FILL);
        Paint paintProtector = new Paint();
        paintProtector.setColor(Color.argb(120, 255, 255, 255));
        paintProtector.setAntiAlias(true);
        paintProtector.setStyle(Paint.Style.FILL);

        Canvas canvas = new Canvas(renderedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(backgroundImageScaled, whiteMargin ? margin : 0, whiteMargin ? margin : 0, paint);


        for (int row = 0; row < byteMatrix.getHeight(); row++) {
            for (int col = 0; col < byteMatrix.getWidth(); col++) {
                switch (byteMatrix.get(col, row)) {
                    case BYTE_AGN:
                    case BYTE_POS:
                    case BYTE_TMG:
                        canvas.drawRect(
                                margin + col * nWidth,
                                margin + row * nHeight,
                                margin + (col + 1.0f) * nWidth,
                                margin + (row + 1.0f) * nHeight,
                                paintDark
                        );
                        break;
                    case BYTE_DTA:
                        if (roundedDataDots) {
                            canvas.drawCircle(
                                    margin + (col + 0.5f) * nWidth,
                                    margin + (row + 0.5f) * nHeight,
                                    dataDotScale * nHeight * 0.5f,
                                    paintDark
                            );
                        } else {
                            canvas.drawRect(
                                    margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                    margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                    margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                    margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                    paintDark
                            );
                        }
                        break;
                    case BYTE_PTC:
                        canvas.drawRect(
                                margin + col * nWidth,
                                margin + row * nHeight,
                                margin + (col + 1.0f) * nWidth,
                                margin + (row + 1.0f) * nHeight,
                                paintProtector
                        );
                        break;
                    case BYTE_EPT:
                        if (roundedDataDots) {
                            canvas.drawCircle(
                                    margin + (col + 0.5f) * nWidth,
                                    margin + (row + 0.5f) * nHeight,
                                    dataDotScale * nHeight * 0.5f,
                                    paintLight
                            );
                        } else {
                            canvas.drawRect(
                                    margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                    margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                    margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                    margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                    paintLight
                            );
                        }
                        break;
                }
            }
        }

        if (logoImage != null) {
            if (logoScale <= 0 || logoScale >= 1) {
                logoScale = DEFAULT_LOGO_SCALE;
            }
            if (logoMargin < 0 || logoMargin * 2 >= innerRenderedSize) {
                logoMargin = DEFAULT_LOGO_MARGIN;
            }
            int logoScaledSize = (int) (innerRenderedSize * logoScale);

            if (logoCornerRadius < 0) logoCornerRadius = 0;
            if (logoCornerRadius * 2 > logoScaledSize)
                logoCornerRadius = (int) (logoScaledSize * 0.5);

            Bitmap logoScaled = Bitmap.createScaledBitmap(logoImage, logoScaledSize, logoScaledSize, true);
            Bitmap logoOpt = Bitmap.createBitmap(logoScaled.getWidth(), logoScaled.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas logoCanvas = new Canvas(logoOpt);
            final Rect logoRect = new Rect(0, 0, logoScaled.getWidth(), logoScaled.getHeight());
            final RectF logoRectF = new RectF(logoRect);
            Paint logoPaint = new Paint();
            logoPaint.setAntiAlias(true);
            logoPaint.setColor(0xFFFFFFFF);
            logoPaint.setStyle(Paint.Style.FILL);
            logoCanvas.drawARGB(0, 0, 0, 0);
            logoCanvas.drawRoundRect(logoRectF, logoCornerRadius, logoCornerRadius, logoPaint);
            logoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            logoCanvas.drawBitmap(logoScaled, logoRect, logoRect, logoPaint);
            logoPaint.setColor(colorLight);
            logoPaint.setStyle(Paint.Style.STROKE);
            logoPaint.setStrokeWidth(logoMargin);
            logoCanvas.drawRoundRect(logoRectF, logoCornerRadius, logoCornerRadius, logoPaint);

            if (binarize)
                binarize(logoOpt, binThreshold);

            canvas.drawBitmap(logoOpt, (int) (0.5 * (renderedBitmap.getWidth() - logoOpt.getWidth())),
                    (int) (0.5 * (renderedBitmap.getHeight() - logoOpt.getHeight())), paint);
        }

        return renderedBitmap;
    }

    private static ByteMatrix getBitMatrix(String contents) {
        try {
            QRCode qrCode = getProtoQRCode(contents, ErrorCorrectionLevel.H);
            int agnCenter[] = qrCode.getVersion().getAlignmentPatternCenters();
            ByteMatrix byteMatrix = qrCode.getMatrix();
            int matSize = byteMatrix.getWidth();
            for (int row = 0; row < matSize; row++) {
                for (int col = 0; col < matSize; col++) {
                    if (isTypeAGN(col, row, agnCenter, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_AGN);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypePOS(col, row, matSize, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_POS);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypeTMG(col, row, matSize)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_TMG);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }

                    if (isTypePOS(col, row, matSize, false)) {
                        if (byteMatrix.get(col, row) == BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }
                }
            }
            return byteMatrix;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param contents             Contents to encode.
     * @param errorCorrectionLevel ErrorCorrectionLevel
     * @return QR code object.
     * @throws WriterException Refer to the messages below.
     */
    private static QRCode getProtoQRCode(String contents, ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }
        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        return Encoder.encode(contents, errorCorrectionLevel, hintMap);
    }

    private static boolean isTypeAGN(int x, int y, int[] agnCenter, boolean edgeOnly) {
        if (agnCenter.length == 0) return false;
        int edgeCenter = agnCenter[agnCenter.length - 1];
        for (int agnY : agnCenter) {
            for (int agnX : agnCenter) {
                if (edgeOnly && agnX != 6 && agnY != 6 && agnX != edgeCenter && agnY != edgeCenter)
                    continue;
                if ((agnX == 6 && agnY == 6) || (agnX == 6 && agnY == edgeCenter) || (agnY == 6 && agnX == edgeCenter))
                    continue;
                if (x >= agnX - 2 && x <= agnX + 2 && y >= agnY - 2 && y <= agnY + 2)
                    return true;
            }
        }
        return false;
    }

    private static boolean isTypePOS(int x, int y, int size, boolean inner) {
        if (inner) {
            return ((x < 7 && (y < 7 || y >= size - 7)) || (x >= size - 7 && y < 7));
        } else {
            return ((x <= 7 && (y <= 7 || y >= size - 8)) || (x >= size - 8 && y <= 7));
        }
    }

    private static boolean isTypeTMG(int x, int y, int size) {
        return ((y == 6 && (x >= 8 && x < size - 8)) || (x == 6 && (y >= 8 && y < size - 8)));
    }

    private static void scaleBitmap(Bitmap src, Bitmap dst) {
        Paint cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);
        cPaint.setFilterBitmap(true);

        float ratioX = dst.getWidth() / (float) src.getWidth();
        float ratioY = dst.getHeight() / (float) src.getHeight();
        float middleX = dst.getWidth() * 0.5f;
        float middleY = dst.getHeight() * 0.5f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(dst);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(src, middleX - src.getWidth() / 2,
                middleY - src.getHeight() / 2, cPaint);
    }

    private static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true);
        int red = 0, green = 0, blue = 0, c = 0;
        int r, g, b;
        for (int y = 0; y < newBitmap.getHeight(); y++) {
            for (int x = 0; x < newBitmap.getHeight(); x++) {
                int color = newBitmap.getPixel(x, y);
                r = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                b = color & 0xFF;
                if (r > 200 || g > 200 || b > 200) continue;
                red += r;
                green += g;
                blue += b;
                c++;
            }
        }
        newBitmap.recycle();
        red = Math.max(0, Math.min(0xFF, red / c));
        green = Math.max(0, Math.min(0xFF, green / c));
        blue = Math.max(0, Math.min(0xFF, blue / c));
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    private static void binarize(Bitmap bitmap, int threshold) {
        int r, g, b;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getHeight(); x++) {
                int color = bitmap.getPixel(x, y);
                r = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                b = color & 0xFF;
                float sum = 0.30f * r + 0.59f * g + 0.11f * b;
                bitmap.setPixel(x, y, sum > threshold ? Color.WHITE : Color.BLACK);
            }
        }
    }

    /* TONS OF SHORTENED METHODS */

    public static Bitmap create(String contents, int size, int margin) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, 0, 0, null, true, true, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, null, true, false, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, Bitmap backgroundImage) throws IllegalArgumentException {
        return create(contents, size, DEFAULT_MARGIN, DEFAULT_DTA_DOT_SCALE, 0, 0, backgroundImage, true, true, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, Bitmap backgroundImage, Bitmap logoImage) throws IllegalArgumentException {
        return create(contents, size, DEFAULT_MARGIN, DEFAULT_DTA_DOT_SCALE, 0, 0, backgroundImage, true, true, false, 0, false, logoImage, DEFAULT_LOGO_MARGIN, DEFAULT_LOGO_RADIUS, DEFAULT_LOGO_SCALE);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark,
                                int colorLight, Bitmap backgroundImage) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage,
                true, false, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int colorDark,
                                int colorLight, Bitmap backgroundImage, Bitmap logoImage) throws IllegalArgumentException {
        return create(contents, size, DEFAULT_MARGIN, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage, true, false, false, 0,
                false, logoImage, DEFAULT_LOGO_MARGIN, DEFAULT_LOGO_RADIUS, DEFAULT_LOGO_SCALE);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark,
                                int colorLight, Bitmap backgroundImage, Bitmap logoImage) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage, true, false, false, 0,
                false, logoImage, DEFAULT_LOGO_MARGIN, DEFAULT_LOGO_RADIUS, DEFAULT_LOGO_SCALE);
    }

    public static Bitmap create(String contents, int size, int margin, Bitmap backgroundImage) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, 0, 0, backgroundImage,
                true, true, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, Bitmap backgroundImage, boolean whiteMargin) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, 0, 0, backgroundImage,
                whiteMargin, true, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, Bitmap backgroundImage, boolean whiteMargin, boolean roundedDataDots) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, 0, 0, backgroundImage,
                whiteMargin, true, false, 0, roundedDataDots, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin)
            throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage,
                whiteMargin, false, false, 0, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark,
                                int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor,
                                boolean roundedDataDots) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage,
                whiteMargin, autoColor, false, 0, roundedDataDots, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark,
                                int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor,
                                boolean binarize, int binarizeThreshold) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage,
                whiteMargin, autoColor, binarize, binarizeThreshold, false, null, 0, 0, 0);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark,
                                int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor,
                                boolean binarize, int binarizeThreshold, boolean roundedDataDots) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage,
                whiteMargin, autoColor, binarize, binarizeThreshold, roundedDataDots, null, 0, 0, 0);
    }
}

