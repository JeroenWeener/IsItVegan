package com.jwindustries.isitvegan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView ingredientRecyclerView = this.findViewById(R.id.ingredient_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        ingredientRecyclerView.setLayoutManager(layoutManager);
        final IngredientAdapter adapter = new IngredientAdapter(this);
        ingredientRecyclerView.setAdapter(adapter);
        ingredientRecyclerView.addItemDecoration(new DividerItemDecoration(ingredientRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        SearchView searchView = this.findViewById(R.id.search_view);
        searchView.setOnClickListener(view -> searchView.setIconified(false));
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
    }
}