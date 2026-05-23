package com.tp.cinetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps3d.model.ImageView;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

  // Clé du fichier SharedPreferences
  public static final String PREFS_NAME    = "cinetrack_prefs";
  public static final String KEY_USERNAME  = "username";
  public static final String KEY_API_KEY   = "tmdb_api_key";
  public static final String KEY_THEME     = "theme"; // thématique (obligatoire CDC)

  private ImageView     btnBack;
  private EditText      etUsername, etApiKey;
  private MaterialButton btnSave;
  private RadioGroup    rgTheme;
  private Switch        switchNotifications;
  private CheckBox      cbRappels;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // onCreate : initialisation unique de l'activité (vues, listeners, données initiales)
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    btnBack             = findViewById(R.id.btn_back);
    etUsername          = findViewById(R.id.et_username);
    etApiKey            = findViewById(R.id.et_api_key);
    btnSave             = findViewById(R.id.btn_save);
    rgTheme             = findViewById(R.id.rg_theme);
    switchNotifications = findViewById(R.id.switch_notifications);
    cbRappels           = findViewById(R.id.cb_rappels);

    btnBack.setOnClickListener(v -> finish());

    loadSettings();

    btnSave.setOnClickListener(v -> saveSettings());
  }

  @Override
  protected void onResume() {
    // onResume : appelé à chaque fois que l'activité revient au premier plan
    // (après pause ou retour d'une autre activité) — rechargement si nécessaire
    super.onResume();
  }

  @Override
  protected void onPause() {
    // onPause : sauvegarde légère si l'utilisateur quitte sans appuyer sur Enregistrer
    super.onPause();
  }

  // ── Menu d'options (item "Paramètres" requis par le CDC) ──────────
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      // Déjà sur Paramètres, on ne re-navigue pas
      Toast.makeText(this, "Déjà dans les paramètres", Toast.LENGTH_SHORT).show();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // ── Chargement ────────────────────────────────────────────────────

  private void loadSettings() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    etUsername.setText(prefs.getString(KEY_USERNAME, ""));
    String apiKey = prefs.getString(KEY_API_KEY, "");
    if (!apiKey.isEmpty()) etApiKey.setText(apiKey);

    // Restitue le thème sauvegardé
    String savedTheme = prefs.getString(KEY_THEME, "scifi");
    if ("fantasy".equals(savedTheme)) {
      rgTheme.check(R.id.rb_theme_fantasy);
    } else {
      rgTheme.check(R.id.rb_theme_scifi);
    }
  }

  private void saveSettings() {
    String username = etUsername.getText().toString().trim();
    String apiKey   = etApiKey.getText().toString().trim();

    // Récupère le thème sélectionné dans le RadioGroup
    String theme = (rgTheme.getCheckedRadioButtonId() == R.id.rb_theme_fantasy)
        ? "fantasy" : "scifi";

    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
    editor.putString(KEY_USERNAME, username);
    if (!apiKey.isEmpty()) editor.putString(KEY_API_KEY, apiKey);
    editor.putString(KEY_THEME, theme);
    editor.apply();

    Toast.makeText(this, "Paramètres sauvegardés", Toast.LENGTH_SHORT).show();
  }
}
