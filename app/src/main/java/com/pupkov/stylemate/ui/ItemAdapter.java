package com.pupkov.stylemate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для горизонтального списка товаров (вещей), входящих в выбранный образ.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items = new ArrayList<>();
    private final OnItemClickListener listener;

    /**
     * Интерфейс для обратного вызова при клике на товар
     */
    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    public ItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Обновление данных в адаптере
     */
    public void updateList(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged(); // Полная перерисовка списка при изменении данных
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.tvName.setText(item.getType());
        holder.tvBrand.setText(item.getBrand());
        holder.tvPrice.setText(item.getPrice());
        holder.tvMaterial.setText(item.getMaterial());

        // Передача ссылки во внешний обработчик при клике на карточку товара
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.getLink() != null && !item.getLink().isEmpty()) {
                listener.onItemClick(item.getLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Кэш для View-компонентов отдельной карточки товара, снижающий нагрузку при скролле
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvBrand, tvMaterial;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProdName);
            tvPrice = itemView.findViewById(R.id.tvProdPrice);
            tvBrand = itemView.findViewById(R.id.tvProdBrand);
            tvMaterial = itemView.findViewById(R.id.tvProdMaterial);
        }
    }
}