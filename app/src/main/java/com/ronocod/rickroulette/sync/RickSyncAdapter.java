package com.ronocod.rickroulette.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ronocod.rickroulette.BuildConfig;
import com.ronocod.rickroulette.R;
import com.ronocod.rickroulette.VideoListActivity;
import com.ronocod.rickroulette.data.VideoContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RickSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = RickSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = (int) TimeUnit.HOURS.toSeconds(3);
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final int VIDEO_NOTIFICATION_ID = 3004;


    public RickSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), VideoContract.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = VideoContract.CONTENT_AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        RickSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, VideoContract.CONTENT_AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            final String YOUTUBE_BASE_URL =
                    "https://www.googleapis.com/youtube/v3/videos?";
            final String PARAM_PART = "part";
            final String PARAM_CHART = "chart";
            final String PARAM_KEY = "key";

            Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_PART, "id,snippet")
                    .appendQueryParameter(PARAM_CHART, "mostPopular")
                    .appendQueryParameter(PARAM_KEY, BuildConfig.YOUTUBE_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int status = urlConnection.getResponseCode();
            Log.d(LOG_TAG, "Response code: " + status);
            InputStream inputStream;
            if (status >= 200 && status < 400) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }

            // Read the input stream into a String
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }

            String jsonString = buffer.toString();
            Log.d(LOG_TAG, "JSON: " + jsonString);
            parseJsonString(jsonString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }


    private void parseJsonString(String jsonString) throws JSONException {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonVideos = json.getJSONArray("items");

            Vector<ContentValues> videoValuesVector = new Vector<>(jsonVideos.length());

            for (int i = 0; i < jsonVideos.length(); i++) {

                JSONObject jsonVideo = jsonVideos.getJSONObject(i);

                String videoId = jsonVideo.getString("id");
                JSONObject snippet = jsonVideo.getJSONObject("snippet");
                String title = snippet.getString("title");

                ContentValues videoRow = new ContentValues();

                videoRow.put(VideoContract.VideoSchema.COLUMN_VIDEO_ID, videoId);
                videoRow.put(VideoContract.VideoSchema.COLUMN_TITLE, title);

                videoValuesVector.add(videoRow);
            }

            // add to database
            if (videoValuesVector.size() > 0) {
                ContentValues[] rows = (ContentValues[]) videoValuesVector.toArray();
                ContentResolver resolver = getContext().getContentResolver();
                resolver.delete(VideoContract.VideoSchema.CONTENT_URI, null, null);
                resolver.bulkInsert(VideoContract.VideoSchema.CONTENT_URI, rows);

                createNotification();
            }

            Log.d(LOG_TAG, "Sync Complete. " + videoValuesVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void createNotification() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotifications = prefs.getBoolean("shouldShowNotifications", true);

        if (displayNotifications) {

            long lastSyncTimestamp = prefs.getLong("lastNotificationTimestamp", 0);

            if (System.currentTimeMillis() - lastSyncTimestamp >= TimeUnit.DAYS.toMillis(1)) {

                Resources resources = context.getResources();


                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setColor(resources.getColor(android.R.color.holo_blue_light))
                                .setSmallIcon(android.R.drawable.ic_media_play)
//                                .setLargeIcon(BitmapFactory.decodeResource(resources))
                                .setContentTitle(context.getString(R.string.app_name))
                                .setContentText("New videos!");

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, VideoListActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // VIDEO_NOTIFICATION_ID allows you to update the notification later on.
                notificationManager.notify(VIDEO_NOTIFICATION_ID, notificationBuilder.build());

                //refreshing last sync
                prefs.edit()
                        .putLong("lastNotificationTimestamp", System.currentTimeMillis())
                        .apply();
            }
        }
    }
}