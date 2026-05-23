package com.tp.cinetrack;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailsActivity extends AppCompatActivity {

  // Vues header
  private ImageView         ivBackdrop;
  private TextView          tvTitle, tvTagline, tvReleaseDate,
      tvRating, tvSynopsis,
      tvSeasons, tvEpisodes, tvHours,
      tvPlatform;
  private ImageView         btnBack;

  private MaterialButton btnTrailer;
  private String         trailerUrl = null;

  // Formulaire
  private Spinner           spinnerAvisStatus;
  private TextInputEditText etNote, etAvis, etDateVisionnage;
  private TextInputLayout   layoutNote, layoutAvis, layoutDateVisionnage;
  private MaterialButton    btnSave;
  private RatingBar         ratingBar; // RatingBar obligatoire (CDC)

  // Série courante (complétée après le fetch)
  private Serie currentSerie = null;

  private final ExecutorService executor    = Executors.newSingleThreadExecutor();
  private final Handler         mainHandler = new Handler(Looper.getMainLooper());

  private final String[] statusOptions = {"En cours", "Terminé", "À voir"};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // onCreate : initialisation de l'écran de détail ; lie les vues et lance le chargement TMDB
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);

    // Liaison vues header
    ivBackdrop       = findViewById(R.id.iv_backdrop);
    tvTitle          = findViewById(R.id.tv_title);
    tvTagline        = findViewById(R.id.tv_quote);
    tvReleaseDate    = findViewById(R.id.tv_date);
    tvRating         = findViewById(R.id.tv_rating);
    tvSynopsis       = findViewById(R.id.tv_synopsis);
    tvSeasons        = findViewById(R.id.tv_seasons);
    tvEpisodes       = findViewById(R.id.tv_episodes);
    tvHours          = findViewById(R.id.tv_hours);
    tvPlatform       = findViewById(R.id.tv_platform);

    // Liaison vues formulaire
    btnBack              = findViewById(R.id.btn_back);
    spinnerAvisStatus    = findViewById(R.id.spinner_avis_status);
    etNote               = findViewById(R.id.et_note);
    etAvis               = findViewById(R.id.et_avis);
    etDateVisionnage     = findViewById(R.id.et_date_visionnage);
    layoutNote           = findViewById(R.id.layout_note);
    layoutAvis           = findViewById(R.id.layout_avis);
    layoutDateVisionnage = findViewById(R.id.layout_date_visionnage);
    btnSave              = findViewById(R.id.btn_save);
    btnTrailer           = findViewById(R.id.btn_trailer);
    ratingBar            = findViewById(R.id.rating_bar); // RatingBar obligatoire

    btnTrailer.setVisibility(View.GONE);
    btnBack.setOnClickListener(v -> finish());
    setupSpinners();
    setupDatePicker();
    setupSaveButton();

    // Synchronise la RatingBar avec le champ etNote (simple liaison unidirectionnelle)
    ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
      if (fromUser) etNote.setText(String.valueOf((int) rating));
    });

    Serie serie = (Serie) getIntent().getSerializableExtra("serie");
    if (serie != null) {
      currentSerie = serie;
      bindPartialData(serie);
      fetchFullDetails(serie.getId());
    }
    boolean isLocal = getIntent().getBooleanExtra("isLocal", false);
    if (isLocal && serie instanceof LocalSerie) {
      prefillForm((LocalSerie) serie);
    }
  }

  @Override
  protected void onResume() {
    // onResume : l'écran de détail revient au premier plan ; aucune action nécessaire
    super.onResume();
  }

  @Override
  protected void onPause() {
    // onPause : l'écran passe en arrière-plan ; aucune sauvegarde partielle requise ici
    super.onPause();
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

  // ─────────────────────────────────────────────────────────────────
  // AFFICHAGE DES DONNÉES
  // ─────────────────────────────────────────────────────────────────

  private void prefillForm(LocalSerie local) {
    String[] statuses = {"En cours", "Terminé", "À voir"};
    for (int i = 0; i < statuses.length; i++) {
      if (statuses[i].equals(local.getUserStatus())) {
        spinnerAvisStatus.setSelection(i);
        break;
      }
    }
    if (local.getPersonalRating() > 0) {
      etNote.setText(String.valueOf((int) local.getPersonalRating()));
      ratingBar.setRating(local.getPersonalRating());
    }
    if (local.getPersonalReview() != null && !local.getPersonalReview().isEmpty()) {
      etAvis.setText(local.getPersonalReview());
    }
    if (local.getWatchDate() != null && !local.getWatchDate().isEmpty()) {
      etDateVisionnage.setText(local.getWatchDate());
    }
  }

  private void bindPartialData(Serie serie) {
    tvTitle.setText(serie.getTitle());
    tvRating.setText(String.format("%.0f", serie.getTmdbRating()));
    if (serie.getSynopsis() != null && !serie.getSynopsis().isEmpty())
      tvSynopsis.setText(serie.getSynopsis());
    tvReleaseDate.setText(serie.getReleaseDate() != null ? serie.getReleaseDate() : "");
    tvTagline.setVisibility(View.GONE);
    String imagePath = serie.getBackdropPath() != null
        ? serie.getBackdropPath() : serie.getPosterPath();
    if (imagePath != null && !imagePath.isEmpty()) {
      Glide.with(this)
          .load(TmdbApiService.IMAGE_BASE_URL + imagePath)
          .centerCrop()
          .placeholder(R.mipmap.ic_placeholder)
          .into(ivBackdrop);
    }
    tvSeasons.setText("--");
    tvEpisodes.setText("--");
    tvHours.setText("--");
    tvPlatform.setText("--");
  }

  private void fetchTrailer(int serieId) {
    executor.execute(() -> {
      try {
        String urlStr = TmdbApiService.BASE_URL
            + "/tv/" + serieId
            + "/videos?api_key=" + TmdbApiService.getApiKey(getApplicationContext())
            + "&language=fr-FR";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() != 200) return;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();
        JSONObject response = new JSONObject(sb.toString());
        JSONArray  videos   = response.getJSONArray("results");
        String foundUrl = null;
        for (int i = 0; i < videos.length(); i++) {
          JSONObject v = videos.getJSONObject(i);
          if (v.getString("type").equals("Trailer") && v.getString("site").equals("YouTube")) {
            foundUrl = "https://www.youtube.com/watch?v=" + v.getString("key");
            break;
          }
        }
        if (foundUrl == null && videos.length() > 0) {
          urlStr = TmdbApiService.BASE_URL + "/tv/" + serieId
              + "/videos?api_key=" + TmdbApiService.getApiKey(getApplicationContext())
              + "&language=en-US";
          url  = new URL(urlStr);
          conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setConnectTimeout(10000);
          conn.setReadTimeout(10000);
          if (conn.getResponseCode() == 200) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();
            JSONArray enVideos = new JSONObject(sb.toString()).getJSONArray("results");
            for (int i = 0; i < enVideos.length(); i++) {
              JSONObject v = enVideos.getJSONObject(i);
              if (v.getString("type").equals("Trailer") && v.getString("site").equals("YouTube")) {
                foundUrl = "https://www.youtube.com/watch?v=" + v.getString("key");
                break;
              }
            }
          }
        }
        final String finalUrl = foundUrl;
        mainHandler.post(() -> {
          if (finalUrl != null) {
            trailerUrl = finalUrl;
            btnTrailer.setVisibility(View.VISIBLE);
            btnTrailer.setOnClickListener(v -> {
              android.content.Intent intent = new android.content.Intent(
                  android.content.Intent.ACTION_VIEW,
                  android.net.Uri.parse(trailerUrl));
              startActivity(intent);
            });
          }
        });
      } catch (Exception e) {
        // Échec silencieux pour le trailer : non bloquant
        e.printStackTrace();
      }
    });
  }

  private void fetchFullDetails(int serieId) {
    btnSave.setEnabled(false);
    btnSave.setAlpha(0.5f);

    executor.execute(() -> {
      try {
        String urlStr = TmdbApiService.BASE_URL
            + "/tv/" + serieId
            + "?api_key=" + TmdbApiService.getApiKey(getApplicationContext())
            + "&language=fr-FR";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() != 200) {
          mainHandler.post(() -> {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
            Toast.makeText(DetailsActivity.this,
                "Erreur réseau. Vérifiez votre connexion.", Toast.LENGTH_LONG).show();
          });
          return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        JSONObject obj = new JSONObject(sb.toString());
        String title       = obj.getString("name");
        String tagline     = obj.optString("tagline", "");
        String releaseDate = obj.optString("first_air_date", "");
        double rating      = obj.optDouble("vote_average", 0.0);
        String synopsis    = obj.optString("overview", "");
        int    seasons     = obj.optInt("number_of_seasons", 0);
        int    episodes    = obj.optInt("number_of_episodes", 0);
        int runTime = 0;
        if (obj.has("episode_run_time")) {
          JSONArray rtArray = obj.getJSONArray("episode_run_time");
          if (rtArray.length() > 0) runTime = rtArray.getInt(0);
        }
        String network = "";
        if (obj.has("networks") && obj.getJSONArray("networks").length() > 0) {
          network = obj.getJSONArray("networks").getJSONObject(0).getString("name");
        }
        String status       = obj.optString("status", "");
        String posterPath   = obj.isNull("poster_path")   ? null : obj.optString("poster_path", null);
        String backdropPath = obj.isNull("backdrop_path") ? null : obj.optString("backdrop_path", null);
        int totalHeures = (episodes * runTime) / 60;

        final Serie full = new Serie(obj.getInt("id"), posterPath, backdropPath,
            title, tagline, releaseDate, rating, synopsis, seasons, episodes, runTime, network, status);
        final int fHeures = totalHeures;

        mainHandler.post(() -> {
          currentSerie = full;
          bindFullData(full, fHeures);
          btnSave.setEnabled(true);
          btnSave.setAlpha(1.0f);
          fetchTrailer(full.getId());
        });

      } catch (Exception e) {
        // Gestion réseau : connexion absente ou timeout
        e.printStackTrace();
        mainHandler.post(() -> {
          btnSave.setEnabled(true);
          btnSave.setAlpha(1.0f);
          Toast.makeText(DetailsActivity.this,
              "Erreur réseau. Vérifiez votre connexion.", Toast.LENGTH_LONG).show();
        });
      }
    });
  }

  private void bindFullData(Serie s, int heures) {
    tvTitle.setText(s.getTitle());
    tvRating.setText(String.format("%.0f", s.getTmdbRating()));
    tvSynopsis.setText(s.getSynopsis());
    tvReleaseDate.setText(s.getReleaseDate());
    if (s.getTagline() != null && !s.getTagline().isEmpty()) {
      tvTagline.setText("\"" + s.getTagline() + "\"");
      tvTagline.setVisibility(View.VISIBLE);
    } else {
      tvTagline.setVisibility(View.GONE);
    }
    tvSeasons.setText(s.getNumberOfSeasons() > 0  ? String.format("%02d", s.getNumberOfSeasons())  : "--");
    tvEpisodes.setText(s.getNumberOfEpisodes() > 0 ? String.format("%02d", s.getNumberOfEpisodes()) : "--");
    tvHours.setText(heures > 0 ? String.format("%02d", heures) : "--");
    tvPlatform.setText(s.getNetwork() != null && !s.getNetwork().isEmpty() ? s.getNetwork() : "N/A");
    String imagePath = s.getBackdropPath() != null ? s.getBackdropPath() : s.getPosterPath();
    if (imagePath != null && !imagePath.isEmpty()) {
      Glide.with(this)
          .load(TmdbApiService.IMAGE_BASE_URL + imagePath)
          .centerCrop()
          .placeholder(R.mipmap.ic_placeholder)
          .into(ivBackdrop);
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // FORMULAIRE
  // ─────────────────────────────────────────────────────────────────

  private void setupSpinners() {
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, statusOptions);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerAvisStatus.setAdapter(adapter);
  }

  private void setupDatePicker() {
    etDateVisionnage.setOnClickListener(v -> {
      Calendar cal = Calendar.getInstance();
      new DatePickerDialog(this,
          (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            etDateVisionnage.setText(date);
          },
          cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
      ).show();
    });
  }

  private void setupSaveButton() {
    btnSave.setOnClickListener(v -> {
      if (validateForm()) saveLocalSerie();
    });
  }

  private boolean validateForm() {
    boolean isValid = true;
    String avis = etAvis.getText() != null ? etAvis.getText().toString().trim() : "";
    if (avis.length() < 10) {
      layoutAvis.setError("L'avis doit contenir au moins 10 caractères");
      isValid = false;
    } else {
      layoutAvis.setError(null);
    }
    String noteStr = etNote.getText() != null ? etNote.getText().toString().trim() : "";
    if (noteStr.isEmpty()) {
      layoutNote.setError("Note requise");
      isValid = false;
    } else {
      try {
        int note = Integer.parseInt(noteStr);
        if (note < 1 || note > 5) {
          layoutNote.setError("Entre 1 et 5");
          isValid = false;
        } else {
          layoutNote.setError(null);
        }
      } catch (NumberFormatException e) {
        layoutNote.setError("Invalide");
        isValid = false;
      }
    }
    String date = etDateVisionnage.getText() != null ? etDateVisionnage.getText().toString().trim() : "";
    if (date.isEmpty()) {
      layoutDateVisionnage.setError("Date requise");
      isValid = false;
    } else {
      layoutDateVisionnage.setError(null);
    }
    return isValid;
  }

  // ─────────────────────────────────────────────────────────────────
  // SAUVEGARDE
  // ─────────────────────────────────────────────────────────────────

  private void saveLocalSerie() {
    if (currentSerie == null) return;
    String userStatus     = spinnerAvisStatus.getSelectedItem().toString();
    float  personalRating = Float.parseFloat(etNote.getText().toString().trim());
    String personalReview = etAvis.getText().toString().trim();
    String watchDate      = etDateVisionnage.getText().toString().trim();

    LocalSerie localSerie = new LocalSerie(currentSerie, userStatus,
        personalRating, personalReview, watchDate);

    executor.execute(() -> {
      boolean success = LocalSerieService.saveOrUpdate(getApplicationContext(), localSerie);
      mainHandler.post(() -> {
        if (success) {
          // Toast obligatoire (CDC) : feedback immédiat à l'utilisateur
          Toast.makeText(this, "Ajouté à votre collection !", Toast.LENGTH_SHORT).show();
          showSuccessModal();
        } else {
          layoutAvis.setError("Erreur lors de la sauvegarde");
        }
      });
    });
  }

  // ─────────────────────────────────────────────────────────────────
  // MODAL SUCCÈS
  // ─────────────────────────────────────────────────────────────────

  private void showSuccessModal() {
    Dialog dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.modal_success);
    dialog.setCancelable(false);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    params.y = 120;
    dialog.getWindow().setAttributes(params);
    dialog.show();
    new Handler().postDelayed(() -> {
      if (dialog.isShowing()) dialog.dismiss();
    }, 2000);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
  }
}
