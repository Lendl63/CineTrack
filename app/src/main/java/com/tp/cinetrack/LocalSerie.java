package com.tp.cinetrack;

public class LocalSerie extends Serie {
  private String userStatus;
  private float personalRating;
  private String personalReview;
  private String watchDate;

  // Constructeur vide
  public LocalSerie() {
    super();
  }

  public LocalSerie(Serie tmdbSerie, String userStatus, float personalRating, String personalReview, String watchDate) {
    // Appel immédiat du constructeur complet de la classe mère
    super(
        tmdbSerie.getId(),
        tmdbSerie.getPosterPath(),
        tmdbSerie.getBackdropPath(),
        tmdbSerie.getTitle(),
        tmdbSerie.getTagline(),
        tmdbSerie.getReleaseDate(),
        tmdbSerie.getTmdbRating(),
        tmdbSerie.getSynopsis(),
        tmdbSerie.getNumberOfSeasons(),
        tmdbSerie.getNumberOfEpisodes(),
        tmdbSerie.getEpisodeRunTime(),
        tmdbSerie.getNetwork(),
        tmdbSerie.getProductionStatus()
    );

    // Initialisation des champs spécifiques à l'utilisateur
    this.userStatus = userStatus;
    this.personalRating = personalRating;
    this.personalReview = personalReview;
    this.watchDate = watchDate;
  }

  public String getUserStatus() {
    return userStatus;
  }

  public void setUserStatus(String userStatus) {
    this.userStatus = userStatus;
  }

  public float getPersonalRating() {
    return personalRating;
  }

  public void setPersonalRating(float personalRating) {
    this.personalRating = personalRating;
  }

  public String getPersonalReview() {
    return personalReview;
  }

  public void setPersonalReview(String personalReview) {
    this.personalReview = personalReview;
  }

  public String getWatchDate() {
    return watchDate;
  }

  public void setWatchDate(String watchDate) {
    this.watchDate = watchDate;
  }
}
