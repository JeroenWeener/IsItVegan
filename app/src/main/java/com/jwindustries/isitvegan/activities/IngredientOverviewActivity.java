package com.jwindustries.isitvegan.activities;

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

import com.jwindustries.isitvegan.IngredientAdapter;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.jwindustries.isitvegan.scanning.ScanActivity;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

public class IngredientOverviewActivity extends BaseActivity {
    private String appLocale;
    private String ingredientsLocale;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private IngredientAdapter adapter;
    private View scrollToTopButton;
    private RecyclerView.OnScrollListener scrollListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleTheme(this);

        this.appLocale = Utils.handleAppLocale(this);
        this.ingredientsLocale = Utils.getIngredientLocale(this);
        // Reset title as locale may have changed
        this.setTitle(R.string.app_name);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_overview);

        this.layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.adapter = new IngredientAdapter(this);

        this.recyclerView = this.findViewById(R.id.ingredient_view);
        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setAdapter(adapter);

        ((DragScrollBar) this.findViewById(R.id.dragScrollBar)).setIndicator(new AlphabetIndicator(this), true);

        this.scrollToTopButton = this.findViewById(R.id.scroll_to_top_button);
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
        this.getMenuInflater().inflate(R.menu.actionbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_view);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(this.getString(R.string.search_view_hint));
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
        if (item.getItemId() == R.id.action_camera) {
            this.startActivity(new Intent(this, ScanActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
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