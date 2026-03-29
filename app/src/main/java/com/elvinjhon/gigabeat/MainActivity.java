package com.elvinjhon.gigabeat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TrackAdapter.OnTrackClickListener {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private TrackAdapter adapter;
    private ProgressBar progressBar;
    private TextView statusText, sectionTitle;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.search_input);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        sectionTitle = findViewById(R.id.section_title);

        adapter = new TrackAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int state) {
                if (state == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard();
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                debounceRunnable = () -> {
                    String q = s.toString().trim();
                    if (!q.isEmpty()) {
                        sectionTitle.setText("Search Results");
                        doSearch(q);
                    } else {
                        sectionTitle.setText("Trending Now");
                        doSearch("top hits 2024");
                    }
                };
                debounceHandler.postDelayed(debounceRunnable, 600);
            }
        });

        searchInput.setOnEditorActionListener((v, id, ev) -> {
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) { hideKeyboard(); doSearch(q); }
                return true;
            }
            return false;
        });

        doSearch("top hits 2024");
    }

    private void doSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                List<Track> tracks = ApiClient.search(query);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (tracks.isEmpty()) {
                        statusText.setText("No tracks found. Try another search.");
                        statusText.setVisibility(View.VISIBLE);
                    }
                    adapter.setTracks(tracks);
                    TrackListHolder.setTracks(tracks);
                    recyclerView.scrollToPosition(0);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Connection error. Check your internet.");
                    statusText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public void onTrackClick(int position) {
        hideKeyboard();
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("track_index", position);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (focus != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
