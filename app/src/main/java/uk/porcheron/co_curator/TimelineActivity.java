package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.FileOutputStream;
import java.util.List;

import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.util.IData;

public class TimelineActivity extends Activity implements View.OnLongClickListener, SurfaceHolder.Callback {
    private static final String TAG = "CC:TimelineActivity";

    private SharedPreferences mSharedPreferences;

    private DbHelper mDbHelper;
    private ProgressDialog mProgressDialog;
    private SurfaceView mSurface;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    public static final int PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        int globalUserId = mSharedPreferences.getInt(getString(R.string.pref_globalUserId), -1);
        int userId = mSharedPreferences.getInt(getString(R.string.pref_userId), -1);
        int groupId = mSharedPreferences.getInt(getString(R.string.pref_groupId), -1);
        if(globalUserId >= 0 && userId >= 0 && groupId >= 0) {
            IData.globalUserId = globalUserId;
            IData.userId = userId;
            IData.groupId = groupId;
        } else {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
            finish();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timelime);

        showLoadingDialog(R.string.dialog_loading);
        Style.loadStyleAttrs(this);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mSurface.getHolder().addCallback(this);

        mDbHelper = new DbHelper(this);
        IData.users = new UserList(this, mSurface);
        IData.items = new ItemList(this, mLayoutAbove, null, null, mLayoutBelow);

        mLayoutAbove.setPadding(0, 0, 0, Style.layoutHalfPadding);
        mLayoutBelow.setPadding(0, Style.layoutHalfPadding, 0, 0);

        mLayoutAbove.setOnLongClickListener(this);
        mLayoutBelow.setOnLongClickListener(this);

        new DbLoader(this).execute();
    }

    @Override
    public boolean onLongClick(View v) {
        promptNewItem(v, IData.items.isEmpty());
        return true;
    }

    public void showLoadingDialog(int str) {
        mProgressDialog = ProgressDialog.show(this, "", getText(str), true);
    }

    public void hideLoadingDialog() {
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

            showLoadingDialog(R.string.dialog_adding_image);

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

            try {
                return Image.save(TimelineActivity.this, bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save image");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(!IData.items.add(IData.items.size(), ItemType.PHOTO, IData.user(), result, true, true)) {
                Log.e(TAG, "Failed to save photo");
            }

            hideLoadingDialog();

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
                        if (!IData.items.add(IData.items.size(), ItemType.NOTE, IData.user(), text, true, true)) {
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

                        if(!IData.items.add(IData.items.size(), ItemType.URL, IData.user(), text, true, true)) {
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


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Redaw trunk");

        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Style.backgroundColor);

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        for(int i = Style.userLayers.length - 1; i >= 0; i--) {
            User user = IData.users.get(i);

            int y1 = (int) (((h - Style.layoutCentreHeight) / 2) + user.centrelineOffset);
            int y2 = (int) (y1 + Style.lineWidth);

            canvas.drawRect(0, y1, w, y2, user.bgPaint);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
