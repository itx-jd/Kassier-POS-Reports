package com.coderium.pos.dashboard;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.dashboard.CategoryAdapter.category_id;
import static com.coderium.pos.dashboard.CategoryUtils.categoryItemList;
import static com.coderium.pos.dashboard.CategoryUtils.fetchCategoriesData;
import static com.coderium.pos.dashboard.CategoryUtils.getCategoryIdByCategoryName;
import static com.coderium.pos.dashboard.CategoryUtils.getIndexByCategoryId;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Objects;

public class ModifyMenuAdapter extends RecyclerView.Adapter<ModifyMenuAdapter.ViewHolder> {

    public List<MenuItem> menuItemList;
    public Context context;

    public ModifyMenuAdapter(List<MenuItem> menuItemList, Context context) {
        this.menuItemList = menuItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        MenuItem menuItem = menuItemList.get(position);

        holder.itemNameTextView.setText(menuItem.getItemName());
        holder.itemSizeTextView.setText(menuItem.getItemSize());

        if(Objects.equals(menuItem.getItemSize(), "None")){
            holder.itemSizeTextView.setVisibility(View.GONE);
        }

        holder.cv_menuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showModifyProductDialog(menuItem);

            }
        });

    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemNameTextView,itemSizeTextView;
        private CardView cv_menuItem;
        private LinearLayout ll_menuItem,ll_background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemSizeTextView = itemView.findViewById(R.id.itemSizeTextView);
            cv_menuItem = itemView.findViewById(R.id.cv_menuItem);
            ll_menuItem = itemView.findViewById(R.id.ll_menuItem);
            ll_background = itemView.findViewById(R.id.ll_background);

        }

    }

    // Function to show the dialog for Modify a product
    private void showModifyProductDialog(MenuItem menuItem) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modify Product");

        // Inflate the dialog layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null);
        final TextInputEditText etProductName = view.findViewById(R.id.name_edit_text);
        final TextInputEditText etProductPrice = view.findViewById(R.id.price_edit_text);
        final TextInputEditText etProductSize = view.findViewById(R.id.size_edit_text);
        final TextInputEditText etProductDes = view.findViewById(R.id.des_edit_text);
        MaterialSpinner categorySpinner = view.findViewById(R.id.categorySpinner);

        // Fetch the categories data and populate the categoryItemList
        fetchCategoriesData(context);

        // Extract category names from the categories list and create a list of category names
        List<String> categoryNames = new ArrayList<>();

        for (int i = 0; i < categoryItemList.size(); i++) {
            categoryNames.add(categoryItemList.get(i).getCategoryName());
        }

        // Create the ArrayAdapter using the categoryNames list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryNames);

        // Set the layout for the dropdown list (optional)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the MaterialSpinner
        categorySpinner.setAdapter(adapter);

        // Set the Current Data

        etProductName.setText(menuItem.getItemName());
        etProductPrice.setText(String.valueOf(menuItem.getItemPrice()));
        etProductSize.setText(menuItem.getItemSize());
        categorySpinner.setSelectedIndex(getIndexByCategoryId(menuItem.getCategoryId()));
        etProductDes.setText(menuItem.getItemDescription());

        builder.setView(view);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                String productName = etProductName.getText().toString().trim();
                String productPrice = etProductPrice.getText().toString().trim();
                String productSize = etProductSize.getText().toString().trim();
                String productDesc = etProductDes.getText().toString().trim();

                String new_categoryId =  getCategoryIdByCategoryName(categorySpinner.getText().toString());

                if(!productName.isEmpty() && !productPrice.isEmpty()){

                    if(productSize.isEmpty()){
                        productSize = "None";
                    }

                    if(Objects.equals(new_categoryId, menuItem.getCategoryId())){

                        updateProductWithoutCategoryChange(menuItem,productName,productPrice,productSize,productDesc);

                    }else{
                        updateProductCategoryChange(menuItem,new_categoryId,productName,productPrice,productSize,productDesc);
                    }



                }else{
                    Toast.makeText(context, "Incomplete Details ! ", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();

            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                showDeleteMenuItemAlertDialog(menuItem);

                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showDeleteMenuItemAlertDialog(MenuItem menuItem) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Alert !");
        builder.setMessage("Do you want to delete " + menuItem.getItemName()+" ?");

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                deleteProduct(menuItem);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);
                dialog.dismiss();
            }
        });

        // Create and show the deleteCategoryAlertDialog
        AlertDialog deleteCategoryAlertDialog = builder.create();
        deleteCategoryAlertDialog.show();
    }

    private void updateProductWithoutCategoryChange(MenuItem menuItem,String productName,String productPrice,String productSize,String productDesc){

        // Get a reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference menuItemsRef = database.getReference("menu_items");

        MenuItem menuItem1 = new MenuItem(menuItem.getCategoryId(),productDesc,menuItem.getItemId(),productName,Integer.parseInt(productPrice),productSize);

        // Use the reference to push the updated product to Firebase
        menuItemsRef.child(menuItem.getCategoryId()).child(menuItem.getItemId()).setValue(menuItem1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                fetchMenuItemsForFirebase();
                Toast.makeText(context, productName + " Updated Successfully ! " + "\uD83D\uDC4D", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed To Update "+productName+" !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct(MenuItem menuItem){

        // Get a reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference menuItemsRef = database.getReference("menu_items");

        // Use the reference to remove the product from Firebase
        menuItemsRef.child(menuItem.getCategoryId()).child(menuItem.getItemId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                fetchMenuItemsForFirebase();
                Toast.makeText(context, menuItem.getItemName() + " Deleted Successfully !", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to delete "+menuItem.getItemName() +" !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateProductCategoryChange(MenuItem menuItem,String new_category_id,String productName,String productPrice,String productSize,String productDesc){

        // Delete product from old category

        // Get a reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference menuItemsRef = database.getReference("menu_items");

        // Use the reference to remove the product from Firebase
        menuItemsRef.child(menuItem.getCategoryId()).child(menuItem.getItemId()).removeValue();

        // Creating New node in new category

        MenuItem menuItem1 = new MenuItem(new_category_id,productDesc, menuItem.getItemId(), productName,Integer.parseInt(productPrice),productSize);

        menuItemsRef.child(new_category_id).child(menuItem.getItemId()).setValue(menuItem1);

        fetchMenuItemsForFirebase();
        Toast.makeText(context, productName+" Updated Successfully !", Toast.LENGTH_SHORT).show();

    }

    private void fetchMenuItemsForFirebase() {

        // Initialize the menuItemsRef if not already initialized
        DatabaseReference menuItemsRef = FirebaseDatabase.getInstance().getReference().child("menu_items");

        menuItemsRef.child(category_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                menuItemList.clear();

                for (DataSnapshot menuItemSnapshot : snapshot.getChildren()) {
                    // Use the new constructor of MenuItem
                    MenuItem menuItem = menuItemSnapshot.getValue(MenuItem.class);
                    menuItemList.add(menuItem);
                }

                notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed.

            }
        });
    }
}