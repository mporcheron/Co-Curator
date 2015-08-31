package uk.porcheron.co_curator;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.porcheron.co_curator.overview.OverviewAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class OverviewActivityFragment extends Fragment {
    private static final String TAG = "CC:OverviewFragment";

    protected RecyclerView mRecyclerView;
    protected OverviewAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private static final int SPAN_COUNT = 2;

    public OverviewActivityFragment() {
        Log.d(TAG, "Created fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
        rootView.setTag(TAG);

        // Create recycler view
        mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        mAdapter = new OverviewAdapter();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }
}
