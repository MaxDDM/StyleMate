package com.example.stylemate.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stylemate.R;
import com.example.stylemate.model.Outfit;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private List<Outfit> items;
    private final OnOutfitClickListener listener;

    // Цвета (подставь свои если нужно)
    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#5C5C5C");

    public interface OnOutfitClickListener {
        void onHeartClick(Outfit outfit, int position);
        void onImageClick(Outfit outfit);
    }

    public OutfitAdapter(List<Outfit> items, OnOutfitClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<Outfit> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit item = items.get(position);

        // 1. Картинка
        holder.imageView.setImageResource(item.getImageResId());

        // 2. Сердце (Красим программно)
        holder.btnLike.setImageResource(R.drawable.ic_heart_outline);

        if (item.isLiked()) {
            holder.btnLike.setColorFilter(COLOR_BLUE); // Красим в синий
        } else {
            holder.btnLike.setColorFilter(COLOR_GRAY); // Красим в серый
        }

        // 3. Клики
        holder.btnLike.setOnClickListener(v -> listener.onHeartClick(item, position));
        holder.itemView.setOnClickListener(v -> listener.onImageClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnLike;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivOutfitImage);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}