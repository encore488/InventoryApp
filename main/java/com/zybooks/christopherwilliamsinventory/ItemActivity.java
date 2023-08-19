package com.zybooks.christopherwilliamsinventory;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ItemActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private DatabaseHelper databaseHelper;
    private EditText itemNameEditText;
    private EditText itemQuantityEditText;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        final String itemName = getIntent().getStringExtra("itemName");
        final int itemQuantity = getIntent().getIntExtra("itemQuantity", 0);
        itemId = getIntent().getIntExtra("itemId", 0); // Get the itemId
        itemNameEditText = findViewById(R.id.nameEdit);
        itemQuantityEditText = findViewById(R.id.quantityEdit);
        Log.d("ItemActivity   LOG 1", "itemName: " + (itemNameEditText != null));
        Log.d("ItemActivity    LOG 1", "itemQuantity: " + (itemQuantityEditText != null));

        requestSmsPermission();

        databaseHelper = new DatabaseHelper(this); // Initialize databaseHelper

//Prefill boxes.     Doesn't work
        itemNameEditText.setText(itemName);
        itemQuantityEditText.setText(String.valueOf(itemQuantity));

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges(itemId, itemNameEditText.getText().toString(), Integer.parseInt(itemQuantityEditText.getText().toString())));
    }
// When you click the save button
    private void saveChanges(int itemId, String newName, int newQuantity) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_NAME, newName);
        values.put(DatabaseHelper.COLUMN_ITEM_QUANTITY, newQuantity);

        String selection = DatabaseHelper.COLUMN_ITEM_ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        int rowsAffected = db.update(DatabaseHelper.TABLE_ITEMS, values, selection, selectionArgs);

        if (rowsAffected > 0) {
            // Update successful
            showToast("Item updated successfully");
        } else {
            // Update failed
            showToast("Item update failed");
        }

        db.close();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void goBackToInventory(View view) {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_SEND_SMS);
        } else {
            handlePermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handlePermissionGranted();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    showPermissionExplanationDialog();
                } else {
                    showPermissionSettingsDialog();
                }
            }
        }
    }

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SMS Permission Required");
        builder.setMessage("The app requires SMS permission to send notifications.");
        builder.setPositiveButton("OK", (dialog, which) ->
                ActivityCompat.requestPermissions(ItemActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_SEND_SMS));
        builder.show();
    }

    private void showPermissionSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SMS Permission Required");
        builder.setMessage("SMS permission is required to send notifications. Please grant the permission from the app settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, PERMISSION_REQUEST_SEND_SMS);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void handlePermissionGranted() {
        String inputText = itemQuantityEditText.getText().toString().trim();
        if (!inputText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(inputText);
                if (quantity == 0) {
                    showWarningDialog();
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "!!!!!!handlePermissionGranted: itemQuantityEditText is null!!!!!!!!!");
            }
        }
    }


    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.warning_message);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            // Do we need to do something if the user clicks 'ok'?
        });
        builder.setCancelable(false); // Prevent dialog from being dismissed by the back button
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                handlePermissionGranted();
            } else {
                // Permission is still not granted
                // Show an appropriate message to the user
            }
        }
    }
    public void deleteItem(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteSelectedItem());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteSelectedItem() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_ITEM_ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        int rowsAffected = db.delete(DatabaseHelper.TABLE_ITEMS, selection, selectionArgs);

        if (rowsAffected > 0) {
            showToast("Item deleted successfully");
            goBackToInventory(null);
        } else {
            showToast("Item deletion failed");
        }

        db.close();
    }

}
