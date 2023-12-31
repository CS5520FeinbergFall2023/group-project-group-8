package edu.northeastern.group_project_group_8;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
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
import java.util.Locale;
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
    String platform;
    String performance;
    double totalAsset;
    String loggedInUser;
    HashMap<String, ArrayList<Price>> priceSumsByDateByAccount;
    HashMap<String, String> platformByAccountMap;

    public AccountAdapter(Context context, ArrayList<String> accountNames, HashMap<String, ArrayList<Price>> priceSumsByDateByAccount, HashMap<String, String> platformByAccountMap, String loggedInUser) {
        this.context = context;
        this.accountNames = accountNames;
        this.priceSumsByDateByAccount = priceSumsByDateByAccount;
        this.platformByAccountMap = platformByAccountMap;
        this.loggedInUser = loggedInUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.account_card_item, parent, false);

        // Attach a click listener to the itemView
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(); // Retrieve the position of the clicked item
                launchAccountDetailsPage(position);
            }
        });

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        accountName = accountNames.get(position);
        platform = platformByAccountMap.get(accountName);
        holder.accountNameTextView.setText(accountName);
        holder.institutionTextView.setText(platform);
        holder.priceSumsByDate = priceSumsByDateByAccount.get(accountName);
        if (holder.priceSumsByDate!= null && !holder.priceSumsByDate.isEmpty()) {
            totalAsset = holder.priceSumsByDate.get(holder.priceSumsByDate.size()-1).price;
            String totalAssetString = String.format(Locale.getDefault(), "%.2f", totalAsset);
            holder.totalAmountTextView.setText("$" + totalAssetString);
            double changeInValue = 100 * (holder.priceSumsByDate.get(holder.priceSumsByDate.size()-1).price - holder.priceSumsByDate.get(0).price) / holder.priceSumsByDate.get(0).price;
            String changeInValueString = String.format(Locale.getDefault(), "%.2f", changeInValue);
            if (changeInValue > 0) {
//                holder.performanceTextView.setTextColor(Color.GREEN);
                holder.performanceTextView.setText("+" + changeInValueString + "%");
                holder.performanceTextView.setBackgroundColor(Color.parseColor("#00FF00"));
            } else if (changeInValue < 0) {
//                holder.performanceTextView.setTextColor(Color.RED);
                holder.performanceTextView.setText(changeInValueString + "%");
                holder.performanceTextView.setBackgroundColor(Color.parseColor("#FF0000"));
            } else {
//                holder.performanceTextView.setTextColor(Color.BLACK);
                holder.performanceTextView.setText(changeInValueString + "%");
                holder.performanceTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            holder.totalAmountTextView.setText("$0.00");
        }

        if (holder.priceSumsByDate != null && holder.priceSumsByDate.size() > 0) {
            Description description = new Description();
            description.setText("");
            holder.lineChart.setDescription(description);
            holder.lineChart.getAxisRight().setDrawLabels(false);
            ArrayList<String> portfolioDates = new ArrayList<>();
            for (Price date: holder.priceSumsByDate) {
                portfolioDates.add(date.date.toString());
            }
            holder.xValues = portfolioDates;

            XAxis xAxis = holder.lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(holder.xValues));
            xAxis.setLabelCount(4);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);

            YAxis yAxis = holder.lineChart.getAxisLeft();
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

            Legend legend = holder.lineChart.getLegend();
            legend.setEnabled(false);

            LineData lineData = new LineData(dataSet1);
            lineData.setDrawValues(false);
            holder.lineChart.setTouchEnabled(false);

            holder.lineChart.setData(lineData);
            holder.lineChart.setAutoScaleMinMaxEnabled(true);
            holder.lineChart.animateY(1000);

            holder.lineChart.invalidate();
        }
    }

    @Override
    public int getItemCount() {
        return accountNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView accountNameTextView;
        TextView totalAmountTextView;
        TextView institutionTextView;
        TextView performanceTextView;
        private static LineChart lineChart;
        ArrayList<Price> priceSumsByDate;
        private List<String> xValues;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            accountNameTextView = itemView.findViewById(R.id.accountNameTextView);
            totalAmountTextView = itemView.findViewById(R.id.totalAmountTextView);
            institutionTextView = itemView.findViewById(R.id.platformTextView);
            performanceTextView = itemView.findViewById(R.id.performanceTextView);
            lineChart = itemView.findViewById(R.id.lineChart);
        }
    }

    private void launchAccountDetailsPage(int position) {
        String clickedAccountName = accountNames.get(position);
        String clickedPlatform = platformByAccountMap.get(clickedAccountName);

        if (priceSumsByDateByAccount.get(clickedAccountName) == null || priceSumsByDateByAccount.get(clickedAccountName).size() == 0) {
            Toast.makeText(context, "This account does not have any holdings.", Toast.LENGTH_SHORT).show();
        } else {
            double clickedTotalAsset = priceSumsByDateByAccount.get(clickedAccountName).get(priceSumsByDateByAccount.get(clickedAccountName).size() - 1).price;

            Intent intent = new Intent(context, AccountDetailsPage.class);
            intent.putExtra("accountName", clickedAccountName);
            intent.putExtra("platform", clickedPlatform);
            intent.putExtra("totalAsset", clickedTotalAsset);
            intent.putExtra("loggedInUserName", loggedInUser);
            context.startActivity(intent);
        }
    }
}

