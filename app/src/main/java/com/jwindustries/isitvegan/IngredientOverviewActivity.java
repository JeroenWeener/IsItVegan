package com.jwindustries.isitvegan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

public class IngredientOverviewActivity extends AppCompatActivity {
    private IngredientAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView ingredientRecyclerView = this.findViewById(R.id.ingredient_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        ingredientRecyclerView.setLayoutManager(layoutManager);
        adapter = new IngredientAdapter(this);
        ingredientRecyclerView.setAdapter(adapter);
        ingredientRecyclerView.addItemDecoration(
                new DividerItemDecoration(ingredientRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onStart() {
        this.handleTheme();
        super.onStart();
    }

    @Override
    public void onResume() {
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        super.onResume();
    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        super.onPause();
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
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void handleTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeSetting = prefs.getString("theme", "system");
        switch (themeSetting) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    private void hideKeyboard() {
        // Check if view has focus
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}