package com.pupkov.stylemate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.Story;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<Story> stories;
    private final OnStoryClickListener clickListener;

    // Интерфейс для обработки кликов во фрагменте
    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    public StoryAdapter(List<Story> stories, OnStoryClickListener clickListener) {
        this.stories = stories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = stories.get(position);
        holder.bind(story, clickListener);
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    // Метод для обновления данных из LiveData
    public void updateStories(List<Story> newStories) {
        this.stories.clear();
        if (newStories != null) {
            this.stories.addAll(newStories);
        }
        notifyDataSetChanged();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivStoryImage;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStoryImage = itemView.findViewById(R.id.ivStoryImage);
        }

        public void bind(final Story story, final OnStoryClickListener listener) {
            // Загружаем картинку через Glide
            // Благодаря ShapeableImageView в XML, трансформация в круг произойдет автоматически через стили
            Glide.with(itemView.getContext())
                    .load(story.getImageUrl())
                    .placeholder(android.R.color.darker_gray) // Временная заглушка при загрузке
                    .error(android.R.color.holo_red_light)    // Если ссылка битая
                    .into(ivStoryImage);

            // Обработка нажатия на историю
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoryClick(story);
                }
            });
        }
    }
}