package com.mobile.evocasa.onboarding;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.mobile.evocasa.R;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Onboarding2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Onboarding2Fragment extends Fragment {

    private VideoView videoBackground;
    private MediaPlayer mMediaPlayer;
    private int mCurrentVideoPosition = 0;

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
        //return inflater.inflate(R.layout.fragment_onboarding2, container, false);

        View view = inflater.inflate(R.layout.fragment_onboarding2, container, false);

        // Hook up the VideoView to our UI.
        videoBackground = view.findViewById(R.id.videoBackground);

        // Build your video Uri
        Uri uri = Uri.parse("android.resource://"
                + requireContext().getPackageName()
                + "/"
                + R.raw.vid_onboarding2);

        // Set the new Uri to our VideoView
        videoBackground.setVideoURI(uri);

        // Set OnPreparedListener for looping and resume behavior
        videoBackground.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer = mediaPlayer;
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

                if (mCurrentVideoPosition != 0) {
                    mMediaPlayer.seekTo(mCurrentVideoPosition);
                }
                videoBackground.start();
            }

        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoBackground != null) {
            mCurrentVideoPosition = videoBackground.getCurrentPosition();
            videoBackground.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bật full screen immersive mode
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        if (videoBackground != null) {
            videoBackground.seekTo(mCurrentVideoPosition);
            videoBackground.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}