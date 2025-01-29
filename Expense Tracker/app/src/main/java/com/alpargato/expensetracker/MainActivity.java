package com.alpargato.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.alpargato.expensetracker.Fragments.HomeFragment;
import com.alpargato.expensetracker.Fragments.NotificationFragment;
import com.alpargato.expensetracker.Fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment = new HomeFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();

        //BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.notification);
        //badgeDrawable.setVisible(true);
        //badgeDrawable.setNumber(8);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemIndex = item.getItemId();
                if (itemIndex == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();
                    return true;
                }else if (itemIndex == R.id.notification) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,notificationFragment).commit();
                    return true;
                }else if (itemIndex == R.id.settings) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,settingsFragment).commit();
                    return true;
                }else {
                    return false;
                }
            }
        });
    }
}