package com.zybooks.christopherwilliamsinventory;

import androidx.appcompat.app.AppCompatActivity;


import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Intent;
public class MainActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper = new DatabaseHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create button to navigate to account creation page, defined in "SubmitActivity.java"
        Button navigateButton = findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPageClick();
            }
        });

   }
    public void submitPageClick(){
        Intent intent = new Intent(MainActivity.this, SubmitActivity.class);
        startActivity(intent);
    }

    public void SayHelloClick(View view) {
        //Find username and password and use them to create strings "user" and "pass"
        EditText rEdit1 = findViewById(R.id.requestEdit1);
        EditText rEdit2 = findViewById(R.id.requestEdit2);
        TextView rText = findViewById(R.id.resultText);
        String user = rEdit1.getText().toString();
        String pass = rEdit2.getText().toString();
        boolean userExists = databaseHelper.userExists(user, pass);

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
        else if(userExists){
            //user and pass exist in database as a pair. Lets login and go to inventory page
            inventoryClick();
        }
        else{
            rText.setText("Invalid username and/or password");
        }
    }

    public void inventoryClick(){
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        startActivity(intent);
    }
}