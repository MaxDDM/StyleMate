package com.example.stylemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteOutfitsAdapter extends RecyclerView.Adapter<FavoriteOutfitsAdapter.OutfitViewHolder> {

    // Сюда нам прилетит список данных
    private List<FavouriteOutfits> outfitList;

    // 1. НОВОЕ: Поле для слушателя кликов
    private OnItemClickListener listener;

    // 2. НОВОЕ: Интерфейс для передачи клика
    public interface OnItemClickListener {
        void onItemClick(FavouriteOutfits item);
    }

    // 3. ОБНОВИЛИ КОНСТРУКТОР: теперь принимаем и слушателя
    public FavoriteOutfitsAdapter(List<FavouriteOutfits> outfitList, OnItemClickListener listener) {
        this.outfitList = outfitList;
        this.listener = listener;
    }

    // 1. Этот метод создает "формочку" (View) из нашего XML файла
    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Надуваем" (inflate) наш xml файл
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.preview_favorite_outfits, parent, false);
        return new OutfitViewHolder(view);
    }

    // 2. Этот метод берет данные из списка и рассовывает их по картинкам и текстам
    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        // Получаем конкретный объект (подборку) по номеру позиции
        FavouriteOutfits outfit = outfitList.get(position);

        // Устанавливаем текст
        holder.tvTitle.setText(outfit.title);

        // Устанавливаем картинки
        // (R.drawable.shoes — это int, поэтому используем setImageResource)
        holder.img1.setImageResource(outfit.photo1);
        holder.img2.setImageResource(outfit.photo2);
        holder.img3.setImageResource(outfit.photo3);
        holder.img4.setImageResource(outfit.photo4);

        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(outfit);
        });
    }

    // 3. Этот метод говорит списку, сколько у нас всего элементов
    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    // ВНУТРЕННИЙ КЛАСС (ViewHolder)
    // Он хранит ссылки на элементы View, чтобы не искать их каждый раз
    public static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView img1, img2, img3, img4;
        TextView tvTitle;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);

            // Находим элементы в нашем preview_favorite_outfits.xml
            img1 = itemView.findViewById(R.id.img1);
            img2 = itemView.findViewById(R.id.img2);
            img3 = itemView.findViewById(R.id.img3);
            img4 = itemView.findViewById(R.id.img4);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}