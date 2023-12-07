package edu.northeastern.group_project_group_8;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountHoldingsAdapter extends RecyclerView.Adapter<AccountHoldingsAdapter.ViewHolder> {

    private List<String> assetDetails;

    // Constructor to initialize data
    public AccountHoldingsAdapter(List<String> assetDetails) {
        this.assetDetails = assetDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_holding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views in each ViewHolder
        holder.assetDetailsTextView.setText(assetDetails.get(position));
    }

    @Override
    public int getItemCount() {
        return assetDetails.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView assetDetailsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            assetDetailsTextView = itemView.findViewById(R.id.assetDetailsTextView);
        }
    }
}

