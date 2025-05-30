package com.coderium.pos.billing;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.preferences.SettingsActivity.service_charges;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BillingActivity extends AppCompatActivity {

    RecyclerView recyclerViewBilling;
    BillingAdapter billItemAdapter;
    TextView tv_total,tv_service_charges;
    String order_id;
    TextView tv_order_id,tv_date;
    String today_date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_billing);
        getSupportActionBar().hide();


        order_id = String.valueOf(System.currentTimeMillis());

        tv_date = findViewById(R.id.tv_date);
        tv_order_id = findViewById(R.id.tv_order_id);
        tv_order_id.setText("#"+order_id);

        recyclerViewBilling = findViewById(R.id.recyclerViewBilling);
        tv_total = findViewById(R.id.tv_total);
        tv_service_charges = findViewById(R.id.tv_service_charges);

        // Get the current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        today_date = sdf.format(new Date());

        tv_date.setText(today_date);

        // Set up the RecyclerView adapter
        billItemAdapter = new BillingAdapter(CartMenuData.getAllMenuItems(),this,tv_total,tv_service_charges);
        recyclerViewBilling.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBilling.setAdapter(billItemAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void place_order(View view) {

        vibrator(this);

        // Prepare order items
        Map<String, OrderItem> orderItems = new HashMap<>();

        for (int i= 0; i < CartMenuData.getListSize(); i++) {
            orderItems.put(CartMenuData.getItem(i).getItemId(), new OrderItem(CartMenuData.getItem(i).getItemId(),CartMenuData.getItem(i).getItemName(),CartMenuData.getItem(i).getItemSize(), Integer.parseInt(CartMenuData.getItem(i).getItemQuantity()) ,CartMenuData.getItem(i).getItemPrice()));
        }

        // Calculate total price and service charges
        double totalPrice = 0;
        for (OrderItem orderItem : orderItems.values()) {
            totalPrice += orderItem.getPrice() * orderItem.getQuantity();
        }
        double serviceCharges = totalPrice * (service_charges / 100);
        totalPrice += serviceCharges;

        // Create the Order object
        Order order = new Order(order_id, today_date, totalPrice, serviceCharges, orderItems);

        // Save the order to the Firebase Realtime Database
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.child(order.getOrderId()).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(BillingActivity.this, "Order Placed Successfully !", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(BillingActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                    CartMenuData.clearList();

                    // For printing

//                    PrinterHelper printerHelper = new PrinterHelper(BillingActivity.this);
//                    printerHelper.printSampleText();

                } else {
                    // Failed to place the order
                    Toast.makeText(BillingActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
