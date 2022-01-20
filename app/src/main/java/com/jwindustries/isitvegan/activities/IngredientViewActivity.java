package com.jwindustries.isitvegan.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

public class IngredientViewActivity extends BaseActivity {
    private Ingredient ingredient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleAppLocale(this);
        // Reset title as locale may have changed
        this.setTitle(R.string.app_name);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_view);

        this.ingredient = (Ingredient) this.getIntent().getSerializableExtra(this.getResources().getString(R.string.key_ingredient));
        if (ingredient != null) {
            ((TextView) this.findViewById(R.id.ingredient_name_view)).setText(ingredient.getName(this.getApplicationContext()));

            TextView eNumberView = this.findViewById(R.id.ingredient_e_number_view);
            if (ingredient.hasENumber()) {
                eNumberView.setText(ingredient.getENumber());
            } else {
                eNumberView.setVisibility(View.GONE);
            }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.ingredient_view_actionbar_menu, menu);
        return true;
    }

    public void searchOnline() {
        String url = "https://duckduckgo.com/?q=" + this.ingredient.getName(this.getApplicationContext()) + "+vegan";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_opening_browser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finishAfterTransition();
            return true;
        } else if (item.getItemId() == R.id.action_search_online) {
            this.searchOnline();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}