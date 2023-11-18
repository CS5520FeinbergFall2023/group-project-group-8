package edu.northeastern.group_project_group_8;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;


import java.util.ArrayList;
import java.util.List;


public class Dashboard extends AppCompatActivity {
    String loggedInUser;
    private LineChart lineChart;
    private List<String> xValues;
    PortfolioData portfolioData;

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

        ArrayList<String> positionList = new ArrayList<String>();
        positionList.add("IBM");
        positionList.add("IBM");
//        positionList.add("IBM");
        portfolioData = new PortfolioData(positionList);
        //TODO: Figure out how to wait for API call to finish before moving forward with populating this page.  Sleep() is not a viable option.
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        for (PositionPrice positionPrice : portfolioData.positionPrices) {
//            for(Price price : positionPrice.prices) {
//                Log.d("", positionPrice.positionName + ": " + price.date + ": " + price.price);
//            }
//        }
        for (Price price : portfolioData.priceSumsByDate) {
            Log.d("", price.date + ": " + price.price);
        }

        // This code is just to test out the graphing functionality
//        lineChart = findViewById(R.id.lineChart);
//        Description description = new Description();
//        description.setText("Student Record");
//        description.setPosition(150f, 15f);
//        lineChart.setDescription(description);
//        lineChart.getAxisRight().setDrawLabels(false);
//
//        xValues = Arrays.asList("Nadun","Kamal", "Jhon", "Jerry");
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
//        xAxis.setLabelCount(4);
//        xAxis.setGranularity(1f);
//
//        YAxis yAxis = lineChart.getAxisLeft();
//        yAxis.setAxisMinimum(0f);
//        yAxis.setAxisMaximum(100f);
//        yAxis.setAxisLineWidth(2f);
//        yAxis.setAxisLineColor(Color.BLACK);
//        yAxis.setLabelCount(10);
//
//        List<Entry> entries1 = new ArrayList<>();
//        entries1.add(new Entry(0, 10f));
//        entries1.add(new Entry(1, 10f));
//        entries1.add(new Entry(2, 65f));
//        entries1.add(new Entry(3, 45f));
//
//        List<Entry> entries2 = new ArrayList<>();
//        entries2.add(new Entry(0, 5f));
//        entries2.add(new Entry(1, 15f));
//        entries2.add(new Entry(2, 25f));
//        entries2.add(new Entry(3, 30f));
//
//        LineDataSet dataSet1 = new LineDataSet(entries1, "Math");
//        dataSet1.setColor(Color.BLUE);
//
//        LineDataSet dataSet2 = new LineDataSet(entries2, "Science");
//        dataSet2.setColor(Color.RED);
//
//        LineData lineData = new LineData(dataSet1, dataSet2);
//
//        lineChart.setData(lineData);
//
//        lineChart.invalidate();
    }
}