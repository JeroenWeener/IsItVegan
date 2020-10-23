package com.jwindustries.isitvegan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView ingredientRecyclerView = this.findViewById(R.id.ingredient_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        ingredientRecyclerView.setLayoutManager(layoutManager);
        IngredientAdapter adapter = new IngredientAdapter(IngredientList.INGREDIENTS);
        ingredientRecyclerView.setAdapter(adapter);
    }
}