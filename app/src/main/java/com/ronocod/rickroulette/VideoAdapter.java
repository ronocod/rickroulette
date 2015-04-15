package com.ronocod.rickroulette;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronocod.rickroulette.data.VideoContract.VideoSchema;

/**
 * {@link VideoAdapter} exposes a list of videos
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class VideoAdapter extends CursorAdapter {

    public VideoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    /**
     * Copy/paste note: Replace existing newView() method in VideoAdapter with this one.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_video, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(VideoSchema.COLUMN_TITLE));
        holder.titleText.setText(title);

    }

    public static class ViewHolder {
        public final TextView titleText;

        public ViewHolder(View view) {
            titleText = (TextView) view.findViewById(R.id.video_title_text);
        }
    }
}