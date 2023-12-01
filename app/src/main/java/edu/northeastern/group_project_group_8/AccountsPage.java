package edu.northeastern.group_project_group_8;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AccountsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts_page);

        ArrayList<String> accountNames = getAccountNames(); // Replace this with your data

        TextView myAccountsTitle = findViewById(R.id.myAccountsTitle);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        AccountAdapter adapter = new AccountAdapter(this, accountNames);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getAccountNames() {
        ArrayList<String> accountNames = new ArrayList<>();
        accountNames.add("acct1");
        accountNames.add("acct3");
        accountNames.add("acct1");
        accountNames.add("acct3");
        accountNames.add("acct1");
        accountNames.add("acct3");

        return accountNames;
    }
}