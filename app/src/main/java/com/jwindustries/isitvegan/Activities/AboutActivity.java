package com.jwindustries.isitvegan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jwindustries.isitvegan.BuildConfig;
import com.jwindustries.isitvegan.R;

public class AboutActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void shareApp(View view) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = this.getString(R.string.share_message) + "\n\n" +
                    this.getString(R.string.app_name) +
                    "\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(shareIntent);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}