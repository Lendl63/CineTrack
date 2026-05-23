package com.tp.cinetrack;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TmdbApiService {

  // ─────────────────────────────────────────────────────────────────
  // Clé API lue depuis SharedPreferences (saisie dans les paramètres).
  // Fallback sur BuildConfig si SharedPreferences est vide
  // (utile en développement avec local.properties).
  // ─────────────────────────────────────────────────────────────────

  public static final String BASE_URL        = "https://api.themoviedb.org/3";
  public static final String IMAGE_BASE_URL  = "https://image.tmdb.org/t/p/w500";

  /**
   * Retourne la clé API : SharedPreferences en priorité, sinon BuildConfig.
   */
  public static String getApiKey(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(
        SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
    String key = prefs.getString(SettingsActivity.KEY_API_KEY, "");
    if (!key.isEmpty()) return key;

    // Fallback sur BuildConfig (valeur de local.properties au build)
    return BuildConfig.TMDB_API_KEY;
  }

  /**
   * Vérifie si une clé API valide est disponible.
   */
  public static boolean isApiKeyConfigured(Context context) {
    String key = getApiKey(context);
    return key != null
        && !key.isEmpty()
        && !key.equals("REMPLACE_PAR_TA_CLE_TMDB");
  }

  /**
   * Recherche des séries sur TMDB par mot-clé.
   * Doit être appelé depuis un thread background.
   */
  public static List<Serie> searchSeries(Context context, String query) {
    List<Serie> results = new ArrayList<>();
    if (!isApiKeyConfigured(context)
        || query == null || query.trim().isEmpty()) {
      return results;
    }

    try {
      String encodedQuery = query.trim().replace(" ", "%20");
      String urlStr = BASE_URL
          + "/search/tv?api_key=" + getApiKey(context)
          + "&query=" + encodedQuery
          + "&language=fr-FR";

      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(10000);
      if (conn.getResponseCode() != 200) return results;

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) sb.append(line);
      reader.close();
      conn.disconnect();

      JSONObject response     = new JSONObject(sb.toString());
      JSONArray  resultsArray = response.getJSONArray("results");

      for (int i = 0; i < resultsArray.length(); i++) {
        JSONObject obj = resultsArray.getJSONObject(i);

        String posterPath   = obj.isNull("poster_path")
            ? null : obj.getString("poster_path");
        String backdropPath = obj.isNull("backdrop_path")
            ? null : obj.getString("backdrop_path");

        results.add(new Serie(
            obj.getInt("id"),
            posterPath,
            backdropPath,
            obj.getString("name"),
            "",
            obj.optString("first_air_date", ""),
            obj.optDouble("vote_average", 0.0),
            obj.optString("overview", ""),
            0, 0, 0, "", ""
        ));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
}