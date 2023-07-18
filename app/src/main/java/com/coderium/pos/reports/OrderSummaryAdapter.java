package com.coderium.pos.reports;

import static com.coderium.pos.Constant.vibrator;
import static com.coderium.pos.preferences.SettingsActivity.service_charges;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.billing.OrderItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {

    private List<OrderItem> orderItems;
    private Context context;
    TextView tv_total,tv_service_charges;
    double grand_total = 0;

    public OrderSummaryAdapter(List<OrderItem> orderItems, Context context, TextView tv_total,TextView tv_service_charges) {
        this.orderItems = orderItems;
        this.context = context;
        this.tv_total = tv_total;
        this.tv_service_charges = tv_service_charges;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(position == 0){
            grand_total = 0;
        }

        OrderItem item = orderItems.get(position);

        holder.tvName.setText(item.getItemName());

        if(Objects.equals(item.getItemSize(), "None")){
            holder.tvSize.setText("");
        }else{
            holder.tvSize.setText(" ("+item.getItemSize()+")");
        }

        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvUnitPrice.setText("PKR " + item.getPrice());

        // calculate item total

        double item_total =  item.getPrice()* item.getQuantity();
        holder.tvTotalPrice.setText("PKR " + item_total);

        // calculate grand total
        grand_total += item_total;

        // Update the grand total and service charges after fetching all cart items

        if(position == orderItems.size()-1){

            double sv_charges = grand_total * (service_charges/ 100);

            tv_service_charges.setText("Rs. "+sv_charges);

            grand_total+=sv_charges;
            tv_total.setText("Rs. "+grand_total);
        }

        holder.rl_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showEditItemDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize, tvQuantity, tvUnitPrice, tvTotalPrice;
        RelativeLayout rl_container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            rl_container = itemView.findViewById(R.id.rl_container);
        }
    }

    private void showEditItemDialog(int position) {

        OrderItem item = orderItems.get(position);

        // Create a dialog instance
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(item.getItemName());

        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_modify_order_summary_item, null);
        builder.setView(dialogView);

        // Show the dialog
        AlertDialog dialog = builder.create();

        TextInputEditText quantityEditText = dialogView.findViewById(R.id.quantity_edit_text);
        Button bt_update = dialogView.findViewById(R.id.bt_update);

        quantityEditText.setText(String.valueOf(item.getQuantity()));


        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrator(context);

                item.setQuantity(Integer.parseInt(quantityEditText.getText().toString()));
                notifyDataSetChanged();
                dialog.dismiss();

            }
        });

        dialog.show();
    }

}
