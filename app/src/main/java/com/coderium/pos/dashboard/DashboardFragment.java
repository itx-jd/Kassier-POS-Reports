package com.coderium.pos.dashboard;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.dashboard.CategoryUtils.categoryItemList;
import static com.coderium.pos.dashboard.CategoryUtils.fetchCategoriesData;
import static com.coderium.pos.dashboard.CategoryUtils.getCategoryIdByCategoryName;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coderium.pos.R;
import com.coderium.pos.foodMenu.MenuItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    View view;
    public  static String card_name;
    TextView tv_category_count,tv_product_count;
    LinearLayout ll_add_product,ll_add_category,ll_modify_product,ll_modify_category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_dashboard, container, false);

        tv_category_count = view.findViewById(R.id.tv_category_count);
        tv_product_count = view.findViewById(R.id.tv_product_count);

        ll_add_category = view.findViewById(R.id.ll_add_category);
        ll_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
            }
        });

        ll_add_product = view.findViewById(R.id.ll_add_product);
        ll_add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddProductDialog();
            }
        });

        ll_modify_category =  view.findViewById(R.id.ll_modify_category);
        ll_modify_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card_name = "category";
                startActivity(new Intent(getContext(), CategoryActivity.class));
            }
        });

        ll_modify_product =  view.findViewById(R.id.ll_modify_product);
        ll_modify_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card_name = "product";
                startActivity(new Intent(getContext(),CategoryActivity.class));
            }
        });



        return view;
    }

    private void showAddCategoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Category");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        final TextInputEditText etCategoryName = view.findViewById(R.id.name_edit_text);
        builder.setView(view);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(getContext());

                String categoryName = etCategoryName.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    // Generate categoryId using timestamp
                    String categoryId = String.valueOf(System.currentTimeMillis());

                    // Reference to the root node of the database
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    // Get the reference to the "categories" node and set the value for the new category
                    databaseRef.child("categories").child(categoryId).setValue(categoryName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            // Fetch the categories data and populate the categoryItemList
                            fetchCategoriesData(getContext());

                            Toast.makeText(getContext(), categoryName+" Category Added ! "+"\uD83D\uDC4D", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed To Add "+categoryName+" Category ! "+"\uD83D\uDE22", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Close the dialog
                    dialog.dismiss();

                }else{
                    Toast.makeText(getContext(), "Incomplete Details ! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(getContext());
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    // Function to show the dialog for adding a product

    private void showAddProductDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Product");

        // Inflate the dialog layout
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);

        final TextInputEditText etProductName = view.findViewById(R.id.name_edit_text);
        final TextInputEditText etProductPrice = view.findViewById(R.id.price_edit_text);
        final TextInputEditText etProductSize = view.findViewById(R.id.size_edit_text);
        final TextInputEditText etProductDes = view.findViewById(R.id.des_edit_text);
        MaterialSpinner categorySpinner = view.findViewById(R.id.categorySpinner);

        // Fetch the categories data and populate the categoryItemList
        fetchCategoriesData(getContext());

        // Extract category names from the categories list and create a list of category names
        List<String> categoryNames = new ArrayList<>();

        for (int i = 0; i < categoryItemList.size(); i++) {
            categoryNames.add(categoryItemList.get(i).getCategoryName());
        }

        // Create the ArrayAdapter using the categoryNames list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);

        // Set the layout for the dropdown list (optional)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the MaterialSpinner
        categorySpinner.setAdapter(adapter);

        builder.setView(view);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(getContext());

                String productName = etProductName.getText().toString().trim();
                String productPrice = etProductPrice.getText().toString().trim();
                String productSize = etProductSize.getText().toString().trim();
                String productDesc = etProductDes.getText().toString().trim();
                final TextInputEditText etProductDes = view.findViewById(R.id.des_edit_text);
                String categoryId =  getCategoryIdByCategoryName(categorySpinner.getText().toString());

                // Generate the timestamp-based item_id
                String itemId = String.valueOf(System.currentTimeMillis());

                if(!productName.isEmpty() && !productPrice.isEmpty()){

                    if(productSize.isEmpty()){
                        productSize = "None";
                    }

                    // Get a reference to your Firebase database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference menuItemsRef = database.getReference("menu_items");

                    // Create a new Product object with the details
                    MenuItem menuItem = new MenuItem(categoryId, productDesc, itemId, productName, Integer.parseInt(productPrice),productSize);


                    // Use the reference to push the new product to Firebase
                    menuItemsRef.child(categoryId).child(itemId).setValue(menuItem);

                    Toast.makeText(getContext(), productName+" Added Successfully ! "+"\uD83D\uDC4D", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getContext(), "Incomplete Details ! ", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(getContext());
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    // Function to fetch data from Firebase and calculate the total number of product IDs
    public void calculateTotalProducts() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference menuItemsRef = database.getReference("menu_items");

        // Add a listener to retrieve data from the Firebase database
        menuItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Variable to keep track of the total number of product IDs
                int totalProducts = 0;

                // Iterate through each category
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    // Add the number of products under this category to the total count
                    totalProducts += (int) categorySnapshot.getChildrenCount();
                }

                tv_product_count.setText(String.valueOf(totalProducts));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors if needed
            }
        });
    }

    // Function to fetch data from Firebase and update the total_category variable
    public void fetchAndUpdateTotalCategory() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = database.getReference("categories");

        // Add a listener to retrieve data from the Firebase database
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_category_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors if needed
            }
        });
    }

    @Override
    public void onResume() {
        calculateTotalProducts();
        fetchAndUpdateTotalCategory();
        super.onResume();
    }
}