package com.coderium.pos.reports;


import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.reports.ReportsFragment.endDate;
import static com.coderium.pos.reports.ReportsFragment.startDate;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.foodMenu.MenuItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductReportAdapter adapter;
    private List<MenuItem> productReportList;
    private DatabaseReference ordersRef;
    LinearLayout ll_not_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_report);

        getSupportActionBar().setTitle("Product Performance");

        ll_not_found = findViewById(R.id.ll_not_found);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productReportList = new ArrayList<>();
        adapter = new ProductReportAdapter(this, productReportList);
        recyclerView.setAdapter(adapter);

        ordersRef = FirebaseDatabase.getInstance().getReference().child("orders");

        generateReport(startDate, endDate);
    }

    private void generateReport(String startDate, String endDate) {

        Query query = ordersRef.orderByChild("orderDate").startAt(startDate).endAt(endDate);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Map to store the sales data for each item
                Map<String, List<MenuItem>> hashMap = new HashMap<>();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot itemsSnapshot = orderSnapshot.child("items");

                    for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                        String itemName = itemSnapshot.child("itemName").getValue(String.class);
                        int itemQuantity = itemSnapshot.child("quantity").getValue(Integer.class);
                        int itemPrice = itemSnapshot.child("price").getValue(Integer.class);
                        String itemSize = itemSnapshot.child("itemSize").getValue(String.class);

                        // Create a new MenuItem object
                        MenuItem menuItem = new MenuItem(itemName, itemPrice, itemSize,String.valueOf(itemQuantity));

                        // Check if the item is already in the hashMap
                        if (hashMap.containsKey(itemName)) {
                            // Get the existing list and add the new MenuItem
                            List<MenuItem> itemList = hashMap.get(itemName);
                            itemList.add(menuItem);
                        } else {
                            // Create a new list and add the MenuItem
                            List<MenuItem> itemList = new ArrayList<>();
                            itemList.add(menuItem);
                            hashMap.put(itemName, itemList);
                        }
                    }
                }

                // Process the hashMap to calculate the total quantity sold and generate the report
                generateSalesReport(hashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void generateSalesReport(Map<String, List<MenuItem>> hashMap) {

        // Iterate over the hashMap to calculate the total quantity sold and generate the report
        for (Map.Entry<String, List<MenuItem>> entry : hashMap.entrySet()) {
            String itemName = entry.getKey();
            List<MenuItem> itemList = entry.getValue();

            int totalQuantity = 0;
            int itemPrice = 0;
            String itemSize = "";

            for (MenuItem menuItem : itemList) {
                totalQuantity += Integer.parseInt(menuItem.getItemQuantity());
                itemPrice = menuItem.getItemPrice();
                itemSize = menuItem.getItemSize();
            }

            // Create a new MenuItem object with the calculated quantities and prices
            MenuItem productReport = new MenuItem(itemName, itemPrice, itemSize, String.valueOf(totalQuantity));

            // Add the product report to the list
            productReportList.add(productReport);
        }

        // Notify the adapter about the data change

        sortProductReportListByQuantity(productReportList);
    }

    private void sortProductReportListByQuantity(List<MenuItem> productReportList) {
        Collections.sort(productReportList, new Comparator<MenuItem>() {
            @Override
            public int compare(MenuItem item1, MenuItem item2) {
                // Sort in descending order based on item quantity
                return Integer.compare(Integer.parseInt(item2.getItemQuantity()), Integer.parseInt(item1.getItemQuantity()));
            }
        });

        adapter.notifyDataSetChanged();

        if(productReportList.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            ll_not_found.setVisibility(View.VISIBLE);
        }

    }

    private void exportReportToCSV() {

        // Create a new file instance
        String file_name = java.util.UUID.randomUUID().toString();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Product_Performance_Report_"+file_name+".csv");

        try {
            // Create a FileWriter object with the file
            FileWriter output = new FileWriter(file);

            // Create a CSVWriter object with the FileWriter
            CSVWriter writer = new CSVWriter(output);

            // Document Info

            String[] reportType = {"Report Type", "Product Performance Report"};
            String[] reportPeriod = {"Report Period", startDate, endDate};
            String[] exportedDateTime = {"Exported Date and Time", Calendar.getInstance().getTime().toString()};
            String[] blank = {""};

            writer.writeNext(reportType);
            writer.writeNext(reportPeriod);
            writer.writeNext(exportedDateTime);
            writer.writeNext(blank);
            writer.writeNext(blank);

            // Write the header row
            String[] header = {"Item Name", "Item Price", "Item Size", "Total Quantity","Sales Revenue"};
            writer.writeNext(header);

            // Write the data rows
            for (MenuItem item : productReportList) {

                int total = Integer.parseInt(item.getItemQuantity()) * item.getItemPrice();

                String[] data = {item.getItemName(), String.valueOf(item.getItemPrice()), item.getItemSize(), item.getItemQuantity(),String.valueOf(total)};
                writer.writeNext(data);
            }

            // Close the writer
            writer.close();

            // Display a success message or handle the file as needed
            // For example, you can show a Toast message:
            Toast.makeText(this, "CSV file exported successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("fie",e.getMessage());
            // Handle the exception
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {

        vibrator(this);

        int itemId = item.getItemId();

        // Handle menu item clicks
        if (itemId == R.id.menu_item_download) {
            
            if(productReportList.size() != 0){
                exportReportToCSV();
            }else{
                Toast.makeText(this, "Nothing To Download !", Toast.LENGTH_SHORT).show();
            }
            
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}