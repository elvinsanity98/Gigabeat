package com.elvinjhon.gigabeat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private List<Track> tracks = new ArrayList<>();
    private OnTrackClickListener listener;

    public interface OnTrackClickListener {
        void onTrackClick(int position);
    }

    public TrackAdapter(OnTrackClickListener listener) {
        this.listener = listener;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Track track = tracks.get(position);
        h.title.setText(track.getTitle());
        h.artist.setText(track.getArtistName());
        h.duration.setText(track.getFormattedDuration());
        h.number.setText(String.valueOf(position + 1));

        Glide.with(h.itemView.getContext())
                .load(track.getCoverMedium())
                .transform(new CenterCrop(), new RoundedCorners(16))
                .placeholder(R.drawable.ic_music_note)
                .into(h.art);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTrackClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView art;
        TextView title, artist, duration, number;

        ViewHolder(View v) {
            super(v);
            art = v.findViewById(R.id.album_art);
            title = v.findViewById(R.id.track_title);
            artist = v.findViewById(R.id.artist_name);
            duration = v.findViewById(R.id.duration);
            number = v.findViewById(R.id.track_number);
        }
    }
}
