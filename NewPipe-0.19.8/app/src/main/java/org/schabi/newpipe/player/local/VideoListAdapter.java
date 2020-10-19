package org.schabi.newpipe.player.local;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.schabi.newpipe.R;

import java.util.ArrayList;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    ArrayList<VideoItem> videoList = new ArrayList<VideoItem>();
    Context context;

    VideoListAdapter(Context context, ArrayList<VideoItem> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoListAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListAdapter.VideoViewHolder holder, int position) {
        final VideoItem videoItem = videoList.get(position);

        holder.title.setText(videoItem.getTitle());
        holder.duration.setText(videoItem.getDuration());
        Glide.with(context).load(videoItem.getData()).into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocalVideoPlayer.class);
                intent.putExtra("VIDEO_ID", videoItem.getId());
                intent.putExtra("VIDEO_TITLE", videoItem.getTitle());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView duration;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            duration = itemView.findViewById(R.id.tv_duration);
            thumbnail = itemView.findViewById(R.id.imageView_thumbnail);
        }
    }
}
