package com.taipei.ttbootcamp.resultView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taipei.ttbootcamp.R;
import com.taipei.ttbootcamp.Utils.Utils;
import com.taipei.ttbootcamp.data.TripData;

import org.jetbrains.annotations.NotNull;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> implements View.OnClickListener {

    private TripData mTripData;
    private OnItemClickListener mOnItemClickListener = null;

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {
        holder.destTextView.setText(mTripData.getWayPoints().get(position).getFirstName());
//        holder.timeTextView.setText(Utils.secondToHourMinute(mTripData.getFuzzySearchResultTravelTimes().get(position)));
        holder.itemView.setTag(position);
    }

    public ResultAdapter(TripData tripData) {
        this.mTripData = tripData;
    }

    @Override
    public int getItemCount() {
        return mTripData.getWayPoints().size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView destTextView, timeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            destTextView = itemView.findViewById(R.id.tv_dest_name);
            timeTextView = itemView.findViewById(R.id.tv_arrival_time);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}