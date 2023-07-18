package com.coderium.pos.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coderium.pos.R;
import com.coderium.pos.foodMenu.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class ProductReportAdapter extends RecyclerView.Adapter<ProductReportAdapter.ProductViewHolder> {

    Context context;
    private List<MenuItem> productReportList;

    public ProductReportAdapter(Context context, List<MenuItem> productReportList) {
        this.context = context;
        this.productReportList = productReportList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        MenuItem item = productReportList.get(position);
        // Set the data to the views in the item layout
        holder.tvName.setText(item.getItemName());

        if(Objects.equals(item.getItemSize(), "None")){
            holder.tvSize.setText("");
        }else{
            holder.tvSize.setText(" ("+item.getItemSize()+")");
        }

        holder.tvQuantity.setText(String.valueOf(item.getItemQuantity()));
        holder.tvUnitPrice.setText("PKR " + item.getItemPrice());

        int total = Integer.parseInt(item.getItemQuantity()) * item.getItemPrice();
        holder.tvTotalPrice.setText("PKR " + String.valueOf(total));

    }

    @Override
    public int getItemCount() {
        return productReportList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize, tvQuantity, tvUnitPrice, tvTotalPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }
}

