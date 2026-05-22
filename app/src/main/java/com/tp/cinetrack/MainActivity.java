package com.tp.cinetrack;   // ← Change avec ton package

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

  private TextView filterTous, filterEnCours, filterTermine, filterAVoir;
  private RecyclerView rvSeries;
  private TextView tvEmptyState;
  private String currentFilter = "Tous";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialisation
    filterTous = findViewById(R.id.filter_tous);
    filterEnCours = findViewById(R.id.filter_encours);
    filterTermine = findViewById(R.id.filter_termine);
    filterAVoir = findViewById(R.id.filter_avoir);

    rvSeries = findViewById(R.id.rv_series);
    tvEmptyState = findViewById(R.id.tv_empty_state);

    // Configuration RecyclerView
    rvSeries.setLayoutManager(new GridLayoutManager(this, 2));

    //Connection a la page recherche
    ImageView btnSearch = findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(v -> {
      startActivity(new Intent(MainActivity.this, SearchActivity.class));
    });

    //Connection a la page recherche
    ImageView btnSettings = findViewById(R.id.btn_settings);
    btnSettings.setOnClickListener(v -> {
      startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    });

    // Gestion des filtres
    setupFilterListeners();

    // Affichage initial (état vide)
    updateEmptyState(true);
  }

  private void setupFilterListeners() {
    View.OnClickListener filterClick = v -> {
      // Réinitialiser tous les filtres en gris
      filterTous.setTextColor(getColor(R.color.gray));
      filterEnCours.setTextColor(getColor(R.color.gray));
      filterTermine.setTextColor(getColor(R.color.gray));
      filterAVoir.setTextColor(getColor(R.color.gray));

      // Mettre le filtre sélectionné en bleu
      ((TextView) v).setTextColor(getColor(R.color.bleu));

      // Mettre à jour le filtre actuel
      currentFilter = ((TextView) v).getText().toString();

      // TODO: Filtrer les données du RecyclerView ici plus tard
      // filterSeries(currentFilter);
    };

    filterTous.setOnClickListener(filterClick);
    filterEnCours.setOnClickListener(filterClick);
    filterTermine.setOnClickListener(filterClick);
    filterAVoir.setOnClickListener(filterClick);
  }

  // Méthode pour afficher/masquer l'état vide
  public void updateEmptyState(boolean isEmpty) {
    tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    rvSeries.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
  }
}