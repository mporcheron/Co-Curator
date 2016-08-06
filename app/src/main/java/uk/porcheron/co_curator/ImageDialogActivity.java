package uk.porcheron.co_curator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import uk.porcheron.co_curator.val.Phone;

/**
 * From http://stackoverflow.com/questions/7693633/android-image-dialog-popup
 *
 * Created by map on 11/08/15.
 */
public class ImageDialogActivity extends Activity {
    private static final String TAG = "CC:ImageDialogActivity";

    public static final String IMAGE = "IMAGE";

    private Bitmap mBitmap;
    private ImageView mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_dialog_image);

        String imagePath = (String) getIntent().getStringExtra(IMAGE);

        try {
            FileInputStream fis = openFileInput(imagePath + ".png");
            mBitmap = BitmapFactory.decodeStream(fis);

            mDialog = (ImageView) findViewById(R.id.image);
            mDialog.setImageBitmap(mBitmap);
            mDialog.setClickable(true);
            mDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch(FileNotFoundException e) {
            Log.e(TAG, "Could not open image " + imagePath);
            finish();
        }
    }

    public void onResume() {
        super.onResume();

        Phone.collectAttrs();

        int padX = (Phone.screenWidth - mBitmap.getWidth()) / 2;
        int padY = (Phone.screenHeight - mBitmap.getHeight()) / 2;
        mDialog.setPadding(padX, padY, padX, padY);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}