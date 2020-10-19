package org.schabi.newpipe.player.local;

import android.net.Uri;

public class VideoItem {
    long id;
    Uri data;
    String title;
    String duration;

    public VideoItem(long id, Uri data, String title, String duration) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
