package com.tp.cinetrack;

import java.io.Serializable;

public class Serie implements Serializable {
  private int id;
  private String posterPath;
  private String backdropPath;
  private String title;
  private String tagline;
  private String releaseDate;
  private double tmdbRating;
  private String synopsis;
  private int numberOfSeasons;
  private int numberOfEpisodes;
  private int episodeRunTime;
  private String network;
  private String productionStatus;

  // Constructeur vide (nécessaire pour certaines sérialisations)
  public Serie() {}

  // Constructeur complet de la classe mère
  public Serie(int id, String posterPath, String backdropPath, String title, String tagline,
               String releaseDate, double tmdbRating, String synopsis, int numberOfSeasons,
               int numberOfEpisodes, int episodeRunTime, String network, String productionStatus) {
    this.id = id;
    this.posterPath = posterPath;
    this.backdropPath = backdropPath;
    this.title = title;
    this.tagline = tagline;
    this.releaseDate = releaseDate;
    this.tmdbRating = tmdbRating;
    this.synopsis = synopsis;
    this.numberOfSeasons = numberOfSeasons;
    this.numberOfEpisodes = numberOfEpisodes;
    this.episodeRunTime = episodeRunTime;
    this.network = network;
    this.productionStatus = productionStatus;
  }
  public int getId() { return id; }

  public String getBackdropPath() {
    return backdropPath;
  }

  public String getPosterPath() {
    return posterPath;
  }

  public String getTitle() {
    return title;
  }

  public String getTagline() {
    return tagline;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public double getTmdbRating() {
    return tmdbRating;
  }

  public String getSynopsis() {
    return synopsis;
  }

  public int getNumberOfSeasons() {
    return numberOfSeasons;
  }

  public int getNumberOfEpisodes() {
    return numberOfEpisodes;
  }

  public int getEpisodeRunTime() {
    return episodeRunTime;
  }

  public String getNetwork() {
    return network;
  }

  public String getProductionStatus() {
    return productionStatus;
  }

  public void setPosterPath(String posterPath) {
    this.posterPath = posterPath;
  }

  public void setBackdropPath(String backdropPath) {
    this.backdropPath = backdropPath;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  public void setTmdbRating(double tmdbRating) {
    this.tmdbRating = tmdbRating;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }

  public void setSynopsis(String synopsis) {
    this.synopsis = synopsis;
  }

  public void setNumberOfSeasons(int numberOfSeasons) {
    this.numberOfSeasons = numberOfSeasons;
  }

  public void setNumberOfEpisodes(int numberOfEpisodes) {
    this.numberOfEpisodes = numberOfEpisodes;
  }

  public void setEpisodeRunTime(int episodeRunTime) {
    this.episodeRunTime = episodeRunTime;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public void setProductionStatus(String productionStatus) {
    this.productionStatus = productionStatus;
  }
}
