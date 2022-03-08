package com.jwindustries.isitvegan.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.jwindustries.isitvegan.IngredientAdapter;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

public class IngredientListFragment extends Fragment {
    private Activity hostActivity;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private IngredientAdapter adapter;
    private View scrollToTopButton;
    private RecyclerView.OnScrollListener scrollListener;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        hostActivity = getActivity();

        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_ingredient_list, container, false);

        layoutManager = new LinearLayoutManager(hostActivity, LinearLayoutManager.VERTICAL, false);
        adapter = new IngredientAdapter(hostActivity);

        recyclerView = rootView.findViewById(R.id.ingredient_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        ((DragScrollBar) rootView.findViewById(R.id.dragScrollBar)).setIndicator(new AlphabetIndicator(hostActivity), true);

        scrollToTopButton = rootView.findViewById(R.id.scroll_to_top_button);
        scrollToTopButton.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        SearchView searchView = rootView.findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterItems(s);
                return false;
            }
        });

        initFilterChips();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.handleScrollToTopButton();
    }

    private void initFilterChips() {
        boolean showVegan = Utils.getShowVegan(hostActivity);
        boolean showMaybeVegan = Utils.getShowMaybeVegan(hostActivity);
        boolean showNonVegan = Utils.getShowNonVegan(hostActivity);

        Chip veganChip = rootView.findViewById(R.id.chip_vegan);
        veganChip.setChecked(showVegan);
        veganChip.setOnCheckedChangeListener((view, checked) -> {
            Utils.setShowVegan(hostActivity, checked);
            adapter.filterItems();
        });

        Chip maybeChip = rootView.findViewById(R.id.chip_maybe_vegan);
        maybeChip.setChecked(showMaybeVegan);
        maybeChip.setOnCheckedChangeListener((view, checked) -> {
            Utils.setShowMaybeVegan(hostActivity, checked);
            adapter.filterItems();
        });

        Chip notVeganChip = rootView.findViewById(R.id.chip_not_vegan);
        notVeganChip.setChecked(showNonVegan);
        notVeganChip.setOnCheckedChangeListener((view, checked) -> {
            Utils.setShowNonVegan(hostActivity, checked);
            adapter.filterItems();
        });
    }

    private void handleScrollToTopButton() {
        int positionThreshold = 4;

        boolean initiallyShowing;
        int topItemPosition = this.layoutManager.findFirstVisibleItemPosition();
        if (topItemPosition >= positionThreshold) {
            scrollToTopButton.setAlpha(1f);
            initiallyShowing = true;
        } else {
            scrollToTopButton.setAlpha(0f);
            initiallyShowing = false;
        }

        // Remove and add scroll listener
        this.recyclerView.removeOnScrollListener(this.scrollListener);
        this.scrollListener = new RecyclerView.OnScrollListener() {
            private boolean showingScrollToTopButton = initiallyShowing;
            private boolean firstTime = true;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int topItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Show button
                if (!showingScrollToTopButton && topItemPosition >= positionThreshold) {

                    // Scroll view first, now that its height has been calculated
                    if (firstTime) {
                        scrollToTopButton.setTranslationY(scrollToTopButton.getHeight());
                        firstTime = false;
                    }

                    scrollToTopButton.animate().alpha(1f).translationY(0).setDuration(100);
                    showingScrollToTopButton = true;
                }

                // Hide button
                if (showingScrollToTopButton && topItemPosition < positionThreshold) {
                    scrollToTopButton.animate().alpha(0f).translationY(scrollToTopButton.getHeight()).setDuration(100);
                    showingScrollToTopButton = false;
                }
            }
        };
        this.recyclerView.addOnScrollListener(scrollListener);
    }
}