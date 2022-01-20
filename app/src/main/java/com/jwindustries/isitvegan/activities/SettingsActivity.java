package com.jwindustries.isitvegan.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.jwindustries.isitvegan.BuildConfig;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;
import com.jwindustries.isitvegan.introduction.IntroductionActivity;

public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.handleAppLocale(this);
        // Reset title as locale may have changed
        this.setTitle(R.string.action_settings);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final int DEBUG_THRESHOLD = 10;
        private int debugTriggers = 0;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Handle about button click
            Preference aboutButton = findPreference("about_button");
            if (aboutButton != null) {
                aboutButton.setOnPreferenceClickListener(preference -> {
                    this.startActivity(new Intent(this.getActivity(), AboutActivity.class));
                    return true;
                });
            }

            // Handle share app button click
            Preference shareAppButton = findPreference("share_app_button");
            if (shareAppButton != null) {
                shareAppButton.setOnPreferenceClickListener(preference -> {
                    this.shareApp();
                    return true;
                });
            }

            // Handle introduction button click
            Preference introductionButton = findPreference("introduction_button");
            if (introductionButton != null) {
                introductionButton.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(this.getActivity(), IntroductionActivity.class);
                    intent.putExtra("force_intro", true);
                    this.startActivity(intent);
                    return true;
                });
            }

            // Handle ingredient suggestion button click
            Preference ingredientSuggestionButton = findPreference("suggest_ingredient_button");
            if (ingredientSuggestionButton != null) {
                ingredientSuggestionButton.setOnPreferenceClickListener(preference -> {
                    Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                    selectorIntent.setData(Uri.parse("mailto:"));

                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.suggestion_mail_address)});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.suggestion_mail_subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.suggestion_mail_body));
                    emailIntent.setSelector(selectorIntent);

                    this.startActivity(Intent.createChooser(emailIntent, getString(R.string.suggestion_mail_chooser_label)));
                    return true;
                });
            }

            // Handle build version click
            Preference buildVersionButton = findPreference("build_version_button");
            if (buildVersionButton != null) {
                buildVersionButton.setOnPreferenceClickListener(v -> {
                    debugTriggers++;
                    if (debugTriggers >= DEBUG_THRESHOLD) {
                        debugTriggers = 0;
                        boolean debugging = Utils.toggleDebugging(getContext());
                        Toast
                                .makeText(
                                        this.getContext(),
                                        debugging
                                                ? R.string.message_debug_enabled
                                                : R.string.message_debug_disabled,
                                        Toast.LENGTH_SHORT
                                )
                                .show();
                    }
                    return true;
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "theme":
                    Utils.handleTheme(this.getActivity());
                    break;
                case "language_app":
                    // Recreate activity to update text
                    Activity activity = this.getActivity();
                    if (activity != null) {
                        activity.recreate();
                    }
                    break;
                case "language_ingredients":
                default:
                    break;
            }
        }

        private void shareApp() {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = this.getString(R.string.share_message) + "\n\n" +
                        this.getString(R.string.app_name) +
                        "\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(shareIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}