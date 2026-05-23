package com.tp.cinetrack;

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
  // Clé API lue depuis local.properties via BuildConfig
  // Pour configurer : ajoute TMDB_API_KEY=ta_cle dans local.properties
  private static final String API_KEY = BuildConfig.TMDB_API_KEY;
  // ─────────────────────────────────────────────────────────────────

  static final String BASE_URL       = "https://api.themoviedb.org/3";
  public  static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

  /**
   * Vérifie si la clé API est configurée et valide.
   * Retourne false si local.properties est absent ou si la clé est vide.
   */
  public static boolean isApiKeyConfigured() {
    return API_KEY != null
        && !API_KEY.isEmpty()
        && !API_KEY.equals("REMPLACE_PAR_TA_CLE_TMDB");
  }

  /**
   * Recherche des séries sur TMDB par mot-clé.
   * Retourne une liste complète de Serie avec tous les champs disponibles.
   * Les champs absents dans /search/tv (saisons, épisodes...) sont à 0/"".
   * Doit être appelé depuis un thread background.
   */
  public static List<Serie> searchSeries(String query) {
    List<Serie> results = new ArrayList<>();
    if (!isApiKeyConfigured() || query == null || query.trim().isEmpty()) {
      return results;
    }

    try {
      String encodedQuery = query.trim().replace(" ", "%20");
      String urlStr = BASE_URL
          + "/search/tv?api_key=" + API_KEY
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

      // Parse JSON — format réel de GET /search/tv (TMDB)
      // Champs disponibles : id, name, tagline(absent), first_air_date,
      // vote_average, overview, poster_path, backdrop_path, status(absent)
      // Champs absents dans search : number_of_seasons, number_of_episodes,
      // episode_run_time, networks → complétés à 0/"" pour la carte,
      // seront chargés dans DetailsActivity via GET /tv/{id}
      JSONObject response    = new JSONObject(sb.toString());
      JSONArray  resultsArray = response.getJSONArray("results");

      for (int i = 0; i < resultsArray.length(); i++) {
        JSONObject obj = resultsArray.getJSONObject(i);

        String posterPath   = obj.isNull("poster_path")
            ? null : obj.getString("poster_path");
        String backdropPath = obj.isNull("backdrop_path")
            ? null : obj.getString("backdrop_path");

        Serie serie = new Serie(
            obj.getInt("id"),
            posterPath,
            backdropPath,
            obj.getString("name"),
            "",                                    // tagline : absent dans /search/tv
            obj.optString("first_air_date", ""),
            obj.optDouble("vote_average", 0.0),
            obj.optString("overview", ""),
            0,                                     // numberOfSeasons : absent dans /search/tv
            0,                                     // numberOfEpisodes : absent dans /search/tv
            0,                                     // episodeRunTime : absent dans /search/tv
            "",                                    // network : absent dans /search/tv
            ""                                     // productionStatus : absent dans /search/tv
        );
        results.add(serie);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }
}