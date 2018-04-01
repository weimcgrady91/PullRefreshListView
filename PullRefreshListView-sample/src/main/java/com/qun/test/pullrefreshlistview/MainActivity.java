package com.qun.test.pullrefreshlistview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.qun.lib.pullrefreshlistview.PullRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullRefreshListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mData;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mData.addAll(0, getNewData());
                    mListView.onRefreshCompleted();
                    mAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    mData.addAll(getMoreData());
                    if (newIndex == 12) {
                        mListView.onLoadMoreCompleted(true);
                    } else {

                        mListView.onLoadMoreCompleted(false);
                    }
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mData = getData();
        mListView = findViewById(R.id.lv);
        mListView.setOnItemClickListener(new PullRefreshListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "position=" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        mListView.setAdapter(mAdapter);
        mListView.setOnFetchDataListener(new PullRefreshListView.OnFetchDataListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }

            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        mListView.setOpenPullRefresh(true);
        mListView.setOpenLoadMore(false);
    }

    private void loadMoreData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    mHandler.sendEmptyMessage(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    mHandler.sendEmptyMessage(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<String> getNewData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("这是刷新的第" + i + "个条目");
        }
        return data;
    }

    int newIndex;

    private List<String> getMoreData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            data.add("这是加载的新数据" + newIndex++);
        }
        return data;
    }

    private List<String> getData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("这是第" + i + "个条目");
        }
        return data;
    }
}
