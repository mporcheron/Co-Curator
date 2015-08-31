package uk.porcheron.co_curator.overview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import uk.porcheron.co_curator.OverviewActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemPhoto;
import uk.porcheron.co_curator.item.ItemUrl;
import uk.porcheron.co_curator.val.Instance;

/**
 * Created by map on 31/08/15.
 */
public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {
    private static final String TAG = "CC:OverviewAdapter";

    private static final int TYPE_NOTE = 0;
    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_URL = 2;

    private List<Item> mItems;

    public OverviewAdapter() {
        mItems = Instance.items.getAllVisible();
    }

    @Override
    public int getItemViewType(int position) {
        Item item = mItems.get(position);

        if(item instanceof ItemUrl) {
            return TYPE_URL;
        } else if(item instanceof ItemPhoto) {
            return TYPE_PHOTO;
        } else {
            return TYPE_NOTE;
        }
    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }

        abstract void setData(String data);
    }

    public static class NoteHolder extends ViewHolder {
        private final TextView mTextView;

        public NoteHolder(View v) {
            super(v);

            mTextView = (TextView) v.findViewById(R.id.textView);
            mTextView.setLineSpacing(0, 1.2f);
        }

        public void setData(String data) {
            mTextView.setText(data.toUpperCase());
        }
    }

    public static class PhotoHolder extends ViewHolder {
        private final ImageView mImageView;

        public PhotoHolder(View v) {
            super(v);

            mImageView = (ImageView) v.findViewById(R.id.imageView);
        }

        public void setData(String data) {
            try {
                FileInputStream fis = OverviewActivity.getInstance().openFileInput(data + ".png");
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                mImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Could not open image " + data);
            }
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        // Create a new view.

        View v;
        switch(type) {
            case TYPE_NOTE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_note, viewGroup, false);
                return new NoteHolder(v);

            case TYPE_PHOTO:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_photo, viewGroup, false);
                return new PhotoHolder(v);

            case TYPE_URL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_photo, viewGroup, false);
                return new PhotoHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Item i = mItems.get(position);
        viewHolder.setData(i.getData());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
