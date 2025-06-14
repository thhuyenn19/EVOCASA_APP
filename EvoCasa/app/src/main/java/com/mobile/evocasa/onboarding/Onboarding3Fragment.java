package com.mobile.evocasa.onboarding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Onboarding3Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Onboarding3Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Onboarding3Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Onboarding3Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Onboarding3Fragment newInstance(String param1, String param2) {
        Onboarding3Fragment fragment = new Onboarding3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void typeTextWithCursor(final TextView textView, final String fullText, final long charDelay, final Runnable onComplete) {
        final int[] index = {0};
        final String cursor = "|";
        final boolean[] showCursor = {true};

        textView.setText("");
        final Runnable[] cursorRunnable = new Runnable[1];

        cursorRunnable[0] = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    String visibleText = fullText.substring(0, index[0]);
                    textView.setText(visibleText + (showCursor[0] ? cursor : ""));
                    showCursor[0] = !showCursor[0];
                    textView.postDelayed(this, 500);
                }
            }
        };
        textView.post(cursorRunnable[0]);

        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    index[0]++;
                    textView.postDelayed(this, charDelay);
                } else {
                    textView.removeCallbacks(cursorRunnable[0]);
                    textView.postDelayed(() -> {
                        textView.setText(fullText);
                        if (onComplete != null) onComplete.run();
                    }, 800);
                }
            }
        };
        textView.postDelayed(typingRunnable, 300);
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
        View view = inflater.inflate(R.layout.fragment_onboarding3, container, false);

        TextView txtViewOnboarding3 = view.findViewById(R.id.txtViewOnboarding3);
        FontUtils.setZblackFont(requireContext(), txtViewOnboarding3);

        TextView txtView3 = view.findViewById(R.id.txtView3);
        FontUtils.setRegularFont(requireContext(), txtView3);

        return view;
    }

    private boolean hasStartedTyping = false;

    @Override
    public void onResume() {
        super.onResume();

        if (!hasStartedTyping && isVisible()) {
            hasStartedTyping = true;

            View view = getView();
            if (view == null) return;

            TextView txtViewOnboarding3 = view.findViewById(R.id.txtViewOnboarding3);
            TextView txtView3 = view.findViewById(R.id.txtView3);

            txtViewOnboarding3.setVisibility(View.INVISIBLE);
            txtView3.setVisibility(View.INVISIBLE);

            String line1 = getString(R.string.title_onboarding3_line_1);
            String line2 = getString(R.string.title_onboarding3_description);

            txtViewOnboarding3.setVisibility(View.VISIBLE);
            typeTextWithCursor(txtViewOnboarding3, line1, 60, () -> {
                txtView3.setVisibility(View.VISIBLE);
                typeTextWithCursor(txtView3, line2, 40, null);
            });

        }
    }

}