package edu.northeastern.group_project_group_8;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private ArrayList<String> accountNames;
    private Context context;
    TextView totalAmountTextView;
    TextView accountHoldings;
    StringBuilder resultBuilder;
    String resultString;
    String accountName;
    double totalAsset;
    HashMap<String, ArrayList<Price>> priceSumsByDateByAccount;

    public AccountAdapter(Context context, ArrayList<String> accountNames, HashMap<String, ArrayList<Price>> priceSumsByDateByAccount) {
        this.context = context;
        this.accountNames = accountNames;
        this.priceSumsByDateByAccount = priceSumsByDateByAccount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.account_card_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        accountName = accountNames.get(position);
        holder.accountNameTextView.setText(accountName);
        holder.priceSumsByDate = priceSumsByDateByAccount.get(accountName);
        if (holder.priceSumsByDate!= null && !holder.priceSumsByDate.isEmpty()) {
            totalAsset = holder.priceSumsByDate.get(holder.priceSumsByDate.size()-1).price;
            holder.totalAmountTextView.setText("$" + totalAsset);
        } else {
            holder.totalAmountTextView.setText("$0.00");
        }

        Description description = new Description();
        description.setText("Portfolio Value");
        description.setPosition(250f, 150f);
        holder.lineChart.setDescription(description);
        holder.lineChart.getAxisRight().setDrawLabels(false);
        ArrayList<String> portfolioDates = new ArrayList<>();
        for (Price date: holder.priceSumsByDate) {
            portfolioDates.add(date.date.toString());
        }
//        xValues = Arrays.asList(String.valueOf(portfolioDates));
        holder.xValues = portfolioDates;
        ArrayList<String> newXValues = new ArrayList<>();
        int xSize = holder.xValues.size();
        Log.d("", "xSize: " + xSize);
        int interval = xSize / 4;
        for (int i = 0; i < xSize; i++) {
            if ((i+1)%interval == 0) {
                newXValues.add(holder.xValues.get(i));
            }
        }
        Log.d("", "newXValues Size: " + newXValues.size());

        XAxis xAxis = holder.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(newXValues));
        xAxis.setLabelCount(newXValues.size());
        xAxis.setGranularity(1f);
//        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = holder.lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
//        yAxis.setAxisMaximum(500f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);
        yAxis.setDrawGridLines(false);

        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < holder.priceSumsByDate.size(); i++) {
            Price currentPrice = holder.priceSumsByDate.get(i);
            entries1.add(new Entry(i, currentPrice.price.floatValue()));
        }

        LineDataSet dataSet1 = new LineDataSet(entries1, "");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setDrawCircles(false);
//        dataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        dataSet1.setCubicIntensity(1f);

        LineData lineData = new LineData(dataSet1);
        lineData.setDrawValues(false);

        holder.lineChart.setData(lineData);
        holder.lineChart.setAutoScaleMinMaxEnabled(true);
        holder.lineChart.animateY(1000);

        holder.lineChart.invalidate();
    }

    @Override
    public int getItemCount() {
        return accountNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView accountNameTextView;
        TextView totalAmountTextView;
        private static LineChart lineChart;
        ArrayList<Price> priceSumsByDate;
        private List<String> xValues;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            accountNameTextView = itemView.findViewById(R.id.accountNameTextView);
            totalAmountTextView = itemView.findViewById(R.id.totalAmountTextView);
            lineChart = itemView.findViewById(R.id.lineChart);
        }
    }
}

