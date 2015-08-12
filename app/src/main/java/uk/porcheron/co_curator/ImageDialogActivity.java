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

/**
 * From http://stackoverflow.com/questions/7693633/android-image-dialog-popup
 *
 * Created by map on 11/08/15.
 */
public class ImageDialogActivity extends Activity {
    private static final String TAG = "CC:ImageDialogActivity";

    public static final String IMAGE = "IMAGE";

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
            FileInputStream fis = openFileInput(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);

            Log.d(TAG, "Bitmap Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            mDialog = (ImageView)findViewById(R.id.image);
            mDialog.setMinimumHeight(bitmap.getWidth());
            mDialog.setMinimumHeight(bitmap.getHeight());
            mDialog.setImageBitmap(bitmap);
            mDialog.setClickable(true);

            //finish the activity (dismiss the image dialog) if the user clicks
            //anywhere on the image
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}