package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.util.UData;

public class TimelineActivity extends Activity implements View.OnLongClickListener {
    private static final String TAG = "CC:TimelineActivity";

    private boolean mCreated = false;

    private DbHelper mDbHelper;
    private ProgressDialog mProgressDialog;
    private SurfaceView mSurface;
    private SurfaceView mStemSurface;
    private LinearLayout mLayoutAbove;
    private RelativeLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    private String mNewImagePath;

    public static final int PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mCreated) {
            return;
        }
        mCreated = true;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timelime);

        mProgressDialog = ProgressDialog.show(this, "", getText(R.string.dialog_loading), true);

        Style.loadStyleAttrs(this);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mStemSurface = (SurfaceView) findViewById(R.id.stemSurface);
        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutCentre = (RelativeLayout) findViewById(R.id.layoutCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mDbHelper = new DbHelper(this);
        UData.users = new UserList(this, mSurface);
        UData.items = new ItemList(this, mLayoutAbove, mLayoutCentre, mStemSurface, mLayoutBelow);

        mLayoutAbove.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutCentre.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutBelow.setPadding(Style.layoutBelowPadX, 0, 0, 0);

        mSurface.setOnLongClickListener(this);
        mLayoutAbove.setOnLongClickListener(this);
        mLayoutCentre.setOnLongClickListener(this);
        mLayoutBelow.setOnLongClickListener(this);

        new DbLoader(this).execute("Go");
    }

    @Override
    public boolean onLongClick(View v) {
        promptNewItem(v, UData.items.isEmpty());
        return true;
    }

    public void finishLoading() {
        mProgressDialog.hide();
    }

    public void promptAdd() {
        onLongClick(mLayoutAbove);
    }

    public DbHelper getDbHelper() {
        return mDbHelper;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Received ActivityResult (requestCode=" + requestCode + ",resultCode=" + resultCode + ")");


        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == PICK_IMAGE) {
            if (data == null) {
                Log.e(TAG, "No data retrieved...");
                return;
            }


            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Log.d(TAG, "File selected by user: " + filePath);

            new ImportImage().execute(filePath);

        }
    }

    private class ImportImage extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
            String filename = UData.globalUserId + "-" + UData.userId + "-" + System.currentTimeMillis() +".png";

            try {
                final FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);

                if(!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    Log.e(TAG, "Could not save bitmap locally");
                }

                fos.close();
                return filename;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(!UData.items.add(UData.items.size(), ItemType.PHOTO, UData.user(), result)) {
                Log.e(TAG, "Failed to save photo");
            }

        }
    }

    public void promptNewItem(final View view, final boolean forceAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
        builder.setTitle(R.string.dialog_add_message);

        if(forceAdd) {
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    promptNewItem(view, true);
                }
            });
        }

        final ItemType[] types = ItemType.values();
        CharSequence[] typeLabels = new CharSequence[types.length];
        for (int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = getString(types[i].getLabel());
        }

        builder.setItems(typeLabels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemType type = types[which];
                switch (type) {
                    case PHOTO:
                        addNewPhoto();
                        break;

                    case NOTE:
                        addNewNote(view, forceAdd);
                        break;

                    case URL:
                        addNewUrl(view, forceAdd);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNewNote(final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(TimelineActivity.this);
        editText.setSingleLine(false);

        AlertDialog dialog = new AlertDialog.Builder(TimelineActivity.this)
                .setTitle(getString(R.string.dialog_note_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_note_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if (!UData.items.add(UData.items.size(), ItemType.NOTE, UData.user(), text)) {
                            promptNewItem(view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_note_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (promptOnCancel) {
                            promptNewItem(view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void addNewUrl(final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(TimelineActivity.this);
        editText.setSingleLine(true);
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);

        AlertDialog dialog = new AlertDialog.Builder(TimelineActivity.this)
                .setTitle(getString(R.string.dialog_url_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_url_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();

                        String insertUrl = text;
                        if(!text.startsWith("http")) {
                            insertUrl = "http://" + text;
                        }

                        if(!UData.items.add(UData.items.size(), ItemType.URL, UData.user(), text)) {
                            promptNewItem(view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_url_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(promptOnCancel) {
                            promptNewItem(view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void addNewPhoto() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(pickIntent, TimelineActivity.PICK_IMAGE);
    }
}
