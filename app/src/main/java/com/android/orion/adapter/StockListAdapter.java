package com.android.orion.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.orion.R;
import com.android.orion.database.Stock;

import java.util.ArrayList;
import java.util.List;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {

    private Context mContext;
    private List<Stock> mStockList;
    private OnStockClickListener mListener;

    public interface OnStockClickListener {
        void onStockClick(Stock stock);
        void onFavoriteClick(Stock stock);
        void onDeleteClick(Stock stock);
    }

    public StockListAdapter(Context context, OnStockClickListener listener) {
        mContext = context;
        mListener = listener;
        mStockList = new ArrayList<>();
    }

    public void setStockList(List<Stock> stockList) {
        if (stockList == null) {
            mStockList = new ArrayList<>();
        } else {
            mStockList = new ArrayList<>(stockList);
        }
        notifyDataSetChanged();
    }

    public void updateStockList(List<Stock> stockList) {
        setStockList(stockList);
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_stock_list_item, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        if (position < 0 || position >= mStockList.size()) {
            return;
        }

        Stock stock = mStockList.get(position);
        holder.bind(stock);
    }

    @Override
    public int getItemCount() {
        return mStockList != null ? mStockList.size() : 0;
    }

    public Stock getItem(int position) {
        if (position >= 0 && position < mStockList.size()) {
            return mStockList.get(position);
        }
        return null;
    }

    public class StockViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewName;
        private TextView mTextViewCode;
        private TextView mTextViewPrice;
        private TextView mTextViewHold;
        private ImageView mImageViewFavorite;
        private ImageView mImageViewDelete;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextViewName = itemView.findViewById(R.id.name);
            mTextViewCode = itemView.findViewById(R.id.code);
            mTextViewPrice = itemView.findViewById(R.id.price);
            mTextViewHold = itemView.findViewById(R.id.hold);
            mImageViewFavorite = itemView.findViewById(R.id.favorite);
            mImageViewDelete = itemView.findViewById(R.id.delete);

            setupClickListeners();
        }

        public void bind(Stock stock) {
            mTextViewName.setText(stock.getName());
            mTextViewCode.setText(stock.getCode());
            mTextViewPrice.setText(String.valueOf(stock.getPrice()));
            mTextViewHold.setText(String.valueOf(stock.getHold()));

            // 设置收藏图标
            if (stock.hasFlag(Stock.FLAG_FAVORITE)) {
                mImageViewFavorite.setImageResource(R.drawable.ic_favorite);
            } else {
                mImageViewFavorite.setImageResource(R.drawable.ic_none_favorite);
            }

            // 设置删除图标
            if (stock.getHold() == 0) {
                mImageViewDelete.setImageResource(R.drawable.ic_delete);
                mImageViewDelete.setEnabled(true);
                mImageViewDelete.setAlpha(1.0f);
            } else {
                mImageViewDelete.setImageResource(R.drawable.ic_undeletable);
                mImageViewDelete.setEnabled(false);
                mImageViewDelete.setAlpha(0.5f);
            }

            // 设置标签
            mImageViewFavorite.setTag(stock);
            mImageViewDelete.setTag(stock);
            itemView.setTag(stock);
        }

        private void setupClickListeners() {
            // 整个item的点击事件
            itemView.setOnClickListener(v -> {
                Stock stock = (Stock) v.getTag();
                if (mListener != null && stock != null) {
                    mListener.onStockClick(stock);
                }
            });

            // 收藏按钮点击事件
            mImageViewFavorite.setOnClickListener(v -> {
                Stock stock = (Stock) v.getTag();
                if (mListener != null && stock != null) {
                    mListener.onFavoriteClick(stock);
                }
            });

            // 删除按钮点击事件
            mImageViewDelete.setOnClickListener(v -> {
                Stock stock = (Stock) v.getTag();
                if (mListener != null && stock != null && stock.getHold() == 0) {
                    mListener.onDeleteClick(stock);
                }
            });
        }
    }
}