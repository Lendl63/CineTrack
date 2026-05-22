package com.tp.cinetrack;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

  private ImageView btnBack;
  private RecyclerView rvSearchResults;
  private TextView tvEmptySearch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    btnBack = findViewById(R.id.btn_back);
    rvSearchResults = findViewById(R.id.rv_search_results);
    tvEmptySearch = findViewById(R.id.tv_empty_search);

    // Configuration du RecyclerView
    rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));

    // Bouton retour
    btnBack.setOnClickListener(v -> finish());

    // Affichage initial : état vide
    showEmptyState(true);
  }

  private void showEmptyState(boolean show) {
    tvEmptySearch.setVisibility(show ? View.VISIBLE : View.GONE);
    rvSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
  }
}