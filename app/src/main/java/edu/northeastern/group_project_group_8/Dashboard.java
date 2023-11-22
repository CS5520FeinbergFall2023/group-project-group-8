package edu.northeastern.group_project_group_8;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
            sleep(3000);
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
        lineChart = findViewById(R.id.lineChart);
        Description description = new Description();
        description.setText("Portfolio Value");
        description.setPosition(250f, 150f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);
        ArrayList<LocalDate> portfolioDates = new ArrayList<>();
        for (Price date: portfolioData.priceSumsByDate) {
            portfolioDates.add(date.date);
        }
        xValues = Arrays.asList(String.valueOf(portfolioDates));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(portfolioDates.size());
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(500f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < portfolioData.priceSumsByDate.size(); i++) {
            Price currentPrice = portfolioData.priceSumsByDate.get(i);
            entries1.add(new Entry(i, currentPrice.price.floatValue()));
        }

        LineDataSet dataSet1 = new LineDataSet(entries1, "Math");
        dataSet1.setColor(Color.BLUE);

        LineData lineData = new LineData(dataSet1);

        lineChart.setData(lineData);

        lineChart.invalidate();
    }
}