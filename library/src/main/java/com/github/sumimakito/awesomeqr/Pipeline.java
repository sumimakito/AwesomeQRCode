package com.github.sumimakito.awesomeqr;

import android.graphics.Bitmap;

import java.io.File;

public abstract class Pipeline {
    private String errorInfo = null;

    public abstract boolean init(File file);

    public abstract Bitmap nextFrame();

    public abstract void pushRendered(Bitmap bitmap);

    public abstract boolean postRender();

    public abstract boolean release();


    public final void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public final String getErrorInfo() {
        return errorInfo;
    }
}
