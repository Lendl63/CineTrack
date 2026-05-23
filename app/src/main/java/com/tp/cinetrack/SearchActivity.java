package com.tp.cinetrack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

  private ImageView         btnBack;
  private TextInputEditText etSearch;
  private RecyclerView      rvSearchResults;
  private TextView          tvEmptySearch;
  private SerieCard card;

  private final Handler         mainHandler = new Handler(Looper.getMainLooper());
  private final ExecutorService executor    = Executors.newSingleThreadExecutor();
  private       Runnable        searchRunnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    btnBack         = findViewById(R.id.btn_back);
    etSearch        = findViewById(R.id.et_search);
    rvSearchResults = findViewById(R.id.rv_search_results);
    tvEmptySearch   = findViewById(R.id.tv_empty_search);

    // RecyclerView
    rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
    card = new SerieCard(this, new ArrayList<>());
    rvSearchResults.setAdapter(card);

    btnBack.setOnClickListener(v -> finish());

    // Message initial selon disponibilité de la clé API
    if (!TmdbApiService.isApiKeyConfigured()) {
      showMessage("Clé API TMDB non configurée.\n"
          + "Ajoute TMDB_API_KEY=ta_clé dans le fichier local.properties.");
    } else {
      showMessage("Rechercher une série de science-fiction...");
    }

    // Debounce 600ms : lance la recherche après la dernière frappe
    etSearch.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
      @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
        if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);
      }
      @Override
      public void afterTextChanged(Editable s) {
        String query = s.toString().trim();

        if (query.isEmpty()) {
          card.updateData(new ArrayList<>());
          if (!TmdbApiService.isApiKeyConfigured()) {
            showMessage("Clé API TMDB non configurée.\n"
                + "Ajoute TMDB_API_KEY=ta_clé dans le fichier local.properties.");
          } else {
            showMessage("Rechercher une série de science-fiction...");
          }
          return;
        }

        searchRunnable = () -> performSearch(query);
        mainHandler.postDelayed(searchRunnable, 600);
      }
    });
  }

  private void performSearch(String query) {
    if (!TmdbApiService.isApiKeyConfigured()) {
      showMessage("Clé API TMDB non configurée.\n"
          + "Ajoute TMDB_API_KEY=ta_clé dans le fichier local.properties.");
      return;
    }

    // Appel réseau sur thread background
    executor.execute(() -> {
      List<Serie> results = TmdbApiService.searchSeries(query);

      mainHandler.post(() -> {
        if (results.isEmpty()) {
          card.updateData(new ArrayList<>());
          showMessage("Aucun résultat pour \"" + query + "\".\n"
              + "Vérifiez qu'il s'agit d'une série de science-fiction\n"
              + "ou vérifiez l'orthographe.");
        } else {
          card.updateData(results);
          showResults();
        }
      });
    });
  }

  private void showMessage(String message) {
    tvEmptySearch.setText(message);
    tvEmptySearch.setVisibility(View.VISIBLE);
    rvSearchResults.setVisibility(View.GONE);
  }

  private void showResults() {
    tvEmptySearch.setVisibility(View.GONE);
    rvSearchResults.setVisibility(View.VISIBLE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
    if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);
  }
}