package com.jwindustries.isitvegan;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Pair;
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
    private final Activity activity;
    private List<Ingredient> ingredientList;

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        public View ingredientView;
        public TextView ingredientNameView;
        public ImageView ingredientTypeIconView;

        public IngredientViewHolder(View ingredientView) {
            super(ingredientView);
            this.ingredientView = ingredientView;
            this.ingredientNameView = ingredientView.findViewById(R.id.ingredient_text_view);
            this.ingredientTypeIconView = ingredientView.findViewById(R.id.ingredient_row_badge);
        }
    }

    public IngredientAdapter(Activity activity) {
        this.activity = activity;
        this.ingredientList = IngredientList.getIngredientList(activity);
    }

    public void filterItems(String query) {
        this.ingredientList = IngredientList.getIngredientList(this.activity).stream().filter((ingredient) ->
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
        holder.ingredientView.setOnClickListener(view -> this.viewIngredient(holder, ingredient));

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
                break;
        }
        holder.ingredientTypeIconView.setImageResource(drawableId);
    }

    @Override
    public int getItemCount() {
        return this.ingredientList.size();
    }

    private void viewIngredient(IngredientViewHolder holder, Ingredient ingredient) {
        Intent intent = new Intent(activity, IngredientViewActivity.class);
        intent.putExtra(activity.getResources().getString(R.string.ingredient_key), ingredient);

        View badgeView = holder.ingredientTypeIconView;
        badgeView.setTransitionName("badge");
        View nameView = holder.ingredientNameView;
        nameView.setTransitionName("name");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                Pair.create(badgeView, "badge"),
                Pair.create(nameView, "name"));

        this.activity.startActivity(intent, options.toBundle());
    }
}
