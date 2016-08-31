package com.mycompany.myapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;


public class Blur {
    private static Context ctx;

    public static void setContext(Context _ctx) {
        ctx = _ctx;
    }

    public static Bitmap DoBlur(Bitmap in, float blur_radius) throws Exception {
        if (ctx == null) {
            Exception e = new Exception("Haven't call Blur.setContext() before any DoBlur*() was called.");
            e.fillInStackTrace();
            throw e;
        }
        //fix or jump end;
        blur_radius = Math.max(Math.min(blur_radius, 25.0f), 0);
        if (blur_radius == 0)
            return in;

        Bitmap out = Bitmap.createBitmap(in.getWidth(), in.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur bs = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation allIn = Allocation.createFromBitmap(rs, in);
        Allocation allOut = Allocation.createFromBitmap(rs, out);
        //Radius must among in 0 to 25.or Boooooooom cause throw exception.
        bs.setRadius(blur_radius);
        bs.setInput(allIn);
        bs.forEach(allOut);

        allOut.copyTo(out);

        in.recycle();
        rs.destroy();

        return out;


    }

    //for more higher level blur,But call BlurD
    public static Bitmap DoBlurWithScale(Bitmap in, float radius, float scale_size) throws Exception {
        Bitmap tmp;
        if (scale_size <= 0) {
            Exception e = new Exception("scale_size cannot be negatives or zero float!");
            e.fillInStackTrace();
            throw e;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale_size, scale_size);
        tmp = Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), matrix, true);
        tmp = DoBlur(tmp, radius);
        matrix = new Matrix();
        matrix.postScale(1 / scale_size, 1 / scale_size);
        tmp = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), matrix, true);
        return tmp;
    }
}
