package com.sketchView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by keshav on 06/04/17.
 */

public class SketchViewContainer extends LinearLayout {

    public SketchView sketchView;

    public SketchViewContainer(Context context) {
        super(context);
        sketchView = new SketchView(context);
        addView(sketchView);
    }

    public SketchFile saveToLocalCache() throws IOException {

        Bitmap viewBitmap = sketchView.getBitMap();

        File cacheFile = File.createTempFile("sketch_", UUID.randomUUID().toString()+".png");
        FileOutputStream imageOutput = new FileOutputStream(cacheFile);
        viewBitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutput);

        SketchFile sketchFile = new SketchFile();
        sketchFile.localFilePath = cacheFile.getAbsolutePath();
        sketchFile.width = viewBitmap.getWidth();
        sketchFile.height = viewBitmap.getHeight();
        return sketchFile;

    }

    public boolean openSketchFile(String localFilePath) {

        Bitmap bitmap = BitmapFactory.decodeFile(localFilePath);
        if(bitmap != null) {
            sketchView.setViewImage(bitmap);
            return true;
        }
        return false;
    }

}
