package com.ronocod.rickroulette;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ronocod.rickroulette.data.VideoContract.VideoSchema;
import com.ronocod.rickroulette.sync.RickSyncAdapter;

/**
 * A list fragment representing a list of Videos. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link VideoDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Listener}
 * interface.
 */
public class VideoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ;
    private static final String LOG_TAG = VideoListFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Listener listener = Listener.NONE;
    private VideoAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideoListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshData();

        // TODO: replace with a real list adapter.

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null) {
            // restore state
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        listener = (Listener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        listener = Listener.NONE;
    }

    public void onVideoClick(long id) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        listener.onVideoSelected(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new VideoAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.video_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    long id = cursor.getLong(cursor.getColumnIndex(VideoSchema._ID));
                    ((Listener) getActivity()).onVideoSelected(id);
                }
            }
        });
        return rootView;
    }

    private void refreshData() {
        RickSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                VideoSchema.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Listener {
        Listener NONE = new Listener() {
            @Override
            public void onVideoSelected(long id) {
            }
        };

        /**
         * Callback for when an item has been selected.
         */
        void onVideoSelected(long id);
    }


}
