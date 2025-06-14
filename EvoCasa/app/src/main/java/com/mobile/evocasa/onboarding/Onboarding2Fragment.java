package com.mobile.evocasa.onboarding;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.VideoView;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Onboarding2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Onboarding2Fragment extends Fragment {

    VideoView videoView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Onboarding2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Onboarding2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Onboarding2Fragment newInstance(String param1, String param2) {
        Onboarding2Fragment fragment = new Onboarding2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    //Animation
    private void typeTextWithCursor(final TextView textView, final String fullText, final long charDelay, final Runnable onComplete) {
        final int[] index = {0};
        final String cursor = "|";
        final boolean[] showCursor = {true};

        textView.setText(""); // Clear initial

        // Runnable nhấp nháy cursor
        final Runnable[] cursorRunnable = new Runnable[1];

        cursorRunnable[0] = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    String visibleText = fullText.substring(0, index[0]);
                    textView.setText(visibleText + (showCursor[0] ? cursor : ""));
                    showCursor[0] = !showCursor[0];
                    textView.postDelayed(cursorRunnable[0], 500); // Blink cursor
                }
            }
        };
        textView.post(cursorRunnable[0]);

        // Runnable đánh máy từng chữ
        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    index[0]++;
                    textView.postDelayed(this, charDelay);
                } else {
                    // Kết thúc: dừng cursor và ẩn sau 0.8s
                    textView.removeCallbacks(cursorRunnable[0]);
                    textView.postDelayed(() -> {
                        textView.setText(fullText); // Xoá cursor
                        if (onComplete != null) onComplete.run();
                    }, 800);
                }
            }
        };
        textView.postDelayed(typingRunnable, 300); // Delay start
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding2, container, false);

        videoView = view.findViewById(R.id.videoview);
        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.vid_onboarding2);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        TextView txtViewExplore = view.findViewById(R.id.txtViewExplore);
        FontUtils.setZregularFont(requireContext(), txtViewExplore);

        TextView txtViewTheSoulOf = view.findViewById(R.id.txtViewTheSoulOf);
        FontUtils.setZregularFont(requireContext(), txtViewTheSoulOf);

        TextView txtViewEvoCasa = view.findViewById(R.id.txtViewEvoCasa);
        FontUtils.setZblackFont(requireContext(), txtViewEvoCasa);

        TextView txtView3 = view.findViewById(R.id.txtView3);
        FontUtils.setItalicFont(requireContext(), txtView3);


        //Animation
        txtViewExplore.setVisibility(View.INVISIBLE);
        txtViewTheSoulOf.setVisibility(View.INVISIBLE);
        txtViewEvoCasa.setVisibility(View.INVISIBLE);
        txtView3.setVisibility(View.INVISIBLE);

        // Gán text

        String text1 = getString(R.string.title_onboarding2_line_1);
        String text2 = getString(R.string.title_onboarding2_line_2);
        String text3 = getString(R.string.title_onboarding2_line_3);
        String text4 = getString(R.string.title_onboarding2_description);


// Gõ từng dòng với cursor
        txtViewExplore.setVisibility(View.VISIBLE);
        typeTextWithCursor(txtViewExplore, text1, 60, () -> {
            txtViewTheSoulOf.setVisibility(View.VISIBLE);
            typeTextWithCursor(txtViewTheSoulOf, text2, 60, () -> {
                txtViewEvoCasa.setVisibility(View.VISIBLE);
                typeTextWithCursor(txtViewEvoCasa, text3, 60, () -> {
                    txtView3.setVisibility(View.VISIBLE);
                    typeTextWithCursor(txtView3, text4, 20, null);
                });
            });
        });




        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    public void onPause() {
        videoView.suspend();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        videoView.stopPlayback();
        super.onDestroy();
    }
}