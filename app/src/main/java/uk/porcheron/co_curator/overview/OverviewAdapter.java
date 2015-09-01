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
import uk.porcheron.co_curator.user.User;
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
        return position;
    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        protected final Item mItem;
        protected final User mUser;
        private final View mView;

        public ViewHolder(View v, Item i) {
            super(v);

            mView = v;
            mItem = i;
            mUser = i.getUser();

            int drawable;
            if (mUser.userId == 3) {
                drawable = R.drawable.overview_bg_3;
            } else if (mUser.userId == 2) {
                drawable = R.drawable.overview_bg_2;
            } else if (mUser.userId == 1) {
                drawable = R.drawable.overview_bg_1;
            } else {
                drawable = R.drawable.overview_bg_0;
            }
            mView.setBackground(OverviewActivity.getInstance().getDrawable(drawable));

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItem.simulateTap(OverviewActivity.getInstance());
                }
            });
        }

        abstract void setData(String data);
    }

    public static class NoteHolder extends ViewHolder {
        private final TextView mTextView;

        public NoteHolder(View v, Item i) {
            super(v, i);

            mTextView = (TextView) v.findViewById(R.id.textView);
            mTextView.setLineSpacing(0, 1.2f);

            mTextView.setTextColor(mUser.fgColor);
        }

        public void setData(String data) {
            mTextView.setText(data);
        }
    }

    public static class PhotoHolder extends ViewHolder {
        private final ImageView mImageView;

        public PhotoHolder(View v, Item i) {
            super(v, i);

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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        // Create a new view.

        Item item = mItems.get(position);
        int type = TYPE_NOTE;
        if(item instanceof ItemUrl) {
            type = TYPE_URL;
        } else if(item instanceof ItemPhoto) {
            type = TYPE_PHOTO;
        }

        View v;
        switch(type) {
            case TYPE_NOTE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_note, viewGroup, false);
                return new NoteHolder(v, item);

            case TYPE_PHOTO:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_photo, viewGroup, false);
                return new PhotoHolder(v, item);

            case TYPE_URL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.overview_item_photo, viewGroup, false);
                return new PhotoHolder(v, item);
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
