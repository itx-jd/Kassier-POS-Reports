package com.coderium.pos.billing;

import static com.coderium.pos.preferences.SettingsActivity.service_charges;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.foodMenu.MenuItem;
import com.coderium.pos.R;

import java.util.List;
import java.util.Objects;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.ViewHolder> {

    private List<MenuItem> menuItems;
    private Context context;
    double grand_total = 0;
    TextView tv_total,tv_service_charges;

    public BillingAdapter(List<MenuItem> menuItems, Context context, TextView tv_total, TextView tv_service_charges) {
        this.menuItems = menuItems;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MenuItem menuItem = menuItems.get(position);

        holder.tvName.setText(menuItem.getItemName());

        if(Objects.equals(menuItem.getItemSize(), "None")){
            holder.tvSize.setText("");
        }else{
            holder.tvSize.setText(" ("+menuItem.getItemSize()+")");
        }

        holder.tvQuantity.setText(String.valueOf(menuItem.getItemQuantity()));
        holder.tvUnitPrice.setText("PKR " + menuItem.getItemPrice());

        // calculate item total

        int item_total =  menuItem.getItemPrice()* Integer.parseInt(menuItem.getItemQuantity());
        holder.tvTotalPrice.setText("PKR " + item_total);

        // calculate grand total
        grand_total += item_total;

        // Update the grand total and service charges after fetching all cart items

        if(position == menuItems.size()-1){

            double sv_charges = grand_total * (service_charges / 100);

            tv_service_charges.setText("Rs. "+sv_charges);

            grand_total+=sv_charges;
            tv_total.setText("Rs. "+grand_total);
        }

    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize, tvQuantity, tvUnitPrice, tvTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }
}
