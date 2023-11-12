package edu.northeastern.group_project_group_8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;


public class Dashboard extends AppCompatActivity {
    String loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        loggedInUser = extras.getString("loggedInUsername");
        Log.d("", "User: " + loggedInUser);
    }
}