package com.coderium.pos.dashboard;


import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class CategoryUtils {

    public static List<CategoryItem> categoryItemList = new ArrayList<>();

    public static String getCategoryIdByCategoryName(String categoryName) {
        for (CategoryItem categoryItem : categoryItemList) {
            if (categoryItem.getCategoryName().equalsIgnoreCase(categoryName)) {
                return categoryItem.getCategoryId();
            }
        }
        // If the category name is not found, you can return null or an appropriate default value.
        return null;
    }

    public static int getIndexByCategoryId(String categoryId) {
        for (int i = 0; i < categoryItemList.size(); i++) {
            CategoryItem categoryItem = categoryItemList.get(i);
            if (categoryItem.getCategoryId().equals(categoryId)) {
                return i;
            }
        }
        // If the categoryId is not found, you can return -1 or an appropriate default value.
        return -1;
    }



    // Method to check if category name is present in the list
    public static boolean isCategoryNamePresent(String categoryName) {
        for (CategoryItem categoryItem : categoryItemList) {
            if (categoryItem.getCategoryName().equals(categoryName)) {
                return true;
            }
        }
        return false;
    }

    public static void fetchCategoriesData(Context context) {

        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference().child("categories");;

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method will be called once with the initial value and again
                // whenever data at this location is updated.

                // Clear the existing list to avoid duplicates if this method is called again
                categoryItemList.clear();

                // Iterate through the dataSnapshot to extract the categories data
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryId = categorySnapshot.getKey();
                    String categoryName = categorySnapshot.getValue(String.class);

                    // Create a new CategoryItem object
                    CategoryItem categoryItem = new CategoryItem(categoryId, categoryName);

                    // Add the CategoryItem object to the static list
                    categoryItemList.add(categoryItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method will be called if there is an error while fetching data
                Toast.makeText(context, "Error fetching categories data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
