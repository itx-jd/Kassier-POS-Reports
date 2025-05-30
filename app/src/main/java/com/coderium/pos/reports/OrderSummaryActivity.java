package com.coderium.pos.reports;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.preferences.SettingsActivity.service_charges;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.billing.Order;
import com.coderium.pos.billing.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSummaryActivity extends AppCompatActivity {

    public List<OrderItem> orderItemsList = new ArrayList<>();;
    RecyclerView recyclerView;
    OrderSummaryAdapter orderSummaryAdapter;
    TextView tv_total,tv_service_charges;
    TextView tv_order_id,tv_date;
    String orderId,orderDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_order_summary);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        orderDate = intent.getStringExtra("orderDate");

        tv_order_id = findViewById(R.id.tv_order_id);
        tv_date = findViewById(R.id.tv_date);

        tv_order_id.setText("#"+orderId);
        tv_date.setText(orderDate);

        tv_total = findViewById(R.id.tv_total);
        tv_service_charges = findViewById(R.id.tv_service_charges);
        recyclerView = findViewById(R.id.recyclerView);

        // Set up the RecyclerView adapter
        orderSummaryAdapter = new OrderSummaryAdapter(orderItemsList ,this,tv_total,tv_service_charges);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderSummaryAdapter);
        fetchOrderItems();

    }

    public void fetchOrderItems() {

        // Retrieve the order item list from Firebase Realtime Database
        DatabaseReference orderItemsRef = FirebaseDatabase.getInstance().getReference()
                .child("orders")
                .child(orderId)
                .child("items");
        orderItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    orderItemsList.clear();
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        OrderItem orderItem = itemSnapshot.getValue(OrderItem.class);
                        orderItemsList.add(orderItem);
                    }
                    orderSummaryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error gracefully
            }
        });
    }

    public void update_order(View view) {

        vibrator(this);

        // Prepare order items
        Map<String, OrderItem> orderItems = new HashMap<>();

        for (int i= 0; i < orderItemsList.size(); i++) {
            orderItems.put(orderItemsList.get(i).getItemId(), new OrderItem(orderItemsList.get(i).getItemId(),orderItemsList.get(i).getItemName(),orderItemsList.get(i).getItemSize(), orderItemsList.get(i).getQuantity() ,orderItemsList.get(i).getPrice()));
        }

        // Calculate total price and service charges
        double totalPrice = 0;
        for (OrderItem orderItem : orderItems.values()) {
            totalPrice += orderItem.getPrice() * orderItem.getQuantity();
        }
        double serviceCharges = totalPrice *  (service_charges/ 100);
        totalPrice += serviceCharges;

        // Create the Order object
        Order order = new Order(orderId, orderDate,totalPrice,serviceCharges, orderItems);

        // Save the order to the Firebase Realtime Database
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.child(order.getOrderId()).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(OrderSummaryActivity.this ,"Order Update Successfully !", Toast.LENGTH_SHORT).show();

                } else {
                    // Failed to place the order
                    Toast.makeText(OrderSummaryActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}