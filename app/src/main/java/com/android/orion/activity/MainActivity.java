package com.android.orion.activity;

import android.os.Bundle;

import com.android.orion.R;
import com.android.orion.adapter.MarquessViewAdapter;
import com.android.orion.adapter.StockAdapter;
import com.android.orion.adapter.TabAdapter;
import com.android.orion.bean.MessageBean;
import com.android.orion.bean.StockBean;
import com.android.orion.utility.AssetsUtils;
import com.android.orion.view.CustomizeMarqueeView;
import com.android.orion.view.CustomizeScrollView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 欢迎关注公众号：码农专栏
 * 每日分享Android最新技术，Android中高级进阶推文
 */
public class MainActivity extends AppCompatActivity implements StockAdapter.OnTabScrollViewListener {
    /**
     * Tab栏ScrollView
     */
    private CustomizeScrollView headHorizontalScrollView;
    /**
     * Tab栏RecyclerView
     */
    private RecyclerView mHeadRecyclerView;
    /**
     * 列表RecyclerView
     */
    private RecyclerView mContentRecyclerView;
    /**
     * 跑马灯View
     */
    private CustomizeMarqueeView mMarqueeView;
    /**
     * 跑马灯Adapter
     */
    private MarquessViewAdapter marquessViewAdapter;
    /**
     * Tab栏Adapter
     */
    private TabAdapter mTabAdapter;
    /**
     * 列表Adapter
     */
    private StockAdapter mStockAdapter;
    /**
     * Tab栏标题
     */
    String values[] = {"最新", "涨幅", "涨跌", "换手", "成交额", "量比", "振幅"};
    /**
     * RecyclerView数据集合
     */
    private List<StockBean> stockBeans = new ArrayList<>();
    /**
     * 跑马灯数据集合
     */
    private List<MessageBean> messageBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeadRecyclerView = findViewById(R.id.headRecyclerView);
        mContentRecyclerView = findViewById(R.id.contentRecyclerView);
        headHorizontalScrollView = findViewById(R.id.headScrollView);
        mMarqueeView = findViewById(R.id.marqueeView);

        // TODO:Tab栏RecycleView
        // 设置RecyclerView水平显示
        mHeadRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mTabAdapter = new TabAdapter(this);
        // 设置ListView禁止滑动，这样使得ScrollView滑动更流畅
        mHeadRecyclerView.setNestedScrollingEnabled(false);
        mHeadRecyclerView.setAdapter(mTabAdapter);
        initTabData();

        // TODO:文中列表RecyclerView
        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mStockAdapter = new StockAdapter(this);
        mContentRecyclerView.setAdapter(mStockAdapter);
        mStockAdapter.setOnTabScrollViewListener(this);
        initStockData();

        // TODO：跑马灯
        marquessViewAdapter = new MarquessViewAdapter(this);
        mMarqueeView.setItemCount(2);
        mMarqueeView.setSingleLine(false);
        mMarqueeView.setAdapter(marquessViewAdapter);
        marquessViewAdapter.setMessageBeans(messageBeans);
        initMarquessData();

        initListener();
    }

    /**
     * 初始化跑马灯数据
     */
    private void initMarquessData() {
        List<MessageBean> list = new Gson().fromJson(AssetsUtils.getJson("message.json", getApplication()),
                new TypeToken<List<MessageBean>>() {
                }.getType());
        messageBeans.addAll(list);
        marquessViewAdapter.notifyDataChanged();
    }

    private void initListener() {
        /**
         * 第三步：Tab栏HorizontalScrollView水平滚动时，遍历所有RecyclerView列表，并使其跟随滚动
         */
        headHorizontalScrollView.setViewListener(new CustomizeScrollView.OnScrollViewListener() {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt) {
                List<StockAdapter.ViewHolder> viewHolders = mStockAdapter.getRecyclerViewHolder();
                for (StockAdapter.ViewHolder viewHolder : viewHolders) {
                    viewHolder.mStockScrollView.scrollTo(l, 0);
                }
            }
        });

        /**
         * 第四步：RecyclerView垂直滑动时，遍历更新所有item中HorizontalScrollView的滚动位置，否则会出现item位置未发生变化状态
         */
        mContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                List<StockAdapter.ViewHolder> viewHolders = mStockAdapter.getRecyclerViewHolder();
                for (StockAdapter.ViewHolder viewHolder : viewHolders) {
                    viewHolder.mStockScrollView.scrollTo(mStockAdapter.getOffestX(), 0);
                }
            }
        });
    }


    /**
     * 初始化Tab数据
     */
    void initTabData() {
        mTabAdapter.setTabData(Arrays.asList(values));
    }

    /**
     * 初始化列表数据
     */
    void initStockData() {
        String json = AssetsUtils.getJson("stock.json", getApplication());
        stockBeans = new Gson().fromJson(json, new TypeToken<List<StockBean>>() {
        }.getType());
        mStockAdapter.setStockBeans(stockBeans);
    }

    @Override
    public void scrollTo(int l, int t) {
        if (headHorizontalScrollView != null) {
            headHorizontalScrollView.scrollTo(l, 0);
        }
    }
}
