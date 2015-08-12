package uk.porcheron.co_curator.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileInputStream;
import java.util.Random;

import uk.porcheron.co_curator.ImageDialogActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemImage extends Item {
    private static final String TAG = "CC:ItemImage";

    private TimelineActivity mActivity;
    private Bitmap mBitmap = null;
    private Bitmap mBitmapThumbnail = null;
    private String mImagePath;

    private Random mRandom = new Random();

    public ItemImage(TimelineActivity activity, int itemId, User user) {
        super(activity, user, itemId);

        mActivity = activity;

        setBounds(Style.imageWidth, Style.imageHeight, Style.imagePadding);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF b = getInnerBounds();

        if(mBitmap == null) {
            Log.d(TAG, "Load image file " + mImagePath);
            try{
                FileInputStream fis = mActivity.openFileInput(mImagePath +".png");
                mBitmap = BitmapFactory.decodeStream(fis);
                fis.close();

                if(mBitmap == null) {
                    Log.e(TAG, "Could not decode file to bitmap " + mImagePath);
                }
            } catch(Exception e){
                Log.e(TAG, "Could not open " + mImagePath);
            }


            try{
                FileInputStream fis = mActivity.openFileInput(mImagePath +"-thumb.png");
                mBitmapThumbnail = BitmapFactory.decodeStream(fis);
                fis.close();

                if(mBitmapThumbnail == null) {
                    Log.e(TAG, "Could not decode thumbnail file to bitmap " + mImagePath);
                }
            } catch(Exception e){
                Log.e(TAG, "Could not open " + mImagePath);
            }
        }

        if(mBitmap != null && mBitmapThumbnail != null) {
            canvas.drawBitmap(mBitmapThumbnail, b.left, b.top, getUser().bgPaint);
        } else {
            canvas.drawRect(b, getUser().bgPaint);
        }

    }
    
    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Image clicked!");

        Intent intent = new Intent(mActivity, ImageDialogActivity.class);
        intent.putExtra(ImageDialogActivity.IMAGE, mImagePath);

        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
