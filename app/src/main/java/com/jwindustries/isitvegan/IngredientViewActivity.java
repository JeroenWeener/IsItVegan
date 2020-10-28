package com.jwindustries.isitvegan;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class IngredientViewActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleAppLanguage(this);
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
            ((ImageView) findViewById(R.id.ingredient_type_icon)).setImageResource(drawableId);
        }
    }
}