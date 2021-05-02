package com.jwindustries.isitvegan.introduction;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jwindustries.isitvegan.R;

public class SliderItemFragment extends Fragment {

    private static final String ARG_POSITION = "slider-position";

    // Prepare all title ids arrays
    @StringRes
    private static final int[] PAGE_TITLES =
            new int[]{R.string.introduction_title_search, R.string.introduction_title_scan, R.string.introduction_title_view, R.string.introduction_title_share};


    // Prepare all subtitle ids arrays
    @StringRes
    private static final int[] PAGE_TEXT =
            new int[]{R.string.introduction_text_search, R.string.introduction_text_scan, R.string.introduction_text_view, R.string.introduction_text_share};

    // Prepare all subtitle images arrays
    @StringRes
    private static final int[] PAGE_IMAGE =
            new int[]{R.drawable.flaky_white, R.drawable.image_search_white, R.drawable.public_white, R.drawable.share_white};

    // prepare all background images arrays
    @StringRes
    private static final int[] BG_IMAGE =
            new int[]{R.color.colorSuccess, R.color.colorError, R.color.colorWarning, R.color.colorSuccess};

    private int position;

    public SliderItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     *
     * @return A new instance of fragment SliderItemFragment.
     */
    public static SliderItemFragment newInstance(int position) {
        SliderItemFragment fragment = new SliderItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null) {
            this.position = this.getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.introduction_fragment_slider_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set page background
        Context context = this.getContext();
        if (context != null) {
            view.setBackground(ContextCompat.getDrawable(this.getContext(), BG_IMAGE[this.position]));
        }

        TextView title = view.findViewById(R.id.titleTextView);
        TextView subtitle = view.findViewById(R.id.subtitleTextView);
        ImageView imageView = view.findViewById(R.id.imageView);

        title.setText(PAGE_TITLES[this.position]);
        subtitle.setText(PAGE_TEXT[this.position]);
        imageView.setImageResource(PAGE_IMAGE[this.position]);
    }
}
