/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ronocod.rickroulette.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class VideoProvider extends ContentProvider {

    static final int VIDEO = 100;
    static final int VIDEO_WITH_ID = 101;
    // The URI Matcher used by this content provider.
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private static final SQLiteQueryBuilder QUERY_BUILDER = new SQLiteQueryBuilder();
    private static final String VIDEO_ID_SELECTION =
            VideoContract.VideoSchema.TABLE_NAME +
                    "." + VideoContract.VideoSchema.COLUMN_VIDEO_ID + " = ? ";
    private VideoDbHelper dbHelper;

    static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        String authority = VideoContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, VideoContract.PATH_VIDEO, VIDEO);

        return matcher;
    }

    private Cursor getVideoById(Uri uri, String[] projection, String sortOrder) {
        String id = uri.getPathSegments().get(1);

        String selection = VIDEO_ID_SELECTION;
        String[] selectionArgs = new String[]{id};

        return QUERY_BUILDER.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        dbHelper = new VideoDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = URI_MATCHER.match(uri);

        switch (match) {
            case VIDEO:
            case VIDEO_WITH_ID:
                return VideoContract.VideoSchema.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (URI_MATCHER.match(uri)) {
            // "video"
            case VIDEO: {
                retCursor = dbHelper.getReadableDatabase().query(
                        VideoContract.VideoSchema.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "video/*"
            case VIDEO_WITH_ID: {
                retCursor = getVideoById(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        Uri returnUri;


        switch (match) {
            case VIDEO: {
                long _id = db.insert(VideoContract.VideoSchema.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = VideoContract.VideoSchema.buildUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        int rowsDeleted;

        switch (match) {
            case VIDEO: {
                rowsDeleted = db.delete(VideoContract.VideoSchema.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0 || TextUtils.isEmpty(selection)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = URI_MATCHER.match(uri);
        int rowsUpdated;

        switch (match) {
            case VIDEO: {
                rowsUpdated = db.update(VideoContract.VideoSchema.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0 || TextUtils.isEmpty(selection)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case VIDEO:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(VideoContract.VideoSchema.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}