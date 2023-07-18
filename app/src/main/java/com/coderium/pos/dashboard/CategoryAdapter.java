package com.coderium.pos.dashboard;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.dashboard.CategoryUtils.getCategoryIdByCategoryName;
import static com.coderium.pos.dashboard.CategoryUtils.isCategoryNamePresent;
import static com.coderium.pos.dashboard.DashboardFragment.card_name;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public List<CategoryItem> categoryItemList;
    public Context context;
    public  static String category_id;

    public CategoryAdapter(Context context, List<CategoryItem> categoryItemList) {
        this.context = context;
        this.categoryItemList = categoryItemList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        CategoryItem categoryItem = categoryItemList.get(position);
        holder.categoryNameTextView.setText(categoryItem.getCategoryName());

        holder.ll_category_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Objects.equals(card_name, "category")){
                    showModifyCategoryDialog(categoryItem);
                }else{
                    category_id = categoryItem.getCategoryId();
                    context.startActivity(new Intent(context, ModifyMenuActivity.class));
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryItemList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryNameTextView;
        LinearLayout ll_category_item;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            ll_category_item = itemView.findViewById(R.id.ll_category_item);
        }

    }

    private void showModifyCategoryDialog(CategoryItem categoryItem) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_category);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modify Category");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        TextInputEditText etCategoryName = view.findViewById(R.id.name_edit_text);
        etCategoryName.setText(categoryItem.getCategoryName());

        String categoryName = etCategoryName.getText().toString().trim();
        String categoryId = getCategoryIdByCategoryName(categoryName);


        builder.setView(view);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                if (!categoryName.isEmpty()) {

                    updateCategoryName(categoryId,etCategoryName.getText().toString().trim());

                    // Close the dialog
                    dialog.dismiss();

                }else{
                    Toast.makeText(context, "Incomplete Details ! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                String categoryName = etCategoryName.getText().toString().trim();

                if(isCategoryNamePresent(categoryName)){
                    showDeleteCategoryAlertDialog(categoryItem);
                }else{
                    Toast.makeText(context, categoryName+" Category Not Found !", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showDeleteCategoryAlertDialog(CategoryItem categoryItem) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Alert !");
        builder.setMessage("Do you want to delete " + categoryItem.getCategoryName()+ " category"+ "?");

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                vibrator(context);

                deleteCategory(categoryItem.getCategoryName());

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

    private void deleteCategory(String categoryName) {

        String categoryId = getCategoryIdByCategoryName(categoryName);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Delete category from "categories" node
        databaseReference.child("categories").child(categoryId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete category from "menu_items" node
                            databaseReference.child("menu_items").child(categoryId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Category deleted successfully

                                                // Fetch the categories data and populate the categoryItemList
                                                fetchCategoriesData();

                                                Toast.makeText(context, categoryName+" Category deleted !", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Error deleting category from "menu_items" node
                                                Toast.makeText(context, "Error deleting category from menu_items !", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Error deleting category from "categories" node
                            Toast.makeText(context, "Error deleting category", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateCategoryName(String categoryId, String newCategoryName) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get the reference to the category you want to update
        DatabaseReference categoryRef = databaseReference.child("categories").child(categoryId);

        // Update the category name
        categoryRef.setValue(newCategoryName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // Fetch the categories data and populate the categoryItemList
                            fetchCategoriesData();

                            // Category name updated successfully
                            Toast.makeText(context, "Category Name Updated!", Toast.LENGTH_SHORT).show();

                        } else {
                            // Error updating category name
                            Toast.makeText(context, "Error updating category name!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void fetchCategoriesData() {

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

                // Notify to update recycler view
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method will be called if there is an error while fetching data
                Toast.makeText(context, "Error fetching categories data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
