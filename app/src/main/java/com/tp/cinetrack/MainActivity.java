package com.tp.cinetrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private RecyclerView       rvSeries;
  private TextView           tvEmptyState;
  private LocalSerieCard     card;
  private List<LocalSerie>   allSeries = new ArrayList<>();
  private Spinner            spinnerFilter;
  private String             currentFilter = "Tous";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    rvSeries      = findViewById(R.id.rv_series);
    tvEmptyState  = findViewById(R.id.tv_empty_state);
    spinnerFilter = findViewById(R.id.spinner_filter);

    rvSeries.setLayoutManager(new GridLayoutManager(this, 2));
    card = new LocalSerieCard(this,
        new ArrayList<>(),
        this::onCardClick,
        this::onCardLongClick
    );
    rvSeries.setAdapter(card);

    ImageView btnSearch = findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(v ->
        startActivity(new Intent(this, SearchActivity.class)));

    ImageView btnSettings = findViewById(R.id.btn_settings);
    btnSettings.setOnClickListener(v ->
        startActivity(new Intent(this, SettingsActivity.class)));

    setupFilterSpinner();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Recharge les séries à chaque retour sur la page
    // (après ajout ou modification depuis DetailsActivity)
    allSeries = LocalSerieService.readAll(this);
    applyFilter(currentFilter);
  }

  // ─────────────────────────────────────────────────────────────────
  // CLIC SIMPLE → ouvre DetailsActivity avec la serie + flag "local"
  // ─────────────────────────────────────────────────────────────────
  private void onCardClick(LocalSerie serie) {
    Intent intent = new Intent(this, DetailsActivity.class);
    intent.putExtra("serie", serie);
    intent.putExtra("isLocal", true); // indique qu'elle vient du local
    startActivity(intent);
  }

  // ─────────────────────────────────────────────────────────────────
  // CLIC LONG → modal : modifier status / note / supprimer
  // ─────────────────────────────────────────────────────────────────
  private void onCardLongClick(LocalSerie serie) {
    String[] options = {"Modifier le status", "Modifier la note", "Supprimer"};

    new AlertDialog.Builder(this)
        .setTitle(serie.getTitle())
        .setItems(options, (dialog, which) -> {
          switch (which) {
            case 0: showStatusDialog(serie);  break;
            case 1: showRatingDialog(serie);  break;
            case 2: showDeleteDialog(serie);  break;
          }
        })
        .show();
  }

  private void showStatusDialog(LocalSerie serie) {
    String[] statuses = {"En cours", "Terminé", "À voir"};
    new AlertDialog.Builder(this)
        .setTitle("Modifier le status")
        .setItems(statuses, (dialog, which) -> {
          serie.setUserStatus(statuses[which]);
          saveAndRefresh(serie);
        })
        .show();
  }

  private void showRatingDialog(LocalSerie serie) {
    String[] notes = {"1", "2", "3", "4", "5"};
    new AlertDialog.Builder(this)
        .setTitle("Modifier la note")
        .setItems(notes, (dialog, which) -> {
          serie.setPersonalRating(which + 1);
          saveAndRefresh(serie);
        })
        .show();
  }

  private void showDeleteDialog(LocalSerie serie) {
    new AlertDialog.Builder(this)
        .setTitle("Supprimer")
        .setMessage("Supprimer \"" + serie.getTitle() + "\" de votre liste ?")
        .setPositiveButton("Supprimer", (dialog, which) -> {
          LocalSerieService.delete(this, serie.getId());
          allSeries = LocalSerieService.readAll(this);
          applyFilter(currentFilter);
        })
        .setNegativeButton("Annuler", null)
        .show();
  }

  private void saveAndRefresh(LocalSerie serie) {
    LocalSerieService.saveOrUpdate(this, serie);
    allSeries = LocalSerieService.readAll(this);
    applyFilter(currentFilter);
  }

  // ─────────────────────────────────────────────────────────────────
  // FILTRE
  // ─────────────────────────────────────────────────────────────────
  private void setupFilterSpinner() {
    String[] options = {"Tous", "En cours", "Terminé", "À voir"};
    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, options);
    spinnerAdapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinnerFilter.setAdapter(spinnerAdapter);

    spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
                                 int position, long id) {
        currentFilter = options[position];
        applyFilter(currentFilter);
      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });
  }

  /**
   * Filtre par userStatus (statut choisi par l'utilisateur).
   */
  private void applyFilter(String filter) {
    List<LocalSerie> filtered = new ArrayList<>();
    for (LocalSerie s : allSeries) {
      if (filter.equals("Tous")
          || filter.equals(s.getUserStatus())) {
        filtered.add(s);
      }
    }
    card.updateData(filtered);
    updateEmptyState(filtered.isEmpty());
  }

  private void updateEmptyState(boolean isEmpty) {
    tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    rvSeries.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
  }
}