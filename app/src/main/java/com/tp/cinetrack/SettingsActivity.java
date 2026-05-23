package com.tp.cinetrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

  // Clé du fichier SharedPreferences
  public static final String PREFS_NAME    = "cinetrack_prefs";
  public static final String KEY_USERNAME  = "username";
  public static final String KEY_API_KEY   = "tmdb_api_key";

  private ImageView     btnBack;
  private EditText      etUsername, etApiKey;
  private MaterialButton btnSave;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    btnBack   = findViewById(R.id.btn_back);
    etUsername = findViewById(R.id.et_username);
    etApiKey   = findViewById(R.id.et_api_key);
    btnSave    = findViewById(R.id.btn_save);

    btnBack.setOnClickListener(v -> finish());

    // Charge les valeurs sauvegardées au démarrage
    loadSettings();

    btnSave.setOnClickListener(v -> saveSettings());
  }

  /**
   * Lit les valeurs dans SharedPreferences et les affiche dans les champs.
   * Si une valeur est absente, le champ reste vide.
   */
  private void loadSettings() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    String username = prefs.getString(KEY_USERNAME, "");
    String apiKey   = prefs.getString(KEY_API_KEY, "");

    etUsername.setText(username);

    // Affiche la clé masquée si elle existe, vide sinon
    if (!apiKey.isEmpty()) {
      etApiKey.setText(apiKey);
    }
  }

  /**
   * Sauvegarde les valeurs saisies dans SharedPreferences.
   */
  private void saveSettings() {
    String username = etUsername.getText().toString().trim();
    String apiKey   = etApiKey.getText().toString().trim();

    SharedPreferences.Editor editor = getSharedPreferences(
        PREFS_NAME, MODE_PRIVATE).edit();

    editor.putString(KEY_USERNAME, username);

    // Ne sauvegarde la clé que si elle n'est pas vide
    if (!apiKey.isEmpty()) {
      editor.putString(KEY_API_KEY, apiKey);
    }

    editor.apply();

    Toast.makeText(this,
        "Paramètres sauvegardés ✓", Toast.LENGTH_SHORT).show();
  }
}