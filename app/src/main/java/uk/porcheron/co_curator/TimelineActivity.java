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
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;
import uk.porcheron.co_curator.val.Instance;

public class TimelineActivity extends Activity implements View.OnLongClickListener, SurfaceHolder.Callback {
    private static final String TAG = "CC:TimelineActivity";

    private static TimelineActivity mInstance;

    private ProgressDialog mProgressDialog;
    private FrameLayout mFrameLayout;

    public static final int PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check the user has previously authenticated
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        int globalUserId = sharedPrefs.getInt(getString(R.string.pref_globalUserId), -1);
        int userId = sharedPrefs.getInt(getString(R.string.pref_userId), -1);
        int groupId = sharedPrefs.getInt(getString(R.string.pref_groupId), -1);
        if(globalUserId >= 0 && userId >= 0 && groupId >= 0) {
            Instance.globalUserId = globalUserId;
            Instance.userId = userId;
            Instance.groupId = groupId;
        } else {
            Intent intent = new Intent(this, ParticipantActivity.class);
            startActivity(intent);
            finish();
        }

        // Setup the activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_timelime);
        showLoadingDialog(R.string.dialog_loading);

        mInstance = this;

        // Load various static values
        Phone.collectAttrs();
        Style.collectAttrs();

        // Begin preparation for drawing the UI
        SurfaceView mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.getHolder().addCallback(this);

        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        LinearLayout layoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        LinearLayout layoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        layoutAbove.setPadding(0, 0, 0, Style.layoutHalfPadding);
        layoutBelow.setPadding(0, Style.layoutHalfPadding, 0, 0);

        layoutAbove.setOnLongClickListener(this);
        layoutBelow.setOnLongClickListener(this);

        // Load items
        Instance.users = new UserList();
        Instance.items = new ItemList(this, layoutAbove, layoutBelow);

        new DbLoader().execute();
    }

    public static TimelineActivity getInstance() {
        return mInstance;
    }

    @Override
    public boolean onLongClick(View v) {
        promptNewItem(v, Instance.items.isEmpty());
        return true;
    }

    public void showLoadingDialog(int str) {
        mProgressDialog = ProgressDialog.show(this, "", getText(str), true);
    }

    public void hideLoadingDialog() {
        mProgressDialog.hide();
    }

    public void promptAdd() {
        onLongClick(mFrameLayout);
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

            if(!Instance.items.add(Instance.items.size(), ItemType.PHOTO, Instance.user(), result, true, true)) {
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
        CharSequence[] typeLabels = new CharSequence[types.length - 1];
        for (int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = getString(types[i+1].getLabel()); // first item is the UNKNOWN type
        }

        builder.setItems(typeLabels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemType type = types[which+1]; // first item is the UNKNOWN type
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
                        if (!Instance.items.add(Instance.items.size(), ItemType.NOTE, Instance.user(), text, true, true)) {
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

                        if(!Instance.items.add(Instance.items.size(), ItemType.URL, Instance.user(), insertUrl, true, true)) {
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
            User user = Instance.users.get(i);

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
