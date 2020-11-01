package com.jwindustries.isitvegan.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

public class IngredientViewActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleAppLocale(this);
        // Reset title as locale may have changed
        this.setTitle(R.string.app_name);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_view);

        Ingredient ingredient = (Ingredient) this.getIntent().getSerializableExtra(this.getResources().getString(R.string.ingredient_key));
        if (ingredient != null) {
            ((TextView) this.findViewById(R.id.ingredient_name_view)).setText(ingredient.getName());
            ((TextView) this.findViewById(R.id.ingredient_information_view)).setText(ingredient.getInformation());

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
            ((ImageView) findViewById(R.id.ingredient_badge)).setImageResource(drawableId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finishAfterTransition();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}