package uk.porcheron.co_curator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * From http://stackoverflow.com/questions/7693633/android-image-dialog-popup
 *
 * Created by map on 11/08/15.
 */
public class ImageDialogActivity extends Activity {
    public static final String IMAGE = "IMAGE";

    private ImageView mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_image);

        Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra(IMAGE);

        mDialog = (ImageView)findViewById(R.id.your_image);
        mDialog.setImageBitmap(bitmap);
        mDialog.setClickable(true);

        //finish the activity (dismiss the image dialog) if the user clicks
        //anywhere on the image
//        mDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

    }
}