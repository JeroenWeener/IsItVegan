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

import com.jwindustries.isitvegan.Activities.IngredientViewActivity;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An ingredient adapter that is initially empty. Ingredients are added on the fly
 */
public class AdditiveIngredientAdapter
        extends RecyclerView.Adapter<AdditiveIngredientAdapter.IngredientViewHolder>
        implements INameableAdapter {

    private final Activity activity;
    private final List<Ingredient> ingredientList;

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        public View ingredientView;
        public TextView ingredientNameView;
        public TextView ingredientENumberView;
        public ImageView ingredientTypeIconView;

        public IngredientViewHolder(View ingredientView) {
            super(ingredientView);
            this.ingredientView = ingredientView;
            this.ingredientNameView = ingredientView.findViewById(R.id.ingredient_text_view);
            this.ingredientENumberView = ingredientView.findViewById(R.id.ingredient_e_number_view);
            this.ingredientTypeIconView = ingredientView.findViewById(R.id.ingredient_row_badge);
        }
    }

    public AdditiveIngredientAdapter(Activity activity) {
        this.activity = activity;
        this.ingredientList = new ArrayList<>();
    }

    /**
     * Adds provided ingredients to ingredient list
     * Only adds ingredients that are not already in the list
     * @param ingredients the ingredients to be add to the ingredient list
     * @return the number of ingredients added to the list
     */
    public int addIngredients(List<Ingredient> ingredients) {
        List<Ingredient> newIngredients = ingredients.stream().filter(ingredient ->
                !this.ingredientList.contains(ingredient)).collect(Collectors.toList());
        this.ingredientList.addAll(0, newIngredients);
        this.notifyItemRangeInserted(0, newIngredients.size());
        return newIngredients.size();
    }

    @Override
    public Character getCharacterForElement(int position) {
        Ingredient ingredient = this.ingredientList.get(position);
        char character =  ingredient.getName().replace("(", "").charAt(0);
        if (Character.isDigit(character)) {
            character = '#';
        }
        return character;
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
        Ingredient ingredient = this.ingredientList.get(position);
        holder.ingredientNameView.setText(ingredient.getName());

        if (ingredient.hasENumber()) {
            holder.ingredientENumberView.setVisibility(View.VISIBLE);
            holder.ingredientENumberView.setText(ingredient.getENumber());
        } else {
            holder.ingredientENumberView.setVisibility(View.GONE);
        }

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

        View nameView = holder.ingredientNameView;
        nameView.setTransitionName("name");
        View badgeView = holder.ingredientTypeIconView;
        badgeView.setTransitionName("badge");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
//                Pair.create(nameView, "name"),
                Pair.create(badgeView, "badge"));

        this.activity.startActivity(intent, options.toBundle());
    }
}
