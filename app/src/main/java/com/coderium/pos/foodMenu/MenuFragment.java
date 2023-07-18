package com.coderium.pos.foodMenu;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.dashboard.CategoryUtils.getCategoryIdByCategoryName;

import com.coderium.pos.dashboard.CategoryItem;
import com.coderium.pos.R;
import com.coderium.pos.billing.CartMenuData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment implements OnMenuItemsChangedListener {

    private View view;
    private ChipGroup chipGroupCategories;
    private RecyclerView recyclerViewMenuItems;
    private MenuAdapter menuItemAdapter;
    private DatabaseReference menuItemsRef;
    LinearLayout ll_not_found;

    public  static MenuItem menuItem;
    Button btn_bill;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        setHasOptionsMenu(true);

        ll_not_found = view.findViewById(R.id.ll_not_found);

        loadMainContent();

        return view;
    }

    public void loadMainContent(){

        btn_bill = view.findViewById(R.id.btn_bill);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        recyclerViewMenuItems = view.findViewById(R.id.recyclerViewMenuItem);

        // Set the layout manager for the RecyclerView
        recyclerViewMenuItems.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        fetchCategoriesFromFirebase();

        // Set the listener in the StaticMenuItemData class to this fragment
        CartMenuData.setOnMenuItemsChangedListener(this);

        onMenuItemsChanged();

    }

    private void fetchCategoriesFromFirebase() {

        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference().child("categories");
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryItem> categoriesList = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryId = categorySnapshot.getKey();
                    String categoryName = categorySnapshot.getValue(String.class);
                    categoriesList.add(new CategoryItem(categoryId, categoryName));
                }
                displayCategoriesInChips(categoriesList);

                // After fetching categories, select the first chip by default
                if (chipGroupCategories.getChildCount() > 0) {
                    Chip firstChip = (Chip) chipGroupCategories.getChildAt(0);
                    handleChipSelection(firstChip);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed.
            }
        });
    }

    private void displayCategoriesInChips(List<CategoryItem> categoriesList) {

        // Clear the chipGroupCategories before adding new chips
        chipGroupCategories.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (CategoryItem category : categoriesList) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_category, chipGroupCategories, false);
            chip.setText(category.getCategoryName());
            chip.setTag(category.getCategoryId());
            chip.setCheckable(true);
            chipGroupCategories.addView(chip);

            // Set item click listener for each chip
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vibrator(getContext());
                    handleChipSelection(chip);
                }
            });
        }
    }

    private void handleChipSelection(Chip selectedChip) {

        int childCount = chipGroupCategories.getChildCount();
        for (int i = 0; i < childCount; i++) {

            Chip chip = (Chip) chipGroupCategories.getChildAt(i);

            if (chip == selectedChip) {
                // Change the layout of the selected chip
                chip.setChipBackgroundColorResource(R.color.red);
                chip.setTextColor(getResources().getColor(R.color.white));

                fetchMenuItemsForCategory(chip.getText().toString());

            } else {
                // Restore the layout of unselected chips
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    private void fetchMenuItemsForCategory(String categoryName) {


        menuItemsRef = FirebaseDatabase.getInstance().getReference().child("menu_items");

        String categoryId = getCategoryIdByCategoryName(categoryName);

        menuItemsRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    recyclerViewMenuItems.setVisibility(View.VISIBLE);
                    ll_not_found.setVisibility(View.GONE);

                    displayMenuItemsInRecyclerView(menuItemsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed.
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMenuItemsInRecyclerView(List<MenuItem> menuItemsList) {
        menuItemAdapter = new MenuAdapter(menuItemsList,getContext());
        recyclerViewMenuItems.setAdapter(menuItemAdapter);
    }

    @Override
    public void onMenuItemsChanged() {

        if(CartMenuData.getListSize() > 0){
            btn_bill.setVisibility(View.VISIBLE);
        }else{
            btn_bill.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {

        loadMainContent();

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {

        if (item.getItemId() == R.id.menu_item_delete) {
            vibrator(getContext());
            CartMenuData.clearList();
            onResume();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
