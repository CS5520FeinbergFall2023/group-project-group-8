package edu.northeastern.group_project_group_8;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class PortfolioData {
    ArrayList<String> positions;
    ArrayList<PositionPrice> positionPrices;
    HashMap<String, HashMap<LocalDate, Double>> positionPricesV2;
    ArrayList<Price> priceSumsByDate;
    private DatabaseReference mDatabaseAccounts;
    private DatabaseReference mDatabaseHoldings;
    String user;
    ArrayList<String> accounts;
    ArrayList<Holding> holdings;
    HashMap<String, HashMap<LocalDate, HashMap<String, Long>>> accountHoldingsByDateMap;


    public PortfolioData(ArrayList<String> positions, String loggedInUser) throws InterruptedException {
        this.user = loggedInUser;
        this.positions = positions;
        this.positionPrices = new ArrayList<PositionPrice>();
        this.priceSumsByDate = new ArrayList<Price>();
        this.mDatabaseAccounts = FirebaseDatabase.getInstance().getReference().child("accounts");
        this.mDatabaseHoldings = FirebaseDatabase.getInstance().getReference().child("holdings");
        this.accounts = new ArrayList<String>();
        this.holdings = new ArrayList<Holding>();
        this.accountHoldingsByDateMap = new HashMap<>();
        this.positionPricesV2 = new HashMap<>();

        getAccountData();

    }

    private void buildPortfolio() {
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
                    if (accountHoldingsByDateMap.containsKey(currentAccount)) {
                        Log.d("", "Account already in map");
                        for (LocalDate date = currentStartDate; date.isBefore(currentEndDate) || date.isEqual(currentEndDate); date = date.plusDays(1)) {
//                            Log.d("", "Date: " + date);
                            accountHoldingsByDateMap.get(currentAccount).put(date, assetCount);
                        }
                    } else {
                        Log.d("", "Account NOT already in map");
                        HashMap<LocalDate, HashMap<String, Long>> dateAsset = new HashMap<>();
                        for (LocalDate date = currentStartDate; date.isBefore(currentEndDate) || date.isEqual(currentEndDate); date = date.plusDays(1)) {
//                            Log.d("", "Date: " + date);
                            dateAsset.put(date, assetCount);
                        }
                        accountHoldingsByDateMap.put(currentAccount, dateAsset);
                    }
                }
            }
        }
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
        }
    }

    private void getHoldingsData() {
        mDatabaseHoldings.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                Log.d("", "snapshot:" + snapshot.getValue());
                if (snapshot.getValue() != null) {
                    Map<String, Object> myHoldingsMap = (Map<String, Object>) snapshot.getValue();
                    Log.d("", "Keys: " + myHoldingsMap.keySet());
                    Log.d("", "holdings1: " + myHoldingsMap.get("holdings1"));
                    for (String key : myHoldingsMap.keySet()) {
                        HashMap<String, Object> currentHolding = (HashMap<String, Object>) myHoldingsMap.get(key);
                        String currentAcct = (String) currentHolding.get("account");
                        Log.d("", "currentAcct: " + currentAcct);
                        String currentAsset = (String) currentHolding.get("asset");
                        Log.d("", "currentasset: " + currentAsset);
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
                        holdings.add(new Holding(currentAcct, currentAsset, currentCount, currentStartDate, currentEndDate));
                    }
                    Log.d("", "Holdings length in getHoldingsData: " + holdings.size());
                    buildPortfolio();
                }
            }
        });
    }

    private void getAccountData() {
        Log.d("", "Getting account data");
        mDatabaseAccounts.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                Log.d("", "Account snapshot:" + snapshot.getValue());
                if (snapshot.getValue()!= null) {
                    Map<String, Object> myAccountsMap = (Map<String, Object>) snapshot.getValue();
                    Log.d("", "Account Keys: " + myAccountsMap.keySet());
                    accounts.clear();
                    for (String key : myAccountsMap.keySet()) {
                        accounts.add(key);
                    }
                    for (String account : accounts) {
                        Log.d("", "Account while getting accounts: " + account);
                    }
                    getHoldingsData();
                }
            }
        });
    }

    private void getAPIData() throws IOException {
        RunnableThread connectToNetworkThreadRunnable = new RunnableThread();
        new Thread(connectToNetworkThreadRunnable).start();

    }

    class RunnableThread implements Runnable {
        @Override
        public void run() {
            for (String position : positions) {
                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + position + "&apikey=demo";
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
                PositionPrice currentPositionPrice = new PositionPrice(symbol);
                HashMap<LocalDate, Double> pricesMap = new HashMap<>();
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
                positionPricesV2.put(symbol, pricesMap);
                Log.d("", "Prices Map: ");
                for (String key : positionPricesV2.keySet()) {
                    Log.d("", "Symbol: " + key);
                    for (LocalDate key2: positionPricesV2.get(key).keySet()) {
                        Log.d("", "Date: " + key2);
                        Log.d("", "Price: " + positionPricesV2.get(key).get(key2));
                    }
                }
//                for (int i = 0; i < currentPositionPrice.prices.size(); i++) {
//                    Log.d("", "Date: " + currentPositionPrice.prices.get(i).date + ", Price: " + currentPositionPrice.prices.get(i).price);
//                }
//                currentPositionPrice.prices.sort(new Comparator<Price>() {
//                    @Override
//                    public int compare(Price o1, Price o2) {
//                        if (o1.date.toEpochDay() > o2.date.toEpochDay()) {
//                            return 1;
//                        } else {
//                            return -1;
//                        }
//                    }
//                });
                positionPrices.add(currentPositionPrice);
                Log.d("", "positionPrices Length: " + positionPrices.size());
                conn.disconnect();
            }
            HashMap<LocalDate, Double> sumPricesMap = new HashMap<LocalDate, Double>();
            HashMap<LocalDate, Double> sumPricesMapV2 = new HashMap<LocalDate, Double>();
            for (PositionPrice positionPrice : positionPrices) {
                for (Price price : positionPrice.prices) {
                    if (!sumPricesMap.containsKey(price.date)) {
                        sumPricesMap.put(price.date, price.price);
                    } else {
                        Double temp = sumPricesMap.get(price.date);
                        temp += price.price;
                        sumPricesMap.put(price.date, temp);
                    }
                }
            }
            // New way of doing things with a map
            //TODO: need to only add if within range of ownership and add based on number of shares owned
            for (String accountKey : accountHoldingsByDateMap.keySet()) {
                for (LocalDate dateKey : accountHoldingsByDateMap.get(accountKey).keySet()) {
                    for (String assetKey : accountHoldingsByDateMap.get(accountKey).get(dateKey).keySet()) {
                        if (!sumPricesMapV2.containsKey(dateKey)) {
                            Log.d("", "Account: " + accountKey);
                            Log.d("", "Date: " + dateKey);
                            Log.d("", "Asset: " + assetKey);
                            Log.d("", "Count: " + accountHoldingsByDateMap.get(accountKey).get(dateKey).get(assetKey));
                            Log.d("", "Price: " + positionPricesV2.get(assetKey).get(dateKey));
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

//            for (String symbolKey : positionPricesV2.keySet()) {
//                for (LocalDate dateKey : positionPricesV2.get(symbolKey).keySet()) {
//                    if (!sumPricesMapV2.containsKey(dateKey)) {
//                        sumPricesMapV2.put(dateKey, positionPricesV2.get(symbolKey).get(dateKey));
//                    } else {
//                        Double temp = sumPricesMapV2.get(dateKey);
//                        temp += positionPricesV2.get(symbolKey).get(dateKey);
//                        sumPricesMapV2.put(dateKey, temp);
//                    }
//                }
//            }
            // Edited for new way of doing things with map
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
            for (Price price : priceSumsByDate) {
                Log.d("", "Date: " + price.date);
                Log.d("", "Value: " + price.price);
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is);
        return s.useDelimiter("\\A").next();
    }
}
