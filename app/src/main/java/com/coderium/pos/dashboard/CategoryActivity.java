package com.coderium.pos.dashboard;

import static com.coderium.pos.dashboard.CategoryUtils.categoryItemList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.coderium.pos.R;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    LinearLayout ll_not_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_category);

        getSupportActionBar().setTitle("Select Category");

        // Assuming you have the RecyclerView in your layout file with the ID 'categoryRecyclerView'.
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        ll_not_found = findViewById(R.id.ll_not_found);

        if(categoryItemList.isEmpty()){
            categoryRecyclerView.setVisibility(View.GONE);
            ll_not_found.setVisibility(View.VISIBLE);
        }

        // Set up the RecyclerView with the adapter
        CategoryAdapter adapter = new CategoryAdapter(this,categoryItemList);
        categoryRecyclerView.setAdapter(adapter);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }
}