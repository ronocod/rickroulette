package com.ronocod.rickroulette;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronocod.rickroulette.data.VideoContract.VideoSchema;

/**
 * A fragment representing a single Video detail screen.
 * This fragment is either contained in a {@link VideoListActivity}
 * in two-pane mode (on tablets) or a {@link VideoDetailActivity}
 * on handsets.
 */
public class VideoDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String KEY_URI = "KEY_URI";
    private static final int LOADER_ID = 1;

    private Uri uri;
    private TextView titleText;
    private ShareActionProvider shareActionProvider;

    public VideoDetailFragment() {
    }

    public static VideoDetailFragment create(Uri uri) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(VideoDetailFragment.KEY_URI, uri);
        VideoDetailFragment fragment = new VideoDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(KEY_URI)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            uri = getArguments().getParcelable(KEY_URI);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_detail, container, false);

        // Show the dummy content as text in a TextView.
        titleText = (TextView) rootView.findViewById(R.id.video_detail);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null == uri) {
            return null;
        }
        // Sort order:  Ascending, by date.
        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();

        String title = data.getString(data.getColumnIndex(VideoSchema.COLUMN_TITLE));
        titleText.setText(title);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent(title));

        }

    }

    private Intent createShareIntent(String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " #RickRoulette");
        return shareIntent;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        titleText.setText("No Video");
    }
}
