package com.mycompany.myapp.CacheCollection;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mikir on 2016/7/16.
 */

public class BitmapCache extends BaseCache<String,Bitmap> {

    public BitmapCache(){}

    @Override
    public long getSize(Bitmap bitmap) {
        return bitmap.getByteCount();
    }
}
