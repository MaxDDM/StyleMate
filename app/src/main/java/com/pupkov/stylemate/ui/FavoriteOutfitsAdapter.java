package com.pupkov.stylemate.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pupkov.stylemate.R;

import java.util.List;

public class FavoriteOutfitsAdapter extends RecyclerView.Adapter<FavoriteOutfitsAdapter.OutfitViewHolder> {

    private List<FavouriteOutfits> outfitList;
    private OnItemClickListener listener;
    private Context context; // НУЖНО ДЛЯ GLIDE

    public interface OnItemClickListener {
        void onItemClick(FavouriteOutfits item);
    }

    // Обновленный конструктор: просим Context
    public FavoriteOutfitsAdapter(Context context, List<FavouriteOutfits> outfitList, OnItemClickListener listener) {
        this.context = context;
        this.outfitList = outfitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.preview_favorite_outfits, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        FavouriteOutfits outfit = outfitList.get(position);

        holder.tvTitle.setText(outfit.title);

        // Загружаем 4 картинки через хелпер-метод (см. ниже)
        loadImage(holder.img1, outfit.photo1);
        loadImage(holder.img2, outfit.photo2);
        loadImage(holder.img3, outfit.photo3);
        loadImage(holder.img4, outfit.photo4);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(outfit);
            }
        });
    }

    // Вспомогательный метод для загрузки
    private void loadImage(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .placeholder(android.R.color.darker_gray) // Создай цвет в colors.xml или используй android.R.color.darker_gray
                    .error(android.R.color.transparent)
                    .into(imageView);
        } else {
            // Если ссылки нет - очищаем картинку (будет пустой серый квадрат, если фон задан в XML)
            Glide.with(context).clear(imageView);
            imageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return (outfitList == null) ? 0 : outfitList.size();
    }

    public void updateList(List<FavouriteOutfits> newList) {
        this.outfitList = newList;
        notifyDataSetChanged();
    }

    public static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView img1, img2, img3, img4;
        TextView tvTitle;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            img1 = itemView.findViewById(R.id.img1);
            img2 = itemView.findViewById(R.id.img2);
            img3 = itemView.findViewById(R.id.img3);
            img4 = itemView.findViewById(R.id.img4);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}