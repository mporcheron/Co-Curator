package uk.porcheron.co_curator.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import uk.porcheron.co_curator.R;

/**
 * Created by map on 10/08/15.
 */
public class TimelineAdapter extends CursorAdapter {
    public TimelineAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_timelime, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        // Find fields to populate in inflated template
//        TextView tvBody = (TextView) view.findViewById(R.id.tvBody);
//        TextView tvPriority = (TextView) view.findViewById(R.id.tvPriority);
//        // Extract properties from cursor
//        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
//        int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
//        // Populate fields with extracted properties
//        tvBody.setText(body);
//        tvPriority.setText(String.valueOf(priority));
    }
}
