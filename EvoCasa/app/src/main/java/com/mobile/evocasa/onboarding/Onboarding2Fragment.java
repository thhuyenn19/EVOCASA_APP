package com.mobile.evocasa.onboarding;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class Onboarding2Fragment extends Fragment {

    private VideoView videoView;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    public Onboarding2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding2, container, false);

        videoView = view.findViewById(R.id.videoview);
        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.vid_onboarding2);
        Log.d("Onboarding2Fragment", "Video path: " + uri.toString());

        // Kiểm tra file video
        if (uri != null) {
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoView.start();
                Log.d("Onboarding2Fragment", "Video prepared and started");
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e("Onboarding2Fragment", "Video error: what=" + what + ", extra=" + extra);
                return true;
            });
        } else {
            Log.e("Onboarding2Fragment", "Video URI is null");
        }

        TextView txtViewExplore = view.findViewById(R.id.txtViewExplore);
        FontUtils.setZregularFont(requireContext(), txtViewExplore);

        TextView txtViewTheSoulOf = view.findViewById(R.id.txtViewTheSoulOf);
        FontUtils.setZregularFont(requireContext(), txtViewTheSoulOf);

        TextView txtViewEvoCasa = view.findViewById(R.id.txtViewEvoCasa);
        FontUtils.setZblackFont(requireContext(), txtViewEvoCasa);

        TextView txtView3 = view.findViewById(R.id.txtView3);
        FontUtils.setItalicFont(requireContext(), txtView3);

        // Animation
        txtViewExplore.setVisibility(View.INVISIBLE);
        txtViewTheSoulOf.setVisibility(View.INVISIBLE);
        txtViewEvoCasa.setVisibility(View.INVISIBLE);
        txtView3.setVisibility(View.INVISIBLE);

        String text1 = getString(R.string.title_onboarding2_line_1);
        String text2 = getString(R.string.title_onboarding2_line_2);
        String text3 = getString(R.string.title_onboarding2_line_3);
        String text4 = getString(R.string.title_onboarding2_description);

        txtViewExplore.setVisibility(View.VISIBLE);
        typeTextWithCursor(txtViewExplore, text1, 100, () -> {
            txtViewTheSoulOf.setVisibility(View.VISIBLE);
            typeTextWithCursor(txtViewTheSoulOf, text2, 100, () -> {
                txtViewEvoCasa.setVisibility(View.VISIBLE);
                typeTextWithCursor(txtViewEvoCasa, text3, 100, () -> {
                    txtView3.setVisibility(View.VISIBLE);
                    typeTextWithCursor(txtView3, text4, 80, null);
                });
            });
        });

        return view;
    }

    private void typeTextWithCursor(final TextView textView, final String fullText, final long charDelay, final Runnable onComplete) {
        final int[] index = {0};
        final String cursor = "|";
        final boolean[] showCursor = {true};

        textView.setText("");

        final Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    String visibleText = fullText.substring(0, index[0]);
                    textView.setText(visibleText + (showCursor[0] ? cursor : ""));
                    showCursor[0] = !showCursor[0];

                    if (index[0] < fullText.length()) {
                        index[0]++;
                        textView.postDelayed(this, charDelay);
                    } else {
                        textView.postDelayed(() -> {
                            textView.setText(fullText);
                            if (onComplete != null) onComplete.run();
                        }, 500);
                    }
                }
            }
        };

        textView.postDelayed(typingRunnable, 200);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null && videoView.isPlaying()) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.suspend();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }
}