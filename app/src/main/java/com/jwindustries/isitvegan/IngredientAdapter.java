package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private final Context context;
    private List<Ingredient> ingredientList;

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        public View ingredientView;
        public TextView ingredientNameView;
        public ImageView ingredientTypeIconView;

        public IngredientViewHolder(View ingredientView) {
            super(ingredientView);
            this.ingredientView = ingredientView;
            this.ingredientNameView = ingredientView.findViewById(R.id.ingredient_text_view);
            this.ingredientTypeIconView = ingredientView.findViewById(R.id.ingredient_type_icon);
        }
    }

    public IngredientAdapter(Context context) {
        this.context = context;
        this.ingredientList = IngredientList.getIngredientList(context);
    }

    public void filterItems(String query) {
        this.ingredientList = IngredientList.getIngredientList(this.context).stream().filter((ingredient) ->
                ingredient.getName().toLowerCase().contains(query)).collect(Collectors.toList());
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ingredientView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_row_view, parent, false);
        return new IngredientViewHolder(ingredientView);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.ingredientNameView.setText(ingredient.getName());
        holder.ingredientView.setOnClickListener(view -> {
            if (ingredient.hasExtraInformation()) {
                this.viewIngredient(ingredient);
            }
        });

        int drawableId;
        switch (ingredient.getIngredientType()) {
            case VEGAN:
                drawableId = R.drawable.vegan_badge;
                break;
            case NOT_VEGAN:
                drawableId = R.drawable.not_vegan_badge;
                break;
            case DEPENDS:
            default:
                drawableId = R.drawable.depends_badge;
        }
        holder.ingredientTypeIconView.setImageResource(drawableId);
    }

    @Override
    public int getItemCount() {
        return this.ingredientList.size();
    }

    private void viewIngredient(Ingredient ingredient) {
        Intent viewIngredientIntent = new Intent(context, IngredientViewActivity.class);
        viewIngredientIntent.putExtra(context.getResources().getString(R.string.ingredient_key), ingredient);
        context.startActivity(viewIngredientIntent);
    }
}
