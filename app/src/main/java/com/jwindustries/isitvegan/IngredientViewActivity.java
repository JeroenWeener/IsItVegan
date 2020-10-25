package com.jwindustries.isitvegan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.widget.TextView;

public class IngredientViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_view);

        Ingredient ingredient = (Ingredient) this.getIntent().getSerializableExtra(this.getResources().getString(R.string.ingredient_key));

        if (ingredient != null) {
            TextView nameView = this.findViewById(R.id.ingredient_name_view);
            nameView.setText(ingredient.getName());

            int drawableId;
            switch (ingredient.getIngredientType()) {
                case VEGAN:
                    drawableId = R.drawable.vegan_gradient;
                    break;
                case NOT_VEGAN:
                    drawableId = R.drawable.non_vegan_gradient;
                    break;
                case DEPENDS:
                default:
                    drawableId = R.drawable.depends_gradient;
                    break;
            }
            nameView.setBackground(ResourcesCompat.getDrawable(this.getResources(), drawableId, this.getTheme()));

            ((TextView) this.findViewById(R.id.ingredient_information_view)).setText(ingredient.getInformation());
        }
    }
}