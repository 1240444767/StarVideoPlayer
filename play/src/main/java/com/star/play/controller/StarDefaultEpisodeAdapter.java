package com.star.play.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.star.play.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Default episode adapter used by {@link StarEpisodeView}.
 * Package-private — users can subclass to extend behavior instead of replacing entirely.
 */
class StarDefaultEpisodeAdapter extends RecyclerView.Adapter<StarDefaultEpisodeAdapter.VH> {

    interface OnEpisodeSelectListener {
        void onEpisodeSelect(int index, String title);
    }

    private final List<String> mData = new ArrayList<>();
    private int mCurrentIndex = -1;
    private OnEpisodeSelectListener mListener;

    void setData(List<String> data, int currentIndex) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        mCurrentIndex = currentIndex;
        notifyDataSetChanged();
    }

    void setCurrentIndex(int index) {
        int old = mCurrentIndex;
        mCurrentIndex = index;
        if (old >= 0 && old < mData.size()) notifyItemChanged(old);
        if (index >= 0 && index < mData.size()) notifyItemChanged(index);
    }

    int getCurrentIndex() {
        return mCurrentIndex;
    }

    List<String> getData() {
        return mData;
    }

    void setOnEpisodeSelectListener(OnEpisodeSelectListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_episode, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (position < 0 || position >= mData.size()) return;
        holder.titleView.setText(mData.get(position));
        holder.titleView.setSelected(position == mCurrentIndex);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= mData.size()) return;
            setCurrentIndex(pos);
            if (mListener != null) {
                mListener.onEpisodeSelect(pos, mData.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titleView;

        VH(@NonNull View v) {
            super(v);
            titleView = v.findViewById(R.id.episode_title);
        }
    }
}
