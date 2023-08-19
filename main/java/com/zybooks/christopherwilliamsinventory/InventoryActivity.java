package com.zybooks.christopherwilliamsinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class InventoryActivity extends AppCompatActivity {

    private LinearLayout buttonContainer;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        buttonContainer = findViewById(R.id.buttonContainer);
        databaseHelper = new DatabaseHelper(this);
        loadItemsFromDatabase();
    }
    private void loadItemsFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_ID,
                DatabaseHelper.COLUMN_ITEM_QUANTITY
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_ITEMS, projection, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get the name, id and quantity
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_ID));
                int itemQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_QUANTITY));
                // Pass them to a new button
                addButtonToContainer(itemName, String.valueOf(itemId), itemQuantity);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
    }


    private long insertItemIntoInventory(String itemName, int quantity) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_NAME, itemName);
        values.put(DatabaseHelper.COLUMN_ITEM_QUANTITY, quantity);

        long newRowId = db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
        if (newRowId == -1) {
            // Insertion failed
            showShortToast();
        }
        db.close();
        return newRowId;
    }

    private void addButtonToContainer(String itemName, String itemId, int itemQuantity) {
        Button newButton = new Button(this);
        newButton.setText(itemName + " (Quantity: " + itemQuantity + ")"); // Display item name and quantity
        newButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        newButton.setOnClickListener(v -> itemClick(itemName, Integer.parseInt(itemId)));

        buttonContainer.addView(newButton);
    }

    public void newItemClick(View view) {
        // Create a new Button
        Button newButton = new Button(this);
        newButton.setText("Item");
        newButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        final String itemName = "Item"; // Capture the itemName here
        long newRowId = insertItemIntoInventory(itemName, 0); // Get the newRowId

        if (newRowId != -1) {
            // Successfully inserted, retrieve the new itemId
            int newItemId = (int) newRowId;

            // Set click listener for the new button
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClick(itemName, newItemId);
                }
            });

            // Add the new button to the buttonContainer
            buttonContainer.addView(newButton);
        } else {
            // Insertion failed, show toast
            showShortToast();
        }
    }
//What happens when you click on an item?
    public void itemClick(String itemName, int itemId) {
        Intent intent = new Intent(InventoryActivity.this, ItemActivity.class);
        intent.putExtra("ItemName", itemName);
        intent.putExtra("itemId", itemId);
        startActivity(intent);
    }

    public void showShortToast() {
        Toast.makeText(this, "Item Creation Failed", Toast.LENGTH_SHORT).show();
    }
}