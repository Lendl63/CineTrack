package com.tp.cinetrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

  private static final String KEY_FILTER = "current_filter"; // pour onSaveInstanceState

  private RecyclerView       rvSeries;
  private TextView           tvEmptyState;
  private LocalSerieCard     card;
  private List<LocalSerie>   allSeries = new ArrayList<>();
  private Spinner            spinnerFilter;
  private String             currentFilter = "Tous";

  // Série sélectionnée par menu contextuel
  private LocalSerie selectedSerie = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // onCreate : initialisation unique ; on restaure le filtre si l'activité est recrée
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Restauration du filtre après rotation / recréation
    if (savedInstanceState != null) {
      currentFilter = savedInstanceState.getString(KEY_FILTER, "Tous");
    }

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

    // Enregistre le RecyclerView pour le menu contextuel
    registerForContextMenu(rvSeries);

    ImageView btnSearch = findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(v ->
        startActivity(new Intent(this, SearchActivity.class)));

    // FAB obligatoire (CDC) — raccourci vers la Recherche
    findViewById(R.id.fab_add).setOnClickListener(v ->
        startActivity(new Intent(this, SearchActivity.class)));

    ImageView btnSettings = findViewById(R.id.btn_settings);
    btnSettings.setOnClickListener(v ->
        startActivity(new Intent(this, SettingsActivity.class)));

    setupFilterSpinner();
  }

  @Override
  protected void onResume() {
    // onResume : recharge la collection après chaque retour (ajout/modif dans DetailsActivity)
    super.onResume();
    allSeries = LocalSerieService.readAll(this);
    applyFilter(currentFilter);
  }

  @Override
  protected void onPause() {
    // onPause : l'activité passe en arrière-plan ; rien à sauvegarder ici (persistance dans fichier)
    super.onPause();
  }

  // ── Sauvegarde / restauration d'état ────────────────────────────

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // Sauvegarde le filtre actif pour le restaurer après rotation
    outState.putString(KEY_FILTER, currentFilter);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    // Restaure le filtre (appelé automatiquement après onCreate si Bundle non null)
    currentFilter = savedInstanceState.getString(KEY_FILTER, "Tous");
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

  // ── Menu contextuel (XML) ────────────────────────────────────────

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.context_menu_serie, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if (selectedSerie == null) return super.onContextItemSelected(item);
    int id = item.getItemId();
    if (id == R.id.ctx_modifier_statut) {
      showStatusDialog(selectedSerie);
      return true;
    } else if (id == R.id.ctx_modifier_note) {
      showRatingDialog(selectedSerie);
      return true;
    } else if (id == R.id.ctx_supprimer) {
      showDeleteDialog(selectedSerie);
      return true;
    }
    return super.onContextItemSelected(item);
  }

  // ─────────────────────────────────────────────────────────────────
  // CLIC SIMPLE → ouvre DetailsActivity avec la serie + flag "local"
  // ─────────────────────────────────────────────────────────────────
  private void onCardClick(LocalSerie serie) {
    Intent intent = new Intent(this, DetailsActivity.class);
    intent.putExtra("serie", serie);
    intent.putExtra("isLocal", true);
    startActivity(intent);
  }

  // ─────────────────────────────────────────────────────────────────
  // CLIC LONG → mémorise la série + ouvre le menu contextuel XML
  // ─────────────────────────────────────────────────────────────────
  private void onCardLongClick(LocalSerie serie) {
    selectedSerie = serie;
    // Déclenche le menu contextuel enregistré sur rvSeries
    openContextMenu(rvSeries);
  }

  private void showStatusDialog(LocalSerie serie) {
    String[] statuses = {"En cours", "Terminé", "À voir"};
    new AlertDialog.Builder(this)
        .setTitle("Modifier le statut")
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

    // Positionne le spinner sur le filtre restauré
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals(currentFilter)) {
        spinnerFilter.setSelection(i);
        break;
      }
    }

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

  private void applyFilter(String filter) {
    List<LocalSerie> filtered = new ArrayList<>();
    for (LocalSerie s : allSeries) {
      if (filter.equals("Tous") || filter.equals(s.getUserStatus())) {
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
