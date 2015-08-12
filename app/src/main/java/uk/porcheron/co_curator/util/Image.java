package uk.porcheron.co_curator.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by map on 12/08/15.
 */
public class Image {
    private static final String TAG = "CC:Image";

    public static String save(Context context, Bitmap bitmap) throws IOException, IllegalArgumentException {
        String filename = IData.globalUserId + "-" + IData.userId + "-" + System.currentTimeMillis();

        int thumbWidth = (int) (Style.imageWidth - 2 * Style.imagePadding);
        int thumbHeight = (int) (Style.imageHeight - 2 * Style.imagePadding);

        //float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        int width = (int) (bitmap.getWidth());// * Style.imageThumbScaleBy);
        int height = (int) (bitmap.getHeight());// * Style.imageThumbScaleBy);

        //Log.v(TAG, "Scale the image from (" + bitmap.getWidth() + "," + bitmap.getHeight() + ") to (" + width + "," + height + ")");
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        int x = (int) ((width / 2f) - (thumbWidth / 2f));
        int y = (int) ((height / 2f) - (thumbHeight / 2f));

        Log.v(TAG, "Create the thumbnail image (" + x + "," + y + ") (" + width + "," + height + ")");
        Bitmap thumbnail = Bitmap.createBitmap(bitmap, x, y, (int) thumbWidth, (int) thumbHeight);

        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
        if(!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fos.close();

        final FileOutputStream fosThumb = context.openFileOutput(filename + "-thumb.png", Context.MODE_PRIVATE);
        if(!thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fosThumb)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fosThumb.close();
        return filename;
    }

}
