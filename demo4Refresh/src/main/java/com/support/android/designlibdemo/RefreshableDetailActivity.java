/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.android.designlibdemo;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollViewExtend;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.PullToRefreshContainer;

import java.util.Random;

public class RefreshableDetailActivity extends AppCompatActivity {

  private SimpleStringRecyclerViewAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_refreshable_image);
//    loadBackdrop();
    final PullToRefreshContainer refreshContainer
        = findViewById(R.id.main_content);
    final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        refreshContainer.endRefresh();
      }
    });

    RecyclerView rv = findViewById(R.id.recyclerview);
    refreshContainer.takeOverScrollBehavior(rv);
    setupRecyclerView(rv);
    loadBackdrop();
    refreshContainer.addOnScrollChangeListener(new NestedScrollViewExtend.OnScrollChangeListener() {
      @Override
      public void onScrollChange(NestedScrollViewExtend v, int scrollX, int scrollY, int
          oldScrollX, int oldScrollY) {
        View view = findViewById(R.id.imageWrapper);
        ImageView imageView = findViewById(R.id.backdrop);
        float progress = scrollY * 1f / view.getHeight();
        float scale = Math.max(1 - progress, 0);
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
        imageView.setPivotY(view.getHeight());
      }
    });
    rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        final View loadingMore = findViewById(R.id.loadingMore);
        int offset = recyclerView.computeVerticalScrollRange() -
            recyclerView.computeVerticalScrollExtent() -
            recyclerView.computeVerticalScrollOffset();
        loadingMore.setScrollY(-offset);
        if (!recyclerView.canScrollVertically(1)) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              final int beforeUpdateCount = adapter.mValues.size();
              for (int i = 0; i < 10; i++) {
                adapter.mValues.add(String.valueOf(new Random().nextLong()));
              }
              loadingMore.post(new Runnable() {
                @Override
                public void run() {
                  adapter.notifyItemRangeInserted(beforeUpdateCount, 10);
                }
              });
            }
          }).start();
        }
      }
    });
  }

  private void loadBackdrop() {
    final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
    Glide.with(this).load(Cheeses.getRandomCheeseDrawable()).centerCrop().into(imageView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sample_actions, menu);
    return true;
  }

  private void setupRecyclerView(RecyclerView recyclerView) {
    LinearLayoutManager layout = new LinearLayoutManager(recyclerView.getContext());
    layout.setAutoMeasureEnabled(false);
    recyclerView.setLayoutManager(layout);
    adapter = new SimpleStringRecyclerViewAdapter(this,
        DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 1)) {
      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setOnClickListener(null);
      }
    };
    recyclerView.setAdapter(adapter);
  }
}
