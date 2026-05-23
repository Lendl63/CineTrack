package com.tp.cinetrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

  private static final String KEY_QUERY = "search_query"; // onSaveInstanceState

  private ImageView         btnBack;
  private TextInputEditText etSearch;
  private RecyclerView      rvSearchResults;
  private TextView          tvEmptySearch;
  private ProgressBar       progressSearch;
  private SerieCard         card;

  private final Handler         mainHandler = new Handler(Looper.getMainLooper());
  private final ExecutorService executor    = Executors.newSingleThreadExecutor();
  private       Runnable        searchRunnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // onCreate : initialisation de l'écran de recherche ; restaure le texte saisi si recréation
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    btnBack         = findViewById(R.id.btn_back);
    etSearch        = findViewById(R.id.et_search);
    rvSearchResults = findViewById(R.id.rv_search_results);
    tvEmptySearch   = findViewById(R.id.tv_empty_search);
    progressSearch  = findViewById(R.id.progress_search);

    rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
    card = new SerieCard(this, new ArrayList<>());
    rvSearchResults.setAdapter(card);

    btnBack.setOnClickListener(v -> finish());

    if (!TmdbApiService.isApiKeyConfigured(this)) {
      showMessage("Clé API TMDB non configurée.\nConfigure-la dans les Paramètres.");
    } else {
      showMessage("Rechercher une série de science-fiction...");
    }

    etSearch.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
      @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
        if (searchRunnable != null)
          mainHandler.removeCallbacks(searchRunnable);
      }
      @Override
      public void afterTextChanged(Editable s) {
        String query = s.toString().trim();
        if (query.isEmpty()) {
          card.updateData(new ArrayList<>());
          hideLoading();
          if (!TmdbApiService.isApiKeyConfigured(SearchActivity.this)) {
            showMessage("Clé API TMDB non configurée.\nConfigure-la dans les Paramètres.");
          } else {
            showMessage("Rechercher une série de science-fiction...");
          }
          return;
        }
        searchRunnable = () -> {
          showLoading();
          performSearch(query);
        };
        mainHandler.postDelayed(searchRunnable, 600);
      }
    });

    // Restaure le texte de recherche après rotation
    if (savedInstanceState != null) {
      String savedQuery = savedInstanceState.getString(KEY_QUERY, "");
      if (!savedQuery.isEmpty()) {
        etSearch.setText(savedQuery);
        etSearch.setSelection(savedQuery.length());
      }
    }
  }

  @Override
  protected void onResume() {
    // onResume : l'écran revient au premier plan ; aucune action nécessaire ici
    super.onResume();
  }

  @Override
  protected void onPause() {
    // onPause : l'activité va en arrière-plan ; annule les callbacks en attente
    super.onPause();
    if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);
  }

  // ── Sauvegarde / restauration du texte de recherche ─────────────

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // Sauvegarde le texte de recherche pour le restaurer après rotation
    String query = etSearch.getText() != null ? etSearch.getText().toString() : "";
    outState.putString(KEY_QUERY, query);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    // Restauration gérée dans onCreate pour éviter la double exécution
  }

  // ── Menu d'options ───────────────────────────────────────────────

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // ── Recherche réseau avec gestion d'absence de connexion ─────────

  private void performSearch(String query) {
    if (!TmdbApiService.isApiKeyConfigured(this)) {
      hideLoading();
      showMessage("Clé API TMDB non configurée.\nConfigure-la dans les Paramètres.");
      return;
    }

    executor.execute(() -> {
      try {
        List<Serie> results = TmdbApiService.searchSeries(
            getApplicationContext(), query);

        mainHandler.post(() -> {
          hideLoading();
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

      } catch (Exception e) {
        // Gestion réseau : affiche un Toast si la connexion est absente ou en erreur
        mainHandler.post(() -> {
          hideLoading();
          Toast.makeText(SearchActivity.this,
              "Erreur réseau. Vérifiez votre connexion.",
              Toast.LENGTH_LONG).show();
          showMessage("Impossible de charger les résultats.");
        });
      }
    });
  }

  // ── Helpers UI ────────────────────────────────────────────────────

  private void showLoading() {
    progressSearch.setVisibility(View.VISIBLE);
    tvEmptySearch.setVisibility(View.GONE);
    rvSearchResults.setVisibility(View.GONE);
  }

  private void hideLoading() {
    progressSearch.setVisibility(View.GONE);
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
    if (searchRunnable != null)
      mainHandler.removeCallbacks(searchRunnable);
  }
}
