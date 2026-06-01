package com.pupkov.stylemate.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.ProfileViewModel;
import com.pupkov.stylemate.repository.ActiveUserInfo;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private FavoriteOutfitsAdapter adapter;

    private TextView tvEmptyState;
    private TextView tvFavoritesTitle;
    private RecyclerView rvFavorites;
    private ImageView imgAvatar;
    private TextView tvUserName;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupClickListeners(view);
        observeViewModel();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && viewModel != null) {
            viewModel.refreshData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    private void initViews(View view) {
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvFavorites.setLayoutManager(layoutManager);

        adapter = new FavoriteOutfitsAdapter(getContext(), new ArrayList<>(), item -> {
            Intent intent = new Intent(requireContext(), CollectionDetailActivity.class);
            intent.putExtra("COLLECTION_TITLE", item.getTitle());
            intent.putExtra("COLLECTION_ID", item.getId());
            startActivity(intent);
        });

        rvFavorites.setAdapter(adapter);
    }

    private void setupClickListeners(View view) {
        View btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        View btnChangeTheme = view.findViewById(R.id.btnChangeTheme);
        if (btnChangeTheme != null) {
            btnChangeTheme.setOnClickListener(v -> {
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    ActiveUserInfo.setDefaults("theme", "", requireContext());
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    ActiveUserInfo.setDefaults("theme", "night", requireContext());
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.userName.observe(getViewLifecycleOwner(), name -> {
            if (name != null) tvUserName.setText(name);
        });

        viewModel.userAvatarUrl.observe(getViewLifecycleOwner(), url -> {
            if (imgAvatar != null) {
                if (url != null && !url.isEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(url)
                            .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_placeholder_avatar)
                            .error(R.drawable.ic_placeholder_avatar)
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
                }
            }
        });

        viewModel.favorites.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.updateList(list);
            }
        });

        viewModel.navigateToHomeEvent.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                navigateToHome();
            }
        });

        viewModel.isEmptyState.observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvFavoritesTitle.setVisibility(View.GONE);
                rvFavorites.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                tvFavoritesTitle.setVisibility(View.VISIBLE);
                rvFavorites.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            View btnHome = getActivity().findViewById(R.id.btnHome);
            if (btnHome != null) {
                btnHome.performClick();
            }
        }
    }
}