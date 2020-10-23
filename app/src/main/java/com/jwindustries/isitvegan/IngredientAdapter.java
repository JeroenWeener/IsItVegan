package com.jwindustries.isitvegan;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Ingredient> ingredientList;

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        public TextView ingredientNameView;

        public IngredientViewHolder(TextView v) {
            super(v);
            this.ingredientNameView = v;
            System.out.println("2");
        }
    }

    public IngredientAdapter(List<Ingredient> ingredients) {
        this.ingredientList = ingredients;
        System.out.println("1");
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_text_view, parent, false);
        return new IngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.ingredientNameView.setText(ingredientList.get(position).getName());
        System.out.println("3");
    }

    @Override
    public int getItemCount() {
        return this.ingredientList.size();
    }
}
