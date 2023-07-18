package com.coderium.pos.reports;

import static com.coderium.pos.Constant.vibrator;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coderium.pos.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class ReportsFragment extends Fragment implements DatePickerDialog.OnDateSetListener{

    View view;
    LinearLayout ll_advance_report,ll_product_report;
    Button bt_from,bt_to;
    DatePickerDialog toDatePickerDialog,fromDatePickerDialog;
    String type; // advance - product

    double totalSales = 0;

    // Get the start and end date for the report
    public static String startDate = "2023-07-01";
    public static String endDate = "2023-07-31";

    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

    TextView tv_total_sale,tv_month_sale,tv_total_orders,tv_avg_sale,tv_today_sale,tv_yesterday_Sale,tv_month_name;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports, container, false);

        tv_total_sale = view.findViewById(R.id.tv_total_sale);
        tv_month_sale = view.findViewById(R.id.tv_month_sale);
        tv_total_orders = view.findViewById(R.id.tv_total_orders);
        tv_avg_sale = view.findViewById(R.id.tv_avg_sale);
        tv_today_sale = view.findViewById(R.id.tv_today_sale);
        tv_yesterday_Sale = view.findViewById(R.id.tv_yesterday_Sale);
        tv_month_name = view.findViewById(R.id.tv_month_name);

        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Format the current month name
        String currentMonthName = currentDate.format(DateTimeFormatter.ofPattern("MMMM"));
        tv_month_name.setText("Sales in "+currentMonthName);

        ll_product_report = view.findViewById(R.id.ll_product_report);

        ll_product_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "product";
                displayDialog();
            }
        });

        ll_advance_report = view.findViewById(R.id.ll_advance_report);

        ll_advance_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "advance";
                displayDialog();
            }
        });

        fetchInsights();

        return view;
    }

    private void displayDialog() {

        // Creating Dialog Box

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Setting Custom Layout

        View dialogView = inflater.inflate(R.layout.dialog_product_report, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Declare and Initialize

        String[] chipTitles = {"Today","Yesterday", "Last Week", "Last Month", "Current Year", "All Time","Custom Range"};
        ChipGroup chipGroupCategories = dialogView.findViewById(R.id.chipGroupCategories);
        Button bt_generate = dialogView.findViewById(R.id.bt_generate);
        MaterialButtonToggleGroup bt_group_date = dialogView.findViewById(R.id.bt_group_date);
        bt_from = dialogView.findViewById(R.id.bt_from);
        bt_to = dialogView.findViewById(R.id.bt_to);

        // By Default Settings

        bt_group_date.setVisibility(View.GONE);

        // Set the default From date to today's date
        String todayDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
        bt_to.setText(todayDate);
        // Set the default To date to one month after
        String oneMonthAfterDate = String.format("%04d-%02d-%02d", year, month - 1, dayOfMonth);
        bt_from.setText(oneMonthAfterDate);

        fromDatePickerDialog = new DatePickerDialog(getContext(), this, year, month-2, dayOfMonth);
        toDatePickerDialog = new DatePickerDialog(getContext(), this, year, month, dayOfMonth);

        // Display Chips

        for (int i = 0; i < chipTitles.length; i++) {
            String title = chipTitles[i];

            Chip chip = new Chip(requireContext());
            chip.setText(title);
            chip.setTag(title);
            chip.setChipBackgroundColorResource(R.color.gray_medium);

            // Set an OnClickListener for the chip
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Handle chip click event
                    // Perform your desired actions here

                    if(chip.getTag() == "Custom Range"){
                        bt_group_date.setVisibility(View.VISIBLE);
                    }else{
                        bt_group_date.setVisibility(View.GONE);
                    }

                    handleChipSelection(chip,chipGroupCategories);

                }
            });

            chipGroupCategories.addView(chip);

            // Click the first chip by default
            if (i == 0) {
                chip.performClick();
            }
        }

        bt_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker(fromDatePickerDialog);
            }
        });

        bt_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker(fromDatePickerDialog);
            }
        });

        bt_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrator(getContext());

                if(type == "product"){
                    startActivity(new Intent(getContext(), ProductReportActivity.class));
                    dialog.dismiss();
                }else{
                    startActivity(new Intent(getContext(), AdvanceActivity.class));
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void handleChipSelection(Chip selectedChip,ChipGroup chipGroupCategories) {

        // Update To and From Date
        if ("Today".equals(selectedChip.getTag())) {
            // Yesterday
            LocalDate today = LocalDate.now();
            startDate = today.toString();
            endDate = today.toString();

        } else if ("Yesterday".equals(selectedChip.getTag())) {
            // Yesterday
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            startDate = yesterday.toString();
            endDate = yesterday.toString();


        } else if ("Last Week".equals(selectedChip.getTag())) {
            // Last Week
            LocalDate today = LocalDate.now();
            LocalDate lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
            LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
            startDate = lastWeekStart.toString();
            endDate = lastWeekEnd.toString();

        } else if ("Last Month".equals(selectedChip.getTag())) {
            // Last Month
            LocalDate today = LocalDate.now();
            LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);
            LocalDate lastMonthEnd = lastMonthStart.plusMonths(1).minusDays(1);
            startDate = lastMonthStart.toString();
            endDate = lastMonthEnd.toString();


        } else if ("Current Year".equals(selectedChip.getTag())) {
            // Current Year
            LocalDate today = LocalDate.now();
            LocalDate currentYearStart = today.withDayOfYear(1);
            LocalDate currentYearEnd = today;
            startDate = currentYearStart.toString();
            endDate = currentYearEnd.toString();
        }
        else if ("All Time".equals(selectedChip.getTag())) {
            // Current Year
            LocalDate today = LocalDate.now();
            startDate = "2023-07-01";
            endDate = today.toString();
        }
        else if ("Custom Range".equals(selectedChip.getTag())) {
            // Current Year
            LocalDate today = LocalDate.now();
            startDate = today.minusMonths(2).toString();
            endDate = today.toString();

        }

        // Update color

        int childCount = chipGroupCategories.getChildCount();

        for (int i = 0; i < childCount; i++) {

            Chip chip = (Chip) chipGroupCategories.getChildAt(i);

            if (chip == selectedChip) {
                // Change the layout of the selected chip
                chip.setChipBackgroundColorResource(R.color.red);
                chip.setTextColor(getResources().getColor(R.color.white));

            }else{
                // Restore the layout of unselected chips
                chip.setChipBackgroundColorResource(R.color.gray_medium);
                chip.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    private void openDatePicker(DatePickerDialog datePickerDialog) {

        // Create and show the date picker dialog
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        // Determine which date picker dialog triggered the callback
        if (view == fromDatePickerDialog.getDatePicker()) {
            // It's the fromDatePickerDialog
            startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            bt_from.setText(startDate);

        } else if (view == toDatePickerDialog.getDatePicker()) {
            // It's the toDatePickerDialog
            endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            bt_to.setText(endDate);
            Toast.makeText(getContext(), endDate, Toast.LENGTH_SHORT).show();
        }

    }

    public void fetchInsights(){

        // Get a reference to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Assuming your orders are stored under a "orders" child node
        DatabaseReference ordersRef = database.getReference("orders");

        // Retrieve the total sales
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalSales = 0;
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    double totalPrice = orderSnapshot.child("totalPrice").getValue(Double.class);
                    totalSales += totalPrice;
                }
                // Set the total sales to the TextView
                tv_total_sale.setText(String.format("Rs. %.1f", totalSales));

                // Retrieve the average sale per day
                long daysOfMonth = LocalDate.now().lengthOfMonth();
                double averageSale = totalSales / daysOfMonth;
                // Set the average sale to the TextView
                tv_avg_sale.setText(String.format("Rs. %.1f", averageSale));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Retrieve the sales for the current month
        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Query currentMonthQuery = ordersRef.orderByChild("orderDate").startAt(currentYearMonth + "-01").endAt(currentYearMonth + "-31");
        currentMonthQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double monthSales = 0;
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    double totalPrice = orderSnapshot.child("totalPrice").getValue(Double.class);
                    monthSales += totalPrice;
                }
                // Set the month sales to the TextView
                tv_month_sale.setText(String.format("Rs. %.1f", monthSales));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Retrieve the total number of orders
        ordersRef.addChildEventListener(new ChildEventListener() {
            int totalOrders = 0;

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                totalOrders++;
                // Set the total orders to the TextView
                tv_total_orders.setText(String.valueOf(totalOrders));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle change if necessary
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                totalOrders--;
                // Set the total orders to the TextView
                tv_total_orders.setText(String.valueOf(totalOrders));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle movement if necessary
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Retrieve the sales for today
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Query todayQuery = ordersRef.orderByChild("orderDate").equalTo(today);
        todayQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double todaySales = 0;
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    double totalPrice = orderSnapshot.child("totalPrice").getValue(Double.class);
                    todaySales += totalPrice;
                }
                // Set the today's sales to the TextView
                tv_today_sale.setText(String.format("Rs. %.1f", todaySales));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Retrieve the sales for yesterday
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Query yesterdayQuery = ordersRef.orderByChild("orderDate").equalTo(yesterday);
        yesterdayQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double yesterdaySales = 0;
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    double totalPrice = orderSnapshot.child("totalPrice").getValue(Double.class);
                    yesterdaySales += totalPrice;
                }
                // Set the yesterday's sales to the TextView
                tv_yesterday_Sale.setText(String.format("Rs. %.1f", yesterdaySales));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

    }
}