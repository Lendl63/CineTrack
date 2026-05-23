package com.tp.cinetrack;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SerieCard extends RecyclerView.Adapter<SerieCard.SerieViewHolder> {

  private final Context    context;
  private       List<Serie> series;

  public SerieCard(Context context, List<Serie> series) {
    this.context = context;
    this.series  = series;
  }

  public void updateData(List<Serie> newList) {
    this.series = newList;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public SerieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context)
        .inflate(R.layout.item_serie_card, parent, false);
    return new SerieViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull SerieViewHolder holder, int position) {
    Serie serie = series.get(position);

    // Titre
    holder.tvTitle.setText(serie.getTitle());

    // Année extraite depuis releaseDate TMDB (format "2021-09-24")
    String year = (serie.getReleaseDate() != null
        && serie.getReleaseDate().length() >= 4)
        ? serie.getReleaseDate().substring(0, 4)
        : "----";
    holder.tvYear.setText(year);

    // Note TMDB sur 10
    holder.tvRating.setText(
        String.format("%.1f/10", serie.getTmdbRating()));

    // Poster : URL TMDB ou placeholder si posterPath est null
    if (serie.getPosterPath() != null && !serie.getPosterPath().isEmpty()) {
      Glide.with(context)
          .load(TmdbApiService.IMAGE_BASE_URL + serie.getPosterPath())
          .placeholder(R.mipmap.ic_placeholder_card)
          .error(R.mipmap.ic_placeholder_card)
          .centerCrop()
          .into(holder.ivPoster);
    } else {
      holder.ivPoster.setImageResource(R.mipmap.ic_placeholder_card);
    }

    // Clic sur la carte → ouvre DetailsActivity avec la Serie complète
    holder.itemView.setOnClickListener(v -> {
      Intent intent = new Intent(context, DetailsActivity.class);
      // La Serie est Serializable : on la passe directement en extra
      intent.putExtra("serie", serie);
      context.startActivity(intent);
    });
  }

  @Override
  public int getItemCount() { return series.size(); }

  static class SerieViewHolder extends RecyclerView.ViewHolder {
    ImageView ivPoster;
    TextView  tvTitle, tvYear, tvRating;

    SerieViewHolder(@NonNull View itemView) {
      super(itemView);
      ivPoster = itemView.findViewById(R.id.iv_poster);
      tvTitle  = itemView.findViewById(R.id.tv_title);
      tvYear   = itemView.findViewById(R.id.tv_year_label);
      tvRating = itemView.findViewById(R.id.tv_rating);
    }
  }
}