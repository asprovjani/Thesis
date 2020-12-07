package org.schabi.newpipe.player.local;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.opencsv.CSVWriter;

import org.schabi.newpipe.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.lang.Character.isDigit;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LocalVideoPlayer extends AppCompatActivity {
    private final String TAG = "LocalVideoPlayer";

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImageButton resolutionButton;
    private TextView resolutionText;
    private SharedPreferences sharedPreferences;

    private long videoID;
    private ArrayList<String> videoList = new ArrayList<>();

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_player);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        videoList.addAll(sharedPreferences.getStringSet("VIDEO_IDS", null));

        videoID = getIntent().getExtras().getLong("VIDEO_ID");

        playerView = findViewById(R.id.playerView);

        resolutionText = findViewById(R.id.resolutionText);
        resolutionText.setText("Resolution: 360p");

        resolutionButton = findViewById(R.id.resolutionButton);
        resolutionButton.setOnClickListener(new View.OnClickListener() {
            //load better resolution when button is pressed
            @Override
            public void onClick(View v) {
                String videoTitle = "";
                Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID);

                String scheme = videoUri.getScheme();
                if(scheme.equals("content")) {
                    String[] proj = {MediaStore.Video.Media.DISPLAY_NAME};
                    Cursor cursor = getApplicationContext().getContentResolver().query(videoUri, proj, null, null, null);
                    if(cursor != null) {
                        int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                        cursor.moveToFirst();
                        videoTitle = cursor.getString(columnIndex);
                        getNextVideo(videoTitle);
                    }
                }
            }
        });

        try {
            saveToFile(getIntent().getExtras().getString("VIDEO_TITLE"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        if(Util.SDK_INT<24){
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(Util.SDK_INT>=24){
            releasePlayer();
        }
        sharedPreferences.edit().putLong("CURRENT_POSITION", -1).apply();
        super.onStop();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/
    private void initializePlayer() {
        if(player == null) {
            player = new SimpleExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
        }

        Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID);
        MediaSource mediaSource = buildMediaSource(videoUri);
        player.prepare(mediaSource);
        long currentTime = sharedPreferences.getLong("CURRENT_POSITION", -1);
        if(currentTime != -1) {
            player.seekTo(player.getCurrentWindowIndex(), currentTime);
        }
        player.setPlayWhenReady(true);
    }

    private void releasePlayer(){
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void getNextVideo(String videoTitle) {
        if(videoTitle.contains("360p")) {
            videoTitle = videoTitle.replace("360p", "480p");
            resolutionText.setText("Resolution: 480p");
        }
        else if(videoTitle.contains("480p")) {
            videoTitle = videoTitle.replace("480p", "720p");
            resolutionText.setText("Resolution: 720p");
        }
        else {
            videoTitle = videoTitle.replace("720p", "1080p");
            resolutionText.setText("Resolution: 1080p");
            resolutionButton.setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < videoList.size(); i++) {
            if(videoList.get(i).contains(videoTitle)) {
                String newTitle = videoList.get(i);
                String newVideoId = "";
                for(int j = 0; j < newTitle.length(); j++) {
                    if(isDigit(newTitle.charAt(j)))
                        newVideoId += newTitle.charAt(j);
                    else
                        break;
                }

                try {
                    saveToFile(videoTitle);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sharedPreferences.edit().putLong("CURRENT_POSITION", player.getCurrentPosition()).apply();
                videoID = Long.parseLong(newVideoId);
                initializePlayer();
                return;
            }
        }
    }

    private void saveToFile(String videoTitle) throws IOException {
        String dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "UserData.csv";
        String path = dir + File.separator + fileName;
        File f = new File(path);
        CSVWriter w;

        if(f.exists() && !f.isDirectory()) {
            FileWriter fWriter = new FileWriter(path, true);
            w = new CSVWriter(fWriter);

            if(sharedPreferences.getBoolean("firstWrite", true)) {
                String[] key = {"VIDEO_TITLE", "TIME"};
                w.writeNext(key);
                SharedPreferences.Editor preferencesEdit = sharedPreferences.edit();
                preferencesEdit.putBoolean("firstWrite", false);
                preferencesEdit.apply();
            }

            String[] value = {videoTitle, dtf.format(LocalDateTime.now())};
            w.writeNext(value);
            w.close();
        }
    }

}