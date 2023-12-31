package edu.northeastern.group_project_group_8;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.Hold;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class AccountDetailsPage extends AppCompatActivity {
    String accountName;
    double totalAsset;
    String loggedInUser;
    TextView totalAmountTextView;
    List<String> assetDetails;
    AccountHoldingsAdapter adapter;
    RecyclerView accountHoldingsRecyclerView;
    StringBuilder resultBuilder;
    private LineChart lineChart;
    private List<String> xValues;
    PortfolioData portfolioData;

    // Attributes from PortfolioData********
    // List of positions/assets in the portfolio
    ArrayList<String> positions;
    // Map of asset name to a map of date to price. Gives us a map of all asset prices across all dates for all assets in the portfolio
    HashMap<String, HashMap<LocalDate, Double>> positionPricesV2;
    // List of the sums of the values of all assets across the portfolio on each date.  This is what gets graphed to the screen.
    ArrayList<Price> priceSumsByDate;
    private DatabaseReference mDatabaseAccounts;
    private DatabaseReference mDatabaseHoldings;
    // List of all accounts in the portfolio
    ArrayList<String> accounts;
    // List of all holdings across all accounts in the portfolio.  This is pulled directly from the DB.
    ArrayList<Holding> holdings;
    // A map that gives the number of each asset held on each date in each account.
    // So we map the account name to a map of dates, which are each mapped to a map
    // of asset names, which each have a count value denoting how many of that asset
    // were held on that date in that account.  This is used to build priceSumsByDate.
    HashMap<String, HashMap<LocalDate, HashMap<String, Long>>> accountHoldingsByDateMap;

    FloatingActionButton addHoldingsFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details_page);

        TextView accountNameTextView = findViewById(R.id.accountNameTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        TextView holdingsTitle = findViewById(R.id.HoldingsTitle);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.action_accounts);
        addHoldingsFAB = findViewById(R.id.fabAddHolding);

        addHoldingsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddHoldingsDialog();
            }
        });

        Bundle extras = getIntent().getExtras();
        accountName = extras.getString("accountName");
        String platform = extras.getString("platform");
        totalAsset = extras.getDouble("totalAsset");
        loggedInUser = extras.getString("loggedInUserName");

        accountNameTextView.setText(platform + " " + accountName);
        String totalAssetString = String.format(Locale.getDefault(), "%.2f", totalAsset);
        totalAmountTextView.setText("Total Asset: $" + totalAssetString);

        resultBuilder = new StringBuilder();

        accountHoldingsRecyclerView = findViewById(R.id.accountHoldingsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        accountHoldingsRecyclerView.setLayoutManager(layoutManager);


        // Initializations from PortfolioData**********
        positions = new ArrayList<>();
        priceSumsByDate = new ArrayList<Price>();
        mDatabaseAccounts = FirebaseDatabase.getInstance().getReference().child("accounts");
        mDatabaseHoldings = FirebaseDatabase.getInstance().getReference().child("holdings");
        accounts = new ArrayList<String>();
        holdings = new ArrayList<Holding>();
        accountHoldingsByDateMap = new HashMap<>();
        positionPricesV2 = new HashMap<>();
        //*********************************************

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.action_home) {
                            launchUserDashboard();
                            return true;
                        } else if (item.getItemId() == R.id.action_accounts) {
                            launchAccountsPage();
                            return true;
                        }
                        return false;
                    }
                });

        getAccountData();
    }

    private void showAddHoldingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_holdings, null);
        builder.setView(dialogView);

        // Get references to UI elements in the dialog
        EditText editTextAssetTicker = dialogView.findViewById(R.id.editTextAssetTicker);
        ToggleButton buySellToggleButton = dialogView.findViewById(R.id.toggleButtonBuySell);
        EditText editTextCount = dialogView.findViewById(R.id.editTextCount);
        buySellToggleButton.setChecked(true);

        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Set up click listener for the "Save" button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle saving the account information
                String assetTicker = editTextAssetTicker.getText().toString().toUpperCase();
                int count = Integer.parseInt(editTextCount.getText().toString());
                Boolean buySell = buySellToggleButton.isChecked();

                // handle input and write account to DB
                if (buySell) {// Buy
                    if (positions.contains(assetTicker)) {
                        for (Holding holding : holdings) {
                            if (holding.endDate == null && holding.asset.toUpperCase().equals(assetTicker.toUpperCase())) {
                                HoldingUpload oldHoldingUpload = new HoldingUpload(holding.account, holding.asset, holding.count, holding.startDate.toString(), LocalDate.now().minusDays(1).toString());
                                HoldingUpload newHoldingUpload = new HoldingUpload(accountName, assetTicker, count + holding.count, LocalDate.now().toString(), "-1");
                                mDatabaseHoldings.child(holding.holdingKey).setValue(oldHoldingUpload);
                                mDatabaseHoldings.child(UUID.randomUUID().toString()).setValue(newHoldingUpload);
                            }
                        }
                        Toast.makeText(AccountDetailsPage.this, "Transaction Complete.", Toast.LENGTH_SHORT).show();
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    } else {
                        HoldingUpload newHoldingUpload = new HoldingUpload(accountName, assetTicker, count, LocalDate.now().toString(), "-1");
                        mDatabaseHoldings.child(UUID.randomUUID().toString()).setValue(newHoldingUpload);
                        Toast.makeText(AccountDetailsPage.this, "Transaction Complete.", Toast.LENGTH_SHORT).show();
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    }
                } else {// Sell
                    if (positions.contains(assetTicker) && accountHoldingsByDateMap.get(accountName).get(LocalDate.now()).get(assetTicker) >= count) {
                        Log.d("", "accountHoldingsByDate: " + accountHoldingsByDateMap.get(accountName).get(LocalDate.now()));
                        Log.d("", "accountHoldingsByDate: " + accountHoldingsByDateMap.get(accountName).get(LocalDate.now()).get(assetTicker));
                        Log.d("", "Today: " + LocalDate.now());
                        int numberToReduce = count;
                        for (Holding holding : holdings) {
                            Log.d("", "Holding Key: " + holding.holdingKey);
                            Log.d("", "Holding Asset: " + holding.asset);
                            Log.d("", "Holding End Date: " + holding.endDate);
                            Holding copiedHolding = new Holding(holding.holdingKey, holding.account, holding.asset, holding.count, holding.startDate, holding.endDate);
                            if (holding.asset.toUpperCase().equals(assetTicker.toUpperCase()) && numberToReduce > 0 && holding.endDate == null) {
                                //The account has enough shares to sell
                                if (holding.count > numberToReduce) {
                                    holding.count -= numberToReduce;
                                    numberToReduce = 0;
                                } else {
                                    numberToReduce -= holding.count;
                                    holding.count = 0;
                                }
                                HoldingUpload oldHoldingUpload = new HoldingUpload(copiedHolding.account, copiedHolding.asset, copiedHolding.count, copiedHolding.startDate.toString(), LocalDate.now().minusDays(1).toString());
                                HoldingUpload newHoldingUpload = new HoldingUpload(holding.account, holding.asset, holding.count, LocalDate.now().toString(), "-1");
                                mDatabaseHoldings.child(holding.holdingKey).setValue(oldHoldingUpload);
                                if (newHoldingUpload.count > 0) {
                                    mDatabaseHoldings.child(UUID.randomUUID().toString()).setValue(newHoldingUpload);
                                }
                            }
                        }
                        Toast.makeText(AccountDetailsPage.this, "Transaction Complete.", Toast.LENGTH_SHORT).show();
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(AccountDetailsPage.this, "You do not own enough shares to sell.", Toast.LENGTH_SHORT).show();
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    }
                }
            }
        });
    }

    public void launchUserDashboard() {
        Intent profileIntent = new Intent(this, Dashboard.class);
        profileIntent.putExtra("loggedInUsername", loggedInUser);
        startActivity(profileIntent);
    }

    public void launchAccountsPage() {
        Intent accountsPageIntent = new Intent(this, AccountsPage.class);
        accountsPageIntent.putExtra("loggedInUsername", loggedInUser);
        startActivity(accountsPageIntent);
    }

    private void getAccountData() {
        // getAccountData() goes to the DB and gets the list of accounts owned by the loggedInUser,
        // and then calls getHoldingsData().  We need to call getHoldingsData() from the onComplete
        // method in this function because we need to wait for the accounts data to get returned
        // before we get the holdings data.

        Log.d("", "Getting account data");
        Log.d("", "Current Thread_inGetAccountData: " + Thread.currentThread().toString());
        mDatabaseAccounts.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                accounts.clear();
                accounts.add(accountName);
                getHoldingsData();
            }
        });
    }

    private void getHoldingsData() {
        //getHoldingsData() goes to the DB and gets the holdings for any accounts in the accounts
        // list.  It then calls buildPortfolio().

        mDatabaseHoldings.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("", "Current Thread_inGetHoldingsDataListener: " + Thread.currentThread().toString());
                DataSnapshot snapshot = task.getResult();
                Log.d("", "snapshot:" + snapshot.getValue());
                if (snapshot.getValue() != null) {
                    Map<String, Object> myHoldingsMap = (Map<String, Object>) snapshot.getValue();
                    Log.d("", "Keys: " + myHoldingsMap.keySet());
                    Log.d("", "holdings1: " + myHoldingsMap.get("holdings1"));
                    for (String key : myHoldingsMap.keySet()) {
                        HashMap<String, Object> currentHolding = (HashMap<String, Object>) myHoldingsMap.get(key);
                        String currentAcct = (String) currentHolding.get("account");
                        if (accounts.contains(currentHolding.get("account"))) {
                            Log.d("", "currentAcct: " + currentAcct);
                            String currentAsset = (String) currentHolding.get("asset");
                            Log.d("", "currentasset: " + currentAsset);
                            positions.add(currentAsset);
                            long currentCount = (long) currentHolding.get("count");
                            Log.d("", "currentCount: " + currentCount);
                            LocalDate currentStartDate = LocalDate.parse((String) currentHolding.get("startDate"));
                            Log.d("", "currentStartDate: " + currentStartDate);
                            LocalDate currentEndDate = null;
                            Log.d("", "endDate: " + currentHolding.get("endDate"));
                            if (!currentHolding.get("endDate").equals("-1")) {
                                currentEndDate = LocalDate.parse((String) currentHolding.get("endDate"));
                                Log.d("", "currentEndDate: " + currentEndDate);
                            }
                            Log.d("", "newcurrentAcct: " + currentAcct);
                            Log.d("", "newcurrentAsset: " + currentAsset);
                            Log.d("", "newcurrentCount: " + currentCount);
                            Log.d("", "newcurrentStartDate: " + currentStartDate);
                            Log.d("", "newcurrentEndDate: " + currentEndDate);
                            holdings.add(new Holding(key, currentAcct, currentAsset, currentCount, currentStartDate, currentEndDate));
                        }
                    }
                    Log.d("", "Holdings length in getHoldingsData: " + holdings.size());
                    buildPortfolio();
                }
            }
        });
    }

    private void buildPortfolio() {
        // buildPortfolio() iterates through the accounts list and holdings list to build the
        // accountHoldingsByDateMap object, which gives us the number of shares of each asset
        // held in each account on each date.  This is needed to get the graph of total portfolio
        // value across all accounts by date.  Once we have this, we call getAPIData() to get
        // the price data and graph the portfolio values over time.

        Log.d("", "Current Thread_inBuildPortfolio: " + Thread.currentThread().toString());
        for (String account: accounts) {
            Log.d("", "account: " + account);
            Log.d("", "Holdings length in buildPortfolio: " + holdings.size());
            for (Holding holding : holdings) {
                Log.d("", "holding.account: " + holding.account);
                Log.d("", "account: " + account);
                if (Objects.equals(holding.account, account)) {
                    String currentAccount = holding.account;
                    Log.d("", "currentAccount: " + currentAccount);
                    String currentAsset = holding.asset;
                    Log.d("", "currentAsset: " + currentAsset);
                    long currentCount = holding.count;
                    Log.d("", "currentCount: " + currentCount);
                    LocalDate currentStartDate = holding.startDate;
                    Log.d("", "currentStartDate: " + currentStartDate);
                    LocalDate currentEndDate = LocalDate.now();
                    if (holding.endDate != null) {
                        currentEndDate = holding.endDate;
                    }
                    Log.d("", "currentEndDate: " + currentEndDate);
                    HashMap<String, Long> assetCount = new HashMap<>();
                    assetCount.put(currentAsset, currentCount);
                    //contains current account
                    if (accountHoldingsByDateMap.containsKey(currentAccount)) {
                        Log.d("", "Account already in map");
                        for (LocalDate date = currentStartDate; date.isBefore(currentEndDate) || date.isEqual(currentEndDate); date = date.plusDays(1)) {
                            Log.d("", "Date: " + date);
                            // contains account, contains date
                            if (accountHoldingsByDateMap.get(currentAccount).containsKey(date)) {
                                Log.d("", "Contains Account, Contains Date");
                                // contains account, contains date, contains asset
                                if (accountHoldingsByDateMap.get(currentAccount).get(date).containsKey(currentAsset)) {
                                    Log.d("", "Contains Account, Contains Date, contains Asset: " + currentAsset);
                                    Log.d("", "Incremementing " + currentAsset);
                                    long temp = accountHoldingsByDateMap.get(currentAccount).get(date).get(currentAsset);
                                    Log.d("", "Old count: " + temp);
                                    temp += currentCount;
                                    Log.d("", "New count: " + temp);
                                    accountHoldingsByDateMap.get(currentAccount).get(date).put(currentAsset, temp);
                                }
                                // contains account, contains date, does not contain asset
                                else {
                                    Log.d("", "Contains Account, Contains Date, DOES NOT contains Asset: " + currentAsset);
                                    accountHoldingsByDateMap.get(currentAccount).get(date).put(currentAsset, currentCount);
                                }
                            }
                            //contains account, does not contain date
                            else {
                                Log.d("", "Contains Account, DOES NOT contain Date, DOES NOT contains Asset: " + currentAsset);
                                HashMap<String, Long> newAssetCount = new HashMap<>();
                                newAssetCount.put(currentAsset, currentCount);
                                accountHoldingsByDateMap.get(currentAccount).put(date, newAssetCount);
                            }
                        }
                    }
                    // does not contain current account
                    else {
                        Log.d("", "Account NOT already in map");
                        HashMap<LocalDate, HashMap<String, Long>> dateAsset = new HashMap<>();
                        for (LocalDate date = currentStartDate; date.isBefore(currentEndDate) || date.isEqual(currentEndDate); date = date.plusDays(1)) {
                            Log.d("", "Date: " + date);
                            for (String accountKey : assetCount.keySet()) {
                                Log.d("", "Asset: " + accountKey);
                            }
                            Log.d("", "Count: " + assetCount.get(currentAsset));
                            HashMap<String, Long> newAssetCount = new HashMap<>();
                            newAssetCount.put(currentAsset, currentCount);
                            dateAsset.put(date, newAssetCount);
                        }
                        accountHoldingsByDateMap.put(currentAccount, dateAsset);
                    }
                }
            }
        }
        // This is just logging to see what is in the portfolio, to make sure the function
        // worked correctly.
        Log.d("", "Done Building Portfolio!");
        Log.d("", "Other Now logging accountHoldingsByDateMap");
        Log.d("", "Other accountHoldingsByDateMap Size: " + accountHoldingsByDateMap.size());
        for (String key : accountHoldingsByDateMap.keySet()) {
            Log.d("", "otherAccountHoldingsByDateMap Key: " + key);
            for (LocalDate key2 : accountHoldingsByDateMap.get(key).keySet()) {
                Log.d("", "Key within account: " + key2);
                for (String key3 : accountHoldingsByDateMap.get(key).get(key2).keySet()) {
                    Log.d("", "Key within date: " + key3);
                    Log.d("", "Value: " + accountHoldingsByDateMap.get(key).get(key2).get(key3));
                }
            }
        }
        try {
            getAPIData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getAPIData() throws IOException, InterruptedException {
        //getAPIData() calls the API for each asset to get the price history of each.
        // This gives us a map of prices data over time for each asset, which we can then combine
        // with the accountHoldingsByDateMap to get the total value of the portfolio across all
        // accounts/assets on each date.  This is the priceSumsByDate list, which is what we
        // then graph to the screen.  Graphing also takes place in this function, because we
        // need to wait for all of the previous calls to the DB and API to succeed before we try
        // to create the graph.  (Most of the API call logic is in the RunnableThread code below.)

        RunnableThread connectToNetworkThreadRunnable = new RunnableThread();
        Thread t1 = new Thread(connectToNetworkThreadRunnable);
        t1.start();
        t1.join();
        Log.d("", "Logging -------------------------------------------------------------from Dashboard");
        for (Price price : priceSumsByDate) {
            Log.d("", price.date + " called from Dashboard: " + price.price);
        }
        lineChart = findViewById(R.id.lineChart);
        Description description = new Description();
        description.setText("");
//        description.setPosition(250f, 150f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);
        ArrayList<String> portfolioDates = new ArrayList<>();
        for (Price date: priceSumsByDate) {
            portfolioDates.add(date.date.toString());
        }
//        xValues = Arrays.asList(String.valueOf(portfolioDates));
        xValues = portfolioDates;

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);
//        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = lineChart.getAxisLeft();
//        yAxis.setAxisMinimum(0f);
//        yAxis.setAxisMaximum(500f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);
        yAxis.setDrawGridLines(false);

        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < priceSumsByDate.size(); i++) {
            Price currentPrice = priceSumsByDate.get(i);
            entries1.add(new Entry(i, currentPrice.price.floatValue()));
        }

        LineDataSet dataSet1 = new LineDataSet(entries1, "Math");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setDrawCircles(false);
//        dataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        dataSet1.setCubicIntensity(1f);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        LineData lineData = new LineData(dataSet1);
        lineData.setDrawValues(false);

        MarkerView marker = new CustomMarkerView(this, R.layout.marker_view);

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChart.highlightValue(h);
                Log.d("", "Highlight: " + h.toString());
                lineChart.setMarker(marker);
                marker.refreshContent(e, h);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        lineChart.setData(lineData);
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.animateY(1000);

        lineChart.invalidate();
    }

    class RunnableThread implements Runnable {
        @Override
        public void run() {
            for (String position : positions) {
//                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + position + "&apikey=demo";
//                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + position + "&apikey=KFUT67C4LM82DY2I"; // Chris API Key
                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + position + "&outputsize=full&apikey=2GSV7G6LKLO25ABN"; // Paid API Key
                URL url = null;
                String symbol = null;
                try {
                    url = new URL(urlString);
                    Log.d("", "urlString");
                } catch (MalformedURLException e) {
                    Log.e("Network Error", e.getMessage());  // Replace RuntimeException

                }
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.e("Network Error", e.getMessage());  // Replace RuntimeException

                }
                try {
                    conn.setRequestMethod("GET");
                } catch (ProtocolException e) {
                    Log.e("Network Error", e.getMessage());  // Replace RuntimeException

                }
                conn.setDoInput(true);
                try {
                    conn.connect();
                } catch (IOException e) {
                    Log.e("Network Error", e.getMessage());  // Replace RuntimeException
                }
                InputStream inputStream = null;
                try {
                    inputStream = conn.getInputStream();
                } catch (IOException e) {
                    Log.e("Network Error", e.getMessage());  // Replace RuntimeException

                }
                final String resp = convertStreamToString(inputStream);
                JSONObject responseObject = null;
                try {
                    responseObject = new JSONObject(resp);
                } catch (JSONException e) {
                    Log.d("", e.toString());
                }
                Log.d("", "responseObject: " + responseObject);
                Log.d("", "responseObject Class:" + responseObject.getClass());

                try {
                    JSONObject metaData = (JSONObject) responseObject.get("Meta Data");
                    Log.d("", "Meta Data: " + metaData);
                    symbol = (String) metaData.get("2. Symbol");
                    Log.d("", "Symbol: " + symbol);
                } catch (JSONException e) {
                    Log.d("", "Failed trying to get meta Data.");
                    conn.disconnect();
                    throw new RuntimeException(e);
                }

                JSONObject timeSeriesData;
                try {
                    timeSeriesData = (JSONObject) responseObject.get("Time Series (Daily)");
                    Log.d("", "Time Series Data: " + timeSeriesData);
                } catch (JSONException e) {
                    Log.d("", "Failed trying to get Time Series Data.");
                    conn.disconnect();
                    throw new RuntimeException(e);
                }
                Iterator<String> keys = timeSeriesData.keys();
                LocalDate currentDate = null;
                Double currentPrice = null;

                // The PositionPrice object has an asset/position name and a list of prices,
                // so we have one for each asset, and it holds all of the price history for
                // that asset.
                PositionPrice currentPositionPrice = new PositionPrice(symbol);
                HashMap<LocalDate, Double> pricesMap = new HashMap<>();
                // Here we iterate through all of the dates in the api response to get the dat
                // and price on that date.
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
//                        Log.d("", key + ": " + timeSeriesData.get(key));
                        JSONObject currentData = (JSONObject) timeSeriesData.get(key);
                        currentDate = LocalDate.parse(key);
                        currentPrice = Double.parseDouble((String) currentData.get("4. close"));
//                        Log.d("",currentDate+": "+currentPrice);
                        currentPositionPrice.prices.add(new Price(currentDate, currentPrice));
                        // New way of doing things with a map
                        for (String accountKey : accountHoldingsByDateMap.keySet()) {
                            if (accountHoldingsByDateMap.get(accountKey).containsKey(currentDate)) {
                                if (accountHoldingsByDateMap.get(accountKey).get(currentDate).containsKey(symbol)) {
                                    pricesMap.put(currentDate, currentPrice);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("", "Failed trying to iterate through time series data");
                        conn.disconnect();
                        throw new RuntimeException(e);
                    }
                }
                // Here we add the map of dates/prices for this asset to positionPricesV2
                positionPricesV2.put(symbol, pricesMap);
                // This is just some logging.
                Log.d("", "Prices Map: ");
                for (String key : positionPricesV2.keySet()) {
                    Log.d("", "Symbol: " + key);
                    for (LocalDate key2: positionPricesV2.get(key).keySet()) {
                        Log.d("", "Date: " + key2);
                        Log.d("", "Price: " + positionPricesV2.get(key).get(key2));
                    }
                }
                conn.disconnect();
            }

            //Here is where we start building the sumPrices object. We start with a map so we can
            // quickly populate it by date without needing to iterate through the object
            // unnecessarily.  We then convert it to a list at the end for graphing purposes.
            HashMap<LocalDate, Double> sumPricesMapV2 = new HashMap<LocalDate, Double>();
            for (String accountKey : accountHoldingsByDateMap.keySet()) {
                for (LocalDate dateKey : accountHoldingsByDateMap.get(accountKey).keySet()) {
                    for (String assetKey : accountHoldingsByDateMap.get(accountKey).get(dateKey).keySet()) {
                        if (!sumPricesMapV2.containsKey(dateKey)) {

                            if (positionPricesV2.get(assetKey).containsKey(dateKey)) {
                                sumPricesMapV2.put(dateKey, positionPricesV2.get(assetKey).get(dateKey) * accountHoldingsByDateMap.get(accountKey).get(dateKey).get(assetKey));
                            }
                        } else {
                            if (positionPricesV2.get(assetKey).containsKey(dateKey)) {
                                Double temp = sumPricesMapV2.get(dateKey);
                                temp += positionPricesV2.get(assetKey).get(dateKey) * accountHoldingsByDateMap.get(accountKey).get(dateKey).get(assetKey);
                                sumPricesMapV2.put(dateKey, temp);
                            }
                        }
                    }
                }
            }
            // here is where we add the total values to the priceSumsByDate list.
            // the Price objects that we are adding here are a class that has two fields:
            // a date and a value/price.  We build this list from the sumPricesMapV2 object,
            // which uses a map to quickly get all of this data without needing to iterate
            // through each item in a list.
            for (LocalDate key : sumPricesMapV2.keySet()) {
                Price newPrice = new Price(key, sumPricesMapV2.get(key));
                priceSumsByDate.add(newPrice);
            }
            priceSumsByDate.sort(new Comparator<Price>() {
                @Override
                public int compare(Price o1, Price o2) {
                    if (o1.date.toEpochDay() > o2.date.toEpochDay()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            Log.d("", "Logging priceSumsByDate");
            for (Price price : priceSumsByDate) {
                Log.d("", "Date: " + price.date);
                Log.d("", "Value: " + price.price);
            }

            LocalDate dateKey = priceSumsByDate.get(priceSumsByDate.size() - 1).date;
            assetDetails = new ArrayList<>();
            for (String assetKey : accountHoldingsByDateMap.get(accountName)
                    .get(dateKey)
                    .keySet()) {
                resultBuilder.setLength(0);
                resultBuilder.append(assetKey).append(": ");
                resultBuilder.append(accountHoldingsByDateMap.get(accountName).get(dateKey).get(assetKey)).append(" share at $");
                resultBuilder.append(positionPricesV2.get(assetKey).get(dateKey));

                String resultString = resultBuilder.toString();
                assetDetails.add(resultString);
            }


            runOnUiThread(() -> {
                // Ensure UI updates are done on the UI thread
                adapter = new AccountHoldingsAdapter(assetDetails);
                accountHoldingsRecyclerView.setAdapter(adapter);
            });
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is);
        return s.useDelimiter("\\A").next();
    }

    public class CustomMarkerView extends MarkerView {
        TextView valueView;
        TextView dateView;

        /**
         * Constructor. Sets up the MarkerView with a custom layout resource.
         *
         * @param context
         * @param layoutResource the layout resource to use for the MarkerView
         */
        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            valueView = findViewById(R.id.valueView);
            dateView = findViewById(R.id.dateView);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            valueView.setText("$" + String.valueOf(highlight.getY()));
            LocalDate date = priceSumsByDate.get((int)highlight.getX()).date;
            dateView.setText(date.toString());
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {

            if(mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight()-200);
            }

            return mOffset;
        }
    }
}
