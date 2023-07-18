package com.coderium.pos.reports;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.billing.Order;

import java.util.ArrayList;
import java.util.List;

public class AdvanceReportAdapter extends RecyclerView.Adapter<AdvanceReportAdapter.ReportViewHolder> {
    private List<Order> orderList;
    private Context context;

    public AdvanceReportAdapter(Context context) {
        this.context = context;
        this.orderList = new ArrayList<>();
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advance_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("#" + order.getOrderId());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalPrice.setText("PKR " + order.getTotalPrice());

        holder.rl_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Order order = orderList.get(position);
                Intent intent = new Intent(context, OrderSummaryActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("orderDate", order.getOrderDate());
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvTotalPrice;
        RelativeLayout rl_container;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_date);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            rl_container = itemView.findViewById(R.id.rl_container);
        }
    }
}
