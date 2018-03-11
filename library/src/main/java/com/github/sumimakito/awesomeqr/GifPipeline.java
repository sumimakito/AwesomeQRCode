package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

public class GifPipeline extends Pipeline {
    private GifDecoder gifDecoder;
    private LinkedList<Bitmap> frameSequence;
    private int currentFrame = 0;
    private File outputFile;
    private RectF cropRect;

    @Override
    public boolean init(File file) {
        if (!file.exists()) {
            setErrorInfo("ENOENT: File does not exist.");
            return false;
        } else if (file.isDirectory()) {
            setErrorInfo("EISDIR: Target is a directory.");
            return false;
        }
        gifDecoder = new GifDecoder();
        boolean isSucceeded = gifDecoder.load(file.getAbsolutePath());
        if (!isSucceeded) {
            setErrorInfo("Failed to decode input file as GIF.");
            return false;
        }
        frameSequence = new LinkedList<>();
        return true;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setCropRect(RectF cropRect) {
        this.cropRect = cropRect;
    }

    @Override
    public Bitmap nextFrame() {
        if (gifDecoder.frameNum() == 0) {
            setErrorInfo("GIF contains zero frames.");
            return null;
        }
        if (currentFrame < gifDecoder.frameNum()) {
            Bitmap frame = gifDecoder.frame(currentFrame);
            currentFrame++;
            if (cropRect != null) {
                Bitmap cropped = Bitmap.createBitmap(frame, Math.round(cropRect.left), Math.round(cropRect.top),
                        Math.round(cropRect.width()), Math.round(cropRect.height()));
                frame.recycle();
                return cropped;
            }
            return frame;
        } else return null;
    }

    @Override
    public void pushRendered(Bitmap bitmap) {
        frameSequence.addLast(bitmap);
    }

    @Override
    public boolean postRender() {
        if (outputFile == null) {
            setErrorInfo("Output file is not yet set.");
            return false;
        }

        if (frameSequence.size() == 0) {
            setErrorInfo("Zero frames in the sequence. This is nearly impossible to happen.");
            return false;
        }

        try {
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.init(frameSequence.getFirst().getWidth(), frameSequence.getFirst().getHeight(), outputFile.getAbsolutePath(), GifEncoder.EncodingType.ENCODING_TYPE_FAST);
            int frameIndex = 0;
            while (!frameSequence.isEmpty()) {
                gifEncoder.encodeFrame(frameSequence.removeFirst(), gifDecoder.delay(frameIndex));
            }
            gifEncoder.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            setErrorInfo("FileNotFoundException. See stacktrace for more information.");
            return false;
        }

        return true;
    }

    @Override
    public boolean release() {
        return true;
    }
}
