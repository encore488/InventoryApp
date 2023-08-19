package com.zybooks.christopherwilliamsinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubmitActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        //Make 'navigateButton' call submitPageClick() when clicked
        Button navigateButton = findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPageClick();
            }
        });

        databaseHelper = new DatabaseHelper(this);


    }
    public void SayHelloClick(View view) {
        //Find username and password and use them to create strings "user" and "pass"
        EditText rEdit1 = findViewById(R.id.requestEdit1);
        EditText rEdit2 = findViewById(R.id.requestEdit2);
        String user = rEdit1.getText().toString();
        String pass = rEdit2.getText().toString();
        TextView rText = findViewById(R.id.resultText);

        //Use regex to find if there are ints in "user"
        String regex = ".*\\d.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(user);

        if (user.equals("")) {
            rText.setText("You must enter a name");
        } else if(pass.equals("")){
            rText.setText("Give us a password, " + user);
        }
        else if(matcher.matches()){
            rText.setText("Your username contains a number. That's not how we do things round here.");
        }
        else {
            insertUserIntoDatabase(user, pass);
        }
    }

    private void insertUserIntoDatabase(String username, String password) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        TextView rText = findViewById(R.id.resultText);

        long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (newRowId != -1) {
            rText.setText("User registration successful!");
        } else {
            // User insertion failed
            rText.setText("User registration failed. Please try again.");
        }

        db.close();
    }
//Button to return to login page
    public void submitPageClick(){
        Intent intent = new Intent(SubmitActivity.this, MainActivity.class);
        startActivity(intent);
    }
}