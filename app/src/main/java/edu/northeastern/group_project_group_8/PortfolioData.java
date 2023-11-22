package edu.northeastern.group_project_group_8;

import android.util.Log;

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
import java.util.Scanner;

public class PortfolioData {
    ArrayList<String> positions;
    ArrayList<PositionPrice> positionPrices;
    ArrayList<Price> priceSumsByDate;

    public PortfolioData(ArrayList<String> positions) {
        this.positions = positions;
        this.positionPrices = new ArrayList<PositionPrice>();
        this.priceSumsByDate = new ArrayList<Price>();

        try {
            getAPIData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                PositionPrice currentPositionPrice = new PositionPrice("IBM");
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
//                        Log.d("", key + ": " + timeSeriesData.get(key));
                        JSONObject currentData = (JSONObject) timeSeriesData.get(key);
                        currentDate = LocalDate.parse(key);
                        currentPrice = Double.parseDouble((String) currentData.get("4. close"));
//                        Log.d("",currentDate+": "+currentPrice);
                        currentPositionPrice.prices.add(new Price(currentDate, currentPrice));
                    } catch (JSONException e) {
                        Log.d("", "Failed trying to iterate through time series data");
                        conn.disconnect();
                        throw new RuntimeException(e);
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
            for (LocalDate key : sumPricesMap.keySet()) {
                Price newPrice = new Price(key, sumPricesMap.get(key));
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
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is);
        return s.useDelimiter("\\A").next();
    }
}
