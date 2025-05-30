package com.coderium.pos.foodMenu;

import static com.coderium.pos.Constant.vibrator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.coderium.pos.R;
import com.coderium.pos.billing.CartMenuData;

import java.util.List;
import java.util.Objects;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private List<MenuItem> menuItemList;
    public Context context;

    public MenuAdapter(List<MenuItem> menuItemList, Context context) {
        this.menuItemList = menuItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MenuItem menuItem = menuItemList.get(position);

        holder.itemNameTextView.setText(menuItem.getItemName());
        holder.itemSizeTextView.setText(menuItem.getItemSize());

        if(Objects.equals(menuItem.getItemSize(), "None")){
            holder.itemSizeTextView.setVisibility(View.GONE);
        }

        updateColorsOfSelectedItems(menuItem,holder);

        holder.cv_menuItem.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                vibrator(context);
                showMenuItemDialog(menuItem,holder,position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemNameTextView,itemSizeTextView;
        private CardView cv_menuItem;
        private LinearLayout ll_background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemSizeTextView = itemView.findViewById(R.id.itemSizeTextView);
            cv_menuItem = itemView.findViewById(R.id.cv_menuItem);
            ll_background = itemView.findViewById(R.id.ll_background);

        }

    }

    private void showMenuItemDialog(MenuItem menuItem, ViewHolder holder, int position) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_menu_item);

        TextView itemNameTextViewDialog = dialog.findViewById(R.id.itemNameTextViewDialog);
        TextView tv_size = dialog.findViewById(R.id.tv_size);
        TextView itemDescriptionTextViewDialog = dialog.findViewById(R.id.itemDescriptionTextViewDialog);
        TextView itemPriceTextViewDialog = dialog.findViewById(R.id.itemPriceTextViewDialog);
        EditText quantityEditText = dialog.findViewById(R.id.quantityEditText);
        Button okButton = dialog.findViewById(R.id.okButton);

        itemNameTextViewDialog.setText(menuItem.getItemName());

        if(Objects.equals(menuItem.getItemSize(), "None")){
            tv_size.setText("");
        }else{
            tv_size.setText("("+menuItem.getItemSize()+")");
        }


        itemDescriptionTextViewDialog.setText(menuItem.getItemDescription());
        itemPriceTextViewDialog.setText("Rs. "+String.valueOf(menuItem.getItemPrice()));

        if(CartMenuData.isItemIdAlreadyInList((menuItem.getItemId()))){
            quantityEditText.setText(CartMenuData.getItemQuantityByItemId(menuItem.getItemId()));
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                vibrator(context);

                String quantityStr = quantityEditText.getText().toString().trim();
                int quantity = 0;
                if (!quantityStr.isEmpty()) {
                    quantity = Integer.parseInt(quantityStr);
                }

                // Update the view here (e.g., change item color) based on the quantity value
                if (quantity > 0) {

                    MenuItem cartMenuItem = new MenuItem(menuItem.getCategoryId(), menuItem.getItemDescription(), menuItem.getItemId(),
                            menuItem.getItemName(), menuItem.getItemPrice(),menuItem.getItemSize(),String.valueOf(quantity));

                    if(CartMenuData.isItemIdAlreadyInList(menuItem.getItemId())){
                        CartMenuData.updateMenuItemByItemId(menuItem.getItemId(),cartMenuItem);
                    }else{

                        // It item is not already present in cart
                        CartMenuData.addMenuItem(cartMenuItem);

                    }

                }else{

                    // If item already present in cart then remove it from cart

                    if(CartMenuData.isItemIdAlreadyInList(menuItem.getItemId())){
                        CartMenuData.removeMenuItemByItemId(menuItem.getItemId());
                    }
                }

                dialog.dismiss();
                updateColorsOfSelectedItems(menuItem,holder);
            }
        });


        // Plus Button

        ImageView plus_button = dialog.findViewById(R.id.btn_plus);
        plus_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                vibrator(context);

                if(quantityEditText.getText().toString().isEmpty()){
                    quantityEditText.setText("0");
                }

                int current_quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
                quantityEditText.setText(String.valueOf(current_quantity+1));
            }
        });

        // Minus Button

        ImageView minus_button = dialog.findViewById(R.id.btn_minus);
        minus_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                vibrator(context);

                if(quantityEditText.getText().toString().isEmpty()){
                    quantityEditText.setText("0");
                }

                int current_quantity = Integer.parseInt(quantityEditText.getText().toString().trim());

                if(current_quantity > 0){
                    quantityEditText.setText(String.valueOf(current_quantity-1));
                }

            }
        });

        dialog.show();
    }

    public void updateColorsOfSelectedItems(MenuItem menuItem,ViewHolder holder){

        if(CartMenuData.isItemIdAlreadyInList(menuItem.getItemId())){

            holder.ll_background.setBackgroundColor(context.getResources().getColor(R.color.orange));
            holder.itemNameTextView.setTextColor(context.getResources().getColor(R.color.red));
            holder.itemSizeTextView.setTextColor(context.getResources().getColor(R.color.red));
        }else{
            // If the quantity is greater than 0, change item color to black and text to white
            holder.ll_background.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.itemNameTextView.setTextColor(context.getResources().getColor(R.color.black));
            holder.itemSizeTextView.setTextColor(context.getResources().getColor(R.color.black));
        }

    }
}