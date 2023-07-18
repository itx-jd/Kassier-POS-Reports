package com.coderium.pos.billing;

import com.coderium.pos.foodMenu.OnMenuItemsChangedListener;
import com.coderium.pos.foodMenu.MenuItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartMenuData {

    // Static list to hold MenuItem objects
    private static List<MenuItem> cartMenuItemsList = new ArrayList<>();

    // Listener to notify when menu items are changed
    private static OnMenuItemsChangedListener listener;

    // Method to set the listener
    public static void setOnMenuItemsChangedListener(OnMenuItemsChangedListener listener) {
        CartMenuData.listener = listener;
    }

    public static MenuItem getItem(int i){
        return cartMenuItemsList.get(i);
    }

    public static void clearList(){
        cartMenuItemsList.clear();
    }

    public static int getListSize(){
        return cartMenuItemsList.size();
    }

    // Method to insert data into the static list
    public static void addMenuItem(MenuItem menuItem) {
        cartMenuItemsList.add(menuItem);

        if (listener != null) {
            listener.onMenuItemsChanged(); // Notify the listener about the change
        }
    }

    // Method to get all the menu items from the static list
    public static List<MenuItem> getAllMenuItems() {
        return cartMenuItemsList;
    }

    // Method to check if an item with a specific itemId already exists in the list
    public static boolean isItemIdAlreadyInList(String itemId) {
        for (MenuItem menuItem : cartMenuItemsList) {
            if (menuItem.getItemId().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    // Method to remove a MenuItem from the list based on its itemId
    public static void removeMenuItemByItemId(String itemId) {

        Iterator<MenuItem> iterator = cartMenuItemsList.iterator();
        while (iterator.hasNext()) {
            MenuItem menuItem = iterator.next();
            if (menuItem.getItemId().equals(itemId)) {
                iterator.remove();

                if (listener != null) {
                    listener.onMenuItemsChanged(); // Notify the listener about the change
                }

                break;
            }
        }
    }

    // Method to update a MenuItem in the list based on its itemId
    public static void updateMenuItemByItemId(String itemId, MenuItem updatedMenuItem) {
        for (int i = 0; i < cartMenuItemsList.size(); i++) {
            MenuItem menuItem = cartMenuItemsList.get(i);
            if (menuItem.getItemId().equals(itemId)) {
                cartMenuItemsList.set(i, updatedMenuItem);
                break;
            }
        }
    }

    // Method to get the item quantity by itemId
    public static String getItemQuantityByItemId(String itemId) {
        for (MenuItem menuItem : cartMenuItemsList) {
            if (menuItem.getItemId().equals(itemId)) {
                return menuItem.getItemQuantity();
            }
        }

        return null;
    }
}
