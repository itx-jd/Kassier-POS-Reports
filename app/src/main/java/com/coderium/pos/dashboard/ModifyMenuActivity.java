package com.coderium.pos.dashboard;

import static com.coderium.pos.dashboard.CategoryAdapter.category_id;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.coderium.pos.R;
import com.coderium.pos.foodMenu.MenuItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ModifyMenuActivity extends AppCompatActivity {

    private DatabaseReference menuItemsRef;
    private RecyclerView recyclerViewMenuItems;
    LinearLayout ll_not_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_menu);

        // Set up custom title of status bar
        customStatusBarTitle();
        recyclerViewMenuItems = findViewById(R.id.recyclerViewMenuItem);
        ll_not_found = findViewById(R.id.ll_not_found);

        // Set the layout manager for the RecyclerView
        recyclerViewMenuItems.setLayoutManager(new GridLayoutManager(this, 2));
        fetchMenuItemsForFirebase(category_id);


    }

    private void fetchMenuItemsForFirebase(String category_id) {

        // Initialize the menuItemsRef if not already initialized
        menuItemsRef = FirebaseDatabase.getInstance().getReference().child("menu_items");

        menuItemsRef.child(category_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<MenuItem> menuItemsList = new ArrayList<>();

                for (DataSnapshot menuItemSnapshot : snapshot.getChildren()) {
                    // Use the new constructor of MenuItem
                    MenuItem menuItem = menuItemSnapshot.getValue(MenuItem.class);
                    menuItemsList.add(menuItem);
                }

                if(menuItemsList.isEmpty()){
                    recyclerViewMenuItems.setVisibility(View.GONE);
                    ll_not_found.setVisibility(View.VISIBLE);
                }else{
                    displayMenuItemsInRecyclerView(menuItemsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed.
                Toast.makeText(ModifyMenuActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMenuItemsInRecyclerView(List<MenuItem> menuItemsList) {
        ModifyMenuAdapter modifyMenuAdapter = new ModifyMenuAdapter(menuItemsList,ModifyMenuActivity.this);
        recyclerViewMenuItems.setAdapter(modifyMenuAdapter);
    }

    public void customStatusBarTitle(){

        // Set custom layout as ActionBar title
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View customActionBarTitleView = LayoutInflater.from(this)
                .inflate(R.layout.action_bar_title_layout, null);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        getSupportActionBar().setCustomView(customActionBarTitleView, layoutParams);
    }
}