package org.schabi.newpipe.player.local;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.schabi.newpipe.R;
import org.schabi.newpipe.UserSurveyActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VideosListActivity extends AppCompatActivity {
    private static final String TAG = "VideosListActivity";

    private ArrayList<String> videoIDs = new ArrayList<String>();

    private RecyclerView recyclerView;
    private ArrayList<VideoItem> videoList = new ArrayList<VideoItem>();
    private VideoListAdapter videoListAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //start UserSurveyActivity on first run
        if(sharedPreferences.getBoolean("FIRST", true)) {
            Log.d(TAG, "First run, start UserSurveyActivity");
            startActivity(new Intent(this, UserSurveyActivity.class));
        } else {
            initializeViews();

            //check for Read Permission
            checkReadPermission();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView_videos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        videoListAdapter = new VideoListAdapter(this, videoList);
        recyclerView.setAdapter(videoListAdapter);
    }

    private void checkReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            } else {
                loadVideos();
            }
        } else {
            loadVideos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                Toast.makeText(this, "Read permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadVideos() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION};
                String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

                Cursor cursor = getApplication().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String title = cursor.getString(titleColumn);
                        int duration = cursor.getInt(durationColumn);

                        if(title.contains("Sitting") || title.contains("Walking") || title.contains("Running")) {
                            videoIDs.add(id + title);
                            Log.d(TAG, "ID: " + id + " TITLE: " + title);

                            if (title.contains(("360p"))) {
                                Uri data = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                                String duration_formatted;
                                int sec = (duration / 1000) % 60;
                                int min = (duration / (1000 * 60)) % 60;
                                int hrs = duration / (1000 * 60 * 60);

                                if (hrs == 0) {
                                    duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                                } else {
                                    duration_formatted = String.valueOf(hrs).concat(":".concat(String.format(Locale.UK, "%02d", min).concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                                }

                                videoList.add(new VideoItem(id, data, title, duration_formatted));
                            }
                        }
                    }
                    Set<String> setVideos = new HashSet<>();
                    setVideos.addAll(videoIDs);
                    sharedPreferences.edit().putStringSet("VIDEO_IDS", setVideos).apply();
                }
            }
        }.start();
    }
}