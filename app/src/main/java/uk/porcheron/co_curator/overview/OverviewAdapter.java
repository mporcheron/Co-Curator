package uk.porcheron.co_curator.overview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.val.Instance;

/**
 * Created by map on 31/08/15.
 */
public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {
    private static final String TAG = "CC:OverviewAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            textView = (TextView) v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Item i = Instance.items.get(position);
        viewHolder.getTextView().setText(i.getData());
    }

    @Override
    public int getItemCount() {
        return Instance.items.size();
    }
}
