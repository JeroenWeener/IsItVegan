package com.jwindustries.isitvegan;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

public class IngredientOverviewActivity extends BaseActivity {
    private String appLocale;
    private String ingredientsLocale;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private IngredientAdapter adapter;
    private View scrollToTopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleTheme(this);

        this.appLocale = Utils.handleAppLocale(this);
        this.ingredientsLocale = Utils.getIngredientLocale(this);
        // Reset title as locale may have changed
        this.setTitle(R.string.app_name);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        recyclerView = this.findViewById(R.id.ingredient_view);
        scrollToTopButton = this.findViewById(R.id.scroll_to_top_button);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new IngredientAdapter(this);
        recyclerView.setAdapter(adapter);

        /*
         * Handle scrollToTopButton
         */
        scrollToTopButton.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
    }

    @Override
    public void onRestart() {
        super.onRestart();

        // Recreate when language has changed
        if (!Utils.getAppLocale(this).equals(this.appLocale) ||
                !Utils.getIngredientLocale(this).equals(this.ingredientsLocale)) {
            this.recreate();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        this.handleScrollToTopButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.searchbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.searchView);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        final IngredientOverviewActivity activity = this;

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                searchView.requestFocus();
                activity.showKeyboard();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                searchView.setQuery("", false);
                activity.hideKeyboard();
                return true;
            }
        });

        searchView.setIconifiedByDefault(false);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void handleScrollToTopButton() {
        int positionThreshold = 4;

        boolean initiallyShowing;
        int topItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (topItemPosition >= positionThreshold) {
            scrollToTopButton.setAlpha(1f);
            initiallyShowing = true;
        } else {
            scrollToTopButton.setAlpha(0f);
            initiallyShowing = false;
        }

        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        });
    }
}