package com.tp.cinetrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class LocalSerieCard
    extends RecyclerView.Adapter<LocalSerieCard.ViewHolder> {

  public interface OnClickListener     { void onClick(LocalSerie serie); }
  public interface OnLongClickListener { void onLongClick(LocalSerie serie); }

  private final Context             context;
  private       List<LocalSerie>    series;
  private final OnClickListener     onClick;
  private final OnLongClickListener onLongClick;

  public LocalSerieCard(Context context, List<LocalSerie> series,
                           OnClickListener onClick,
                           OnLongClickListener onLongClick) {
    this.context     = context;
    this.series      = series;
    this.onClick     = onClick;
    this.onLongClick = onLongClick;
  }

  public void updateData(List<LocalSerie> newList) {
    this.series = newList;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context)
        .inflate(R.layout.item_serie_card, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    LocalSerie serie = series.get(position);

    holder.tvTitle.setText(serie.getTitle());

    // Année
    String year = (serie.getReleaseDate() != null
        && serie.getReleaseDate().length() >= 4)
        ? serie.getReleaseDate().substring(0, 4) : "----";
    holder.tvYear.setText(year);

    // Note personnelle de l'utilisateur (sur 5)
    holder.tvRating.setText(serie.getPersonalRating() > 0
        ? String.format("%.0f/5", serie.getPersonalRating())
        : "-/5");

    // Image — les URLs sont déjà complètes dans le fichier local
    String imageUrl = serie.getPosterPath();
    if (imageUrl != null && !imageUrl.isEmpty()) {
      Glide.with(context)
          .load(imageUrl)
          .placeholder(R.mipmap.ic_placeholder_card)
          .error(R.mipmap.ic_placeholder_card)
          .centerCrop()
          .into(holder.ivPoster);
    } else {
      holder.ivPoster.setImageResource(R.mipmap.ic_placeholder_card);
    }

    // Clic simple → DetailsActivity
    holder.itemView.setOnClickListener(v -> onClick.onClick(serie));

    // Clic long → modal options
    holder.itemView.setOnLongClickListener(v -> {
      onLongClick.onLongClick(serie);
      return true;
    });
  }

  @Override
  public int getItemCount() { return series.size(); }

  static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ivPoster;
    TextView  tvTitle, tvYear, tvRating;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      ivPoster = itemView.findViewById(R.id.iv_poster);
      tvTitle  = itemView.findViewById(R.id.tv_title);
      tvYear   = itemView.findViewById(R.id.tv_year_label);
      tvRating = itemView.findViewById(R.id.tv_rating);
    }
  }
}