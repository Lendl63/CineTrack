package com.tp.cinetrack;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class LocalSerieService {

  // Nom du fichier JSON local dans le dossier privé de l'app
  private static final String FILE_NAME = "series_locales.json";

  /**
   * Retourne le fichier JSON.
   * Le crée avec un tableau vide s'il n'existe pas encore.
   */
  private static File getOrCreateFile(Context context) {
    File file = new File(context.getFilesDir(), FILE_NAME);
    try {
      if (!file.exists()) {
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write("[]");
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return file;
  }

  /**
   * Lit toutes les séries sauvegardées depuis le fichier JSON.
   * Retourne une liste vide si le fichier est absent ou vide.
   */
  public static List<LocalSerie> readAll(Context context) {
    List<LocalSerie> list = new ArrayList<>();
    try {
      File file = getOrCreateFile(context);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) sb.append(line);
      reader.close();

      String content = sb.toString().trim();
      if (content.isEmpty()) return list;

      JSONArray array = new JSONArray(content);
      for (int i = 0; i < array.length(); i++) {
        list.add(fromJson(array.getJSONObject(i)));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * Sauvegarde une LocalSerie dans le fichier JSON.
   * En ecrasant le contenue du fichier
   */
  public static boolean saveOrUpdate(Context context, LocalSerie serie) {
    try {
      List<LocalSerie> list = readAll(context);

      // Remplace si l'id existe déjà, sinon ajoute
      boolean found = false;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).getId() == serie.getId()) {
          list.set(i, serie);
          found = true;
          break;
        }
      }
      if (!found) list.add(serie);

      // Réécrit tout le fichier
      JSONArray array = new JSONArray();
      for (LocalSerie s : list) array.put(toJson(s));

      File file = getOrCreateFile(context);
      FileWriter writer = new FileWriter(file, false);
      writer.write(array.toString(2)); // indentation pour lisibilité
      writer.close();
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  /**
   * Supprime la série avec l'id donné du fichier JSON local.
   */
  public static boolean delete(Context context, int serieId) {
    try {
      List<LocalSerie> list = readAll(context);
      list.removeIf(s -> s.getId() == serieId);

      JSONArray array = new JSONArray();
      for (LocalSerie s : list) array.put(toJson(s));

      File file = getOrCreateFile(context);
      FileWriter writer = new FileWriter(file, false);
      writer.write(array.toString(2));
      writer.close();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Convertit un JSONObject (format fichier) en LocalSerie.
   */
  private static LocalSerie fromJson(JSONObject obj) throws Exception {
    Serie tmdb = new Serie(
        obj.getInt("id"),
        obj.isNull("posterPath")   ? null : obj.getString("posterPath"),
        obj.isNull("backdropPath") ? null : obj.getString("backdropPath"),
        obj.optString("title", ""),
        obj.optString("tagline", ""),
        obj.optString("releaseDate", ""),
        obj.optDouble("tmdbRating", 0.0),
        obj.optString("synopsis", ""),
        obj.optInt("numberOfSeasons", 0),
        obj.optInt("numberOfEpisodes", 0),
        obj.optInt("episodeRunTime", 0),
        obj.optString("network", ""),
        obj.optString("productionStatus", "")
    );
    return new LocalSerie(
        tmdb,
        obj.optString("userStatus", ""),
        (float) obj.optDouble("personalRating", 0.0),
        obj.optString("personalReview", ""),
        obj.optString("watchDate", "")
    );
  }

  /**
   * Convertit une LocalSerie en JSONObject pour le fichier.
   * Les chemins d'image sont stockés en URL complète pour l'affichage au cas ou l'utilisateur ce connecte
   */
  private static JSONObject toJson(LocalSerie s) throws Exception {
    JSONObject obj = new JSONObject();

    // Données TMDB
    obj.put("id",               s.getId());
    obj.put("title",            s.getTitle());
    obj.put("tagline",          s.getTagline() != null ? s.getTagline() : "");
    obj.put("releaseDate",      s.getReleaseDate() != null ? s.getReleaseDate() : "");
    obj.put("tmdbRating",       s.getTmdbRating());
    obj.put("synopsis",         s.getSynopsis() != null ? s.getSynopsis() : "");
    obj.put("numberOfSeasons",  s.getNumberOfSeasons());
    obj.put("numberOfEpisodes", s.getNumberOfEpisodes());
    obj.put("episodeRunTime",   s.getEpisodeRunTime());
    obj.put("network",          s.getNetwork() != null ? s.getNetwork() : "");
    obj.put("productionStatus", s.getProductionStatus() != null ? s.getProductionStatus() : "");

    // URLs complètes des images sont construit pour l'affichage
    obj.put("posterPath", s.getPosterPath() != null
        ? TmdbApiService.IMAGE_BASE_URL + s.getPosterPath()
        : JSONObject.NULL);
    obj.put("backdropPath", s.getBackdropPath() != null
        ? TmdbApiService.IMAGE_BASE_URL + s.getBackdropPath()
        : JSONObject.NULL);

    // Données utilisateur
    obj.put("userStatus",     s.getUserStatus() != null ? s.getUserStatus() : "");
    obj.put("personalRating", s.getPersonalRating());
    obj.put("personalReview", s.getPersonalReview() != null ? s.getPersonalReview() : "");
    obj.put("watchDate",      s.getWatchDate() != null ? s.getWatchDate() : "");

    return obj;
  }
}