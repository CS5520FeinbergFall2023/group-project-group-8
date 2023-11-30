package edu.northeastern.group_project_group_8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<User> users;
    EditText usernameET;
    Button loginButton;
    String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        users = new ArrayList<User>();
        getUsersFromDB(mDatabase);
        usernameET = findViewById(R.id.usernameET);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsersFromDB(mDatabase);
                // If user leaves username field blank, we prompt them for the username before letting them proceed
                if (usernameET.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
                } else {
                    loggedInUsername = usernameET.getText().toString();
                    // Check to see if the username exists already.  if it does not, then we add it to the DB
                    ArrayList<String> usernameList = new ArrayList<String>();
                    for (User user : users) {
                        usernameList.add(user.getUsername());
                    }
                    if (!usernameList.contains(loggedInUsername)) {
                        User newUser = new User(loggedInUsername);
                        mDatabase.child(newUser.getUsername()).setValue(newUser);
                    }
                    launchUserDashboard();
//                    launchAccountDetailsPage();
                }
            }
        });
    }

    private void getUsersFromDB(DatabaseReference mDatabase) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("", "snapshot:" + snapshot.getValue());
                if (snapshot.getValue() != null) {
                    Map<String, Object> myUsersMap = (Map<String, Object>) snapshot.getValue();

                    users.clear();
                    for (String key: myUsersMap.keySet()) {
                        Map<String, Object> individualUserMap = (Map<String, Object>) myUsersMap.get(key);
                        String currentUserId = (String) individualUserMap.get("userId");
                        User currentUser = new User(key, currentUserId);
                        users.add(currentUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void launchUserDashboard() {
        Intent profileIntent = new Intent(this, Dashboard.class);
        profileIntent.putExtra("loggedInUsername", loggedInUsername);
        startActivity(profileIntent);
    }

    public void launchAccountDetailsPage() {
        Intent accountDetailsPageIntent = new Intent(this, AccountDetailsPage.class);
        accountDetailsPageIntent.putExtra("loggedInUsername", loggedInUsername);
        startActivity(accountDetailsPageIntent);
    }
}