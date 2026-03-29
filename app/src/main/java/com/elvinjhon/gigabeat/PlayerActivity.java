package com.elvinjhon.gigabeat;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class PlayerActivity extends AppCompatActivity {

    private ImageView albumArt;
    private TextView trackTitle, artistName, albumTitle, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton btnPlay, btnNext, btnPrev, btnBack;
    private View loadingOverlay;

    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentIndex;
    private boolean isPlaying = false;
    private boolean isPrepared = false;

    private final Runnable seekUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPrepared && isPlaying) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                currentTime.setText(fmt(pos));
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        albumArt = findViewById(R.id.player_album_art);
        trackTitle = findViewById(R.id.player_track_title);
        artistName = findViewById(R.id.player_artist_name);
        albumTitle = findViewById(R.id.player_album_title);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        btnPlay = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);
        btnPrev = findViewById(R.id.btn_previous);
        btnBack = findViewById(R.id.btn_back);
        loadingOverlay = findViewById(R.id.loading_overlay);

        currentIndex = getIntent().getIntExtra("track_index", 0);

        btnPlay.setOnClickListener(v -> togglePlay());
        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrev());
        btnBack.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(fmt(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        loadTrack(currentIndex);
    }

    private void loadTrack(int index) {
        if (index < 0 || index >= TrackListHolder.size()) return;
        currentIndex = index;
        Track track = TrackListHolder.getTrack(index);
        if (track == null) return;

        trackTitle.setText(track.getTitle());
        artistName.setText(track.getArtistName());
        albumTitle.setText(track.getAlbumTitle());

        String cover = track.getCoverBig();
        if (cover == null || cover.isEmpty()) cover = track.getCoverMedium();

        Glide.with(this)
                .load(cover)
                .transform(new CenterCrop(), new RoundedCorners(40))
                .placeholder(R.drawable.ic_music_note)
                .into(albumArt);

        btnPrev.setAlpha(currentIndex > 0 ? 1f : 0.3f);
        btnNext.setAlpha(currentIndex < TrackListHolder.size() - 1 ? 1f : 0.3f);

        playUrl(track.getPreview());
    }

    private void playUrl(String url) {
        loadingOverlay.setVisibility(View.VISIBLE);
        isPrepared = false;
        handler.removeCallbacks(seekUpdater);

        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build());

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                loadingOverlay.setVisibility(View.GONE);
                seekBar.setMax(mp.getDuration());
                totalTime.setText(fmt(mp.getDuration()));
                mp.start();
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.ic_pause);
                handler.post(seekUpdater);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.ic_play_arrow);
                seekBar.setProgress(seekBar.getMax());
                if (currentIndex < TrackListHolder.size() - 1) playNext();
            });

            mediaPlayer.setOnErrorListener((mp, w, e) -> {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (Exception e) {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, "Cannot play track", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlay() {
        if (mediaPlayer == null || !isPrepared) return;
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlay.setImageResource(R.drawable.ic_play_arrow);
            handler.removeCallbacks(seekUpdater);
        } else {
            mediaPlayer.start();
            isPlaying = true;
            btnPlay.setImageResource(R.drawable.ic_pause);
            handler.post(seekUpdater);
        }
    }

    private void playNext() {
        if (currentIndex < TrackListHolder.size() - 1) loadTrack(currentIndex + 1);
    }

    private void playPrev() {
        if (currentIndex > 0) loadTrack(currentIndex - 1);
    }

    private String fmt(int ms) {
        int s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(seekUpdater);
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
