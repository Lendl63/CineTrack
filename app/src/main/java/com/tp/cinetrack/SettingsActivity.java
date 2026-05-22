package com.tp.cinetrack;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsActivity extends AppCompatActivity {


  private ImageView btnBack;
  private RecyclerView rvSearchResults;
  private TextView tvEmptySearch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    btnBack = findViewById(R.id.btn_back);
    // Bouton retour
    btnBack.setOnClickListener(v -> finish());
  }
}

