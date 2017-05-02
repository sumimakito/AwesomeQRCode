package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
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

    /**
     * This default value makes data blocks appear smaller.
     */
    private static float DEFAULT_DTA_DOT_SCALE = 0.3f;
    private static int DEFAULT_BINARIZING_THRESHOLD = 128;

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, null, true, true);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight, Bitmap backgroundImage) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage, true, true);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DTA_DOT_SCALE, colorDark, colorLight, backgroundImage, whiteMargin, true);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, false);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, Bitmap backgroundImage, boolean whiteMargin, boolean binarize) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, Color.BLACK, Color.WHITE, backgroundImage, whiteMargin, true, binarize, DEFAULT_BINARIZING_THRESHOLD);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, Bitmap backgroundImage, boolean whiteMargin, boolean binarize, int binarizeThreshold) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, Color.BLACK, Color.WHITE, backgroundImage, whiteMargin, true, binarize, binarizeThreshold);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, false, DEFAULT_BINARIZING_THRESHOLD);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, binarize, DEFAULT_BINARIZING_THRESHOLD);
    }

    /**
     * Create a QR matrix and render it use given configs.
     *
     * @param contents        Contents to encode.
     * @param size            Width as well as the height of the output QR code, includes margin.
     * @param margin          Margin to add around the QR code.
     * @param dataDotScale    Scale the data blocks and makes them appear smaller.
     * @param colorDark       Color of blocks. Will be OVERRIDE by autoColor. (BYTE_DTA, BYTE_POS, BYTE_AGN, BYTE_TMG)
     * @param colorLight      Color of empty space. Will be OVERRIDE by autoColor. (BYTE_EPT)
     * @param backgroundImage The background image to embed in the QR code. If null, no background image will be embedded.
     * @param whiteMargin     If true, background image will not be drawn on the margin area.
     * @param autoColor       If true, colorDark will be set to the dominant color of backgroundImage.
     * @return Bitmap of QR code
     * @throws IllegalArgumentException Refer to the messages below.
     */
    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize, int binarizeThreshold) throws IllegalArgumentException {
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
        return render(byteMatrix, size - 2 * margin, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, binarize, binarizeThreshold);
    }

    private static Bitmap render(ByteMatrix byteMatrix, int innerRenderedSize, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize, int binarizeThreshold) {
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

        if (binarize && backgroundImage != null) {
            int threshold = DEFAULT_BINARIZING_THRESHOLD;
            if (binarizeThreshold > 0 && binarizeThreshold < 255) {
                threshold = binarizeThreshold;
            }
            colorDark = Color.BLACK;
            colorLight = Color.WHITE;
            binarize(backgroundImageScaled, threshold);
        }

        Paint paint = new Paint();
        Paint paintDark = new Paint();
        paintDark.setColor(colorDark);
        paintDark.setAntiAlias(true);
        Paint paintLight = new Paint();
        paintLight.setColor(colorLight);
        paintLight.setAntiAlias(true);
        Paint paintProtector = new Paint();
        paintProtector.setColor(Color.argb(120, 255, 255, 255));
        paintProtector.setAntiAlias(true);

        Canvas canvas = new Canvas(renderedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(backgroundImageScaled, whiteMargin ? margin : 0, whiteMargin ? margin : 0, paint);


        for (int row = 0; row < byteMatrix.getHeight(); row++) {
            String s = "";
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
                        s += "Ｘ";
                        break;
                    case BYTE_DTA:
                        canvas.drawRect(
                                margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                paintDark
                        );
                        s += "〇";
                        break;
                    case BYTE_PTC:
                        canvas.drawRect(
                                margin + col * nWidth,
                                margin + row * nHeight,
                                margin + (col + 1.0f) * nWidth,
                                margin + (row + 1.0f) * nHeight,
                                paintProtector
                        );
                        s += "＋";
                        break;
                    case BYTE_EPT:
                        canvas.drawRect(
                                margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                paintLight
                        );
                        s += "　";
                        break;
                }
            }
            Log.d("QR_MAPPING", s);
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
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
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
}

