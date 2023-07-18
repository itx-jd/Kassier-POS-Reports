package com.coderium.pos.reports;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.reports.ReportsFragment.endDate;
import static com.coderium.pos.reports.ReportsFragment.startDate;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.billing.Order;
import com.coderium.pos.billing.OrderItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvanceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdvanceReportAdapter reportAdapter;
    public List<Order> orderList = new ArrayList<>();
    LinearLayout ll_not_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance);

        getSupportActionBar().setTitle("Sales Report");

        ll_not_found = findViewById(R.id.ll_not_found);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new AdvanceReportAdapter(this);
        recyclerView.setAdapter(reportAdapter);

        fetchOrders(startDate, endDate);

    }

    @Override
    protected void onResume() {
        fetchOrders(startDate, endDate);
        super.onResume();
    }

    private void fetchOrders(String startDate, String endDate) {
        Query query = FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("orderDate")
                .startAt(startDate)
                .endAt(endDate);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                orderList.clear();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {

                    Order order = orderSnapshot.getValue(Order.class);

                    if (order != null) {
                        Map<String, OrderItem> items = order.getItems();
                        if (items != null) {
                            order.setItems(new HashMap<>(items)); // To prevent Firebase warning about modifications to the map
                        }

                        orderList.add(order);
                    }
                }

                if(orderList.isEmpty()){
                    recyclerView.setVisibility(View.GONE);
                    ll_not_found.setVisibility(View.VISIBLE);
                }
                reportAdapter.setOrderList(orderList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_advance_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        vibrator(this);

        int itemId = item.getItemId();

        // Handle menu item clicks
        if (itemId == R.id.menu_item_download) {

            if(orderList.size() != 0){
                exportOrderToCSV();
            }else{
                Toast.makeText(this, "Nothing To Download !", Toast.LENGTH_SHORT).show();
            }

            return true;
        }else if(itemId == R.id.menu_item_chart){
            showOrderSummaryDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportOrderToCSV() {

        // Create a new file instance
        String file_name = java.util.UUID.randomUUID().toString();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Order_Report_" + file_name + ".csv");

        try {
            // Create a FileWriter object with the file
            FileWriter output = new FileWriter(file);

            // Create a CSVWriter object with the FileWriter
            CSVWriter writer = new CSVWriter(output);

            // Document Info
            String[] reportType = {"Report Type", "Order Report"};
            String[] reportPeriod = {"Report Period", startDate, endDate};
            String[] exportedDateTime = {"Exported Date and Time", Calendar.getInstance().getTime().toString()};
            String[] blank = {""};

            writer.writeNext(reportType);
            writer.writeNext(reportPeriod);
            writer.writeNext(exportedDateTime);
            writer.writeNext(blank);
            writer.writeNext(blank);

            // Write the header row
            String[] header = {"Order ID", "Date", "Items", "Service Charges", "Grand Total"};
            writer.writeNext(header);

            // Write the data rows
            for (Order order : orderList) {
                StringBuilder itemsBuilder = new StringBuilder();

                Map<String, OrderItem> items = order.getItems();
                if (items != null) {
                    for (Map.Entry<String, OrderItem> entry : items.entrySet()) {
                        String itemId = entry.getKey();
                        OrderItem item = entry.getValue();

                        String itemDetails = itemId + " - " + item.getItemName() + "," + item.getItemSize() + "," + item.getPrice() + "," + item.getQuantity();
                        itemsBuilder.append(itemDetails).append(";");
                    }
                }

                String[] data = {order.getOrderId(), order.getOrderDate(), itemsBuilder.toString(), String.valueOf(order.getServiceCharges()), String.valueOf(order.getTotalPrice())};
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

    private void showOrderSummaryDialog() {
        // Create the dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_report_chart);
        dialog.setTitle("Order Summary");

        // Get references to the dialog views
        TextView tvTotalSale = dialog.findViewById(R.id.tv_total_sale);
        TextView tvHighestSale = dialog.findViewById(R.id.tv_highest_sale);
        TextView tvTotalOrders = dialog.findViewById(R.id.tv_total_orders);
        TextView tvAverageSale = dialog.findViewById(R.id.tv_avg_sale);

        // Calculate the required information
        double totalSales = 0;
        double highestSale = 0;
        int totalOrders = orderList.size();

        for (Order order : orderList) {
            double grandTotal = order.getTotalPrice();
            totalSales += grandTotal;

            if (grandTotal > highestSale) {
                highestSale = grandTotal;
            }
        }

        double averageSale = totalSales / totalOrders;

        // Set the values to the dialog views
        tvTotalSale.setText("Rs. " + String.format("%.1f", totalSales));
        tvHighestSale.setText("Rs. " + String.format("%.1f", highestSale));
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvAverageSale.setText("Rs. " + String.format("%.1f", averageSale));

        // Show the dialog
        dialog.show();
    }

}