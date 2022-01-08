package com.jwindustries.isitvegan.introduction;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.jwindustries.isitvegan.activities.IngredientOverviewActivity;

public class IntroductionActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button advanceButton;
    private SliderPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean forceIntro = this.getIntent() != null && this.getIntent().getBooleanExtra("force_intro", false);
        if (!forceIntro) {
            // If the tutorial has already been finished skip it and move to overview
            boolean isTutorialFinished = Utils.isTutorialFinished(this);
            if (isTutorialFinished) {
                this.navigateToOverview();
            }
        }

        super.onCreate(savedInstanceState);

        // Make activity fullscreen
        this.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        this.setContentView(R.layout.activity_introduction);

        // Hide action bar
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Bind views
        this.viewPager = findViewById(R.id.pager_intro_slider);
        TabLayout tabLayout = findViewById(R.id.introduction_tabs);
        this.advanceButton = findViewById(R.id.button_advance);

        // Init slider pager adapter
        this.adapter = new SliderPagerAdapter(this.getSupportFragmentManager(),
                this.getLifecycle());
        // Set adapter
        this.viewPager.setAdapter(this.adapter);
        // Set dot indicators
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText("OBJECT " + (position + 1))).attach();
        // Make status bar transparent
        this.changeStatusBarColor();
        this.advanceButton.setOnClickListener(view -> {
            if (this.viewPager.getCurrentItem() < this.adapter.getItemCount() - 1) {
                this.viewPager.setCurrentItem(this.viewPager.getCurrentItem() + 1);
            } else {
                Utils.storeTutorialFinished(this);
                this.navigateToOverview();
            }
        });

        /*
         * Add a listener that will be invoked whenever the page changes
         * or is incrementally scrolled
         */
        this.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    advanceButton.setText(R.string.introduction_get_started);
                } else {
                    advanceButton.setText(R.string.introduction_next);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void changeStatusBarColor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private void navigateToOverview() {
        this.startActivity(new Intent(this, IngredientOverviewActivity.class));
        this.finish();
    }
}