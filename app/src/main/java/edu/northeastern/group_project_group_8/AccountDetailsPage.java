package edu.northeastern.group_project_group_8;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountDetailsPage extends AppCompatActivity {
    String accountName = "Fidelity Account 1";
    double totalAsset = 23333.3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details_page);

        TextView accountNameTextView = findViewById(R.id.accountNameTextView);
        TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        LineChart lineChart = findViewById(R.id.lineChart);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        accountNameTextView.setText(accountName);
        totalAmountTextView.setText("Total Asset: $" + totalAsset);

        // Set up line chart
        // ...

        // Set up bottom navigation
//        bottomNavigationView.setOnNavigationItemSelectedListener(
//                new BottomNavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.action_home:
//                                // Handle home navigation
//                                break;
//                            case R.id.action_accounts:
//                                // Handle positions navigation
//                                break;
//                            case R.id.action_holdings:
//                                // Handle account details navigation
//                                break;
//                        }
//                        return true;
//                    }
//                });
    }
}
