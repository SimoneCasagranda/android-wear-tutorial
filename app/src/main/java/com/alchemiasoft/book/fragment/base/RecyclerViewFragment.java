/*
 * Copyright 2014 Simone Casagranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alchemiasoft.book.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.widget.SmartSwipeRefreshLayout;

/**
 * Base fragment that works in a similar way to ListFragment but with RecyclerView.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class RecyclerViewFragment extends Fragment {

    private SmartSwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;

    private TextView mEmptyTextView;
    private View mProgressContainer;
    private View mContentContainer;
    private boolean mContentShown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.recycler_content, container, false);
        mSwipeRefreshLayout = (SmartSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mEmptyTextView = (TextView) view.findViewById(R.id.empty_text);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        mContentContainer = view.findViewById(R.id.contentContainer);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentShown(false, false);
        mEmptyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEmptyTextClicked();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RecyclerViewFragment.this.onRefresh();
            }
        });
    }

    @Override
    public void onDestroyView() {
        mSwipeRefreshLayout = null;
        mRecyclerView = null;
        mContentShown = false;
        mProgressContainer = mContentContainer = null;
        mEmptyTextView = null;
        super.onDestroyView();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setRecyclerAdapter(RecyclerView.Adapter<?> recyclerAdapter) {
        mAdapter = recyclerAdapter;
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setEmptyText(int resId) {
        mEmptyTextView.setText(resId);
    }

    public void setEmptyText(CharSequence text) {
        mEmptyTextView.setText(text);
    }

    public void setContentShown(boolean shown) {
        setContentShown(shown, true);
    }

    public void setContentShownNoAnimation(boolean shown) {
        setContentShown(shown, false);
    }

    public void onRefresh() {

    }

    public void onEmptyTextClicked() {

    }

    public void startRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public void stopRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setContentShown(boolean shown, boolean animate) {
        if (mContentShown == shown) {
            mEmptyTextView.setVisibility(mAdapter == null || mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            return;
        }
        mContentShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
            boolean noContent = mAdapter == null || mAdapter.getItemCount() == 0;
            mEmptyTextView.setVisibility(noContent ? View.VISIBLE : View.GONE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
        }
    }

    public void setScrollInterceptor(SmartSwipeRefreshLayout.ScrollInterceptor interceptor) {
        mSwipeRefreshLayout.setScrollInterceptor(interceptor);
    }
}
