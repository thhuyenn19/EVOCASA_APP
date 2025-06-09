package com.mobile.evocasa;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.profile.ProfileFragment;

public class NarBarActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nar_bar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_nav_container, new BottomNavFragment())
                .commit();
        showFragment(0);
    }
    @Override
    public void onBottomNavSelected(int position) {
        showFragment(position);
    }
    private void showFragment(int pos) {
        Fragment frag;
        switch (pos) {
            case 1: frag = new ShopFragment(); break;
            case 2: frag = new NotificationFragment(); break;
            case 3: frag = new ProfileFragment(); break;
            default: frag = new HomeFragment(); break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
    }
}

