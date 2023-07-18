package com.coderium.pos;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.dashboard.CategoryUtils.fetchCategoriesData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coderium.pos.billing.CartMenuData;
import com.coderium.pos.billing.BillingActivity;
import com.coderium.pos.dashboard.DashboardFragment;
import com.coderium.pos.foodMenu.MenuFragment;
import com.coderium.pos.preferences.SettingsActivity;
import com.coderium.pos.reports.ReportsFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    String titleText;
    int back = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up custom title of status bar
        customStatusBarTitle();

        // Fetch the categories data and populate the categoryItemList
        fetchCategoriesData(this);

        // Set up the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switchFragment(item.getItemId());

                // Close the navigation drawer after handling the item click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Set up the hamburger icon to open the navigation drawer
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        // default selected item

        navigationView.getMenu().getItem(0).setChecked(true);
        switchFragment(R.id.menu_item_menu);

    }

    private void switchFragment(int itemId) {

        switch (itemId) {

            case R.id.menu_item_menu:
                titleText = "Menu";
                CartMenuData.clearList();
                replaceFragment(new MenuFragment());
                break;
            case R.id.menu_item_reports:
                titleText = "Insights";
                replaceFragment(new ReportsFragment());
                break;
            case R.id.menu_item_dashbaord:
                titleText = "Dashboard";
                replaceFragment(new DashboardFragment());
                break;
            case R.id.menu_item_preferences:
                titleText = "Preferences";
                 startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                // In case an unknown item is clicked, display a toast message.
                Toast.makeText(this, "Unknown item clicked", Toast.LENGTH_SHORT).show();
                return;
        }
    }

    public void replaceFragment(Fragment fragment){

        // Replace the current fragment with the selected one.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        customStatusBarTitle();

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle the action bar's Up/Home button to open the navigation drawer
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // Close the navigation drawer when the back button is pressed
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {

            if(!Objects.equals(titleText, "Menu")){
                navigationView.getMenu().getItem(0).setChecked(true);
                switchFragment(R.id.menu_item_menu);
            }else{

                if (back == 0) {
                    Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
                    back = 1;
                } else {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    finish();

                }
            }

        }
    }

    public void btn_bill(View view) {
        vibrator(this);
        startActivity(new Intent(this, BillingActivity.class));
    }

    public void customStatusBarTitle(){

        // Set custom layout as ActionBar title
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View customActionBarTitleView = LayoutInflater.from(this)
                .inflate(R.layout.action_bar_title_layout, null);
        TextView titleTextView = customActionBarTitleView.findViewById(R.id.customActionBarTitle);
        titleTextView.setText(titleText);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );

        getSupportActionBar().setCustomView(customActionBarTitleView, layoutParams);
    }

}