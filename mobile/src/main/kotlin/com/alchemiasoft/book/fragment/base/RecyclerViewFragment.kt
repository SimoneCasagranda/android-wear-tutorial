/*
 * Copyright 2015 Simone Casagranda.
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


package com.alchemiasoft.book.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.alchemiasoft.book.R
import com.alchemiasoft.book.widget.SmartSwipeRefreshLayout

/**
 * Base fragment that works in a similar way to ListFragment but with RecyclerView.
 *
 * Created by Simone Casagranda on 20/12/14.
 */
open class RecyclerViewFragment : Fragment() {

    private var mSwipeRefreshLayout: SmartSwipeRefreshLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mEmptyTextView: TextView? = null
    private var mProgressContainer: View? = null
    private var mContentContainer: View? = null
    private var mContentShown = false

    /**
     * The RecyclerView; only available between onCreateView and onDestroyView.
     */
    val recyclerView: RecyclerView
        get() = checkNotNull(mRecyclerView) { "RecyclerView is not available outside the view lifecycle." }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.recycler_content, container, false)
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mEmptyTextView = view.findViewById(R.id.empty_text)
        mProgressContainer = view.findViewById(R.id.progressContainer)
        mContentContainer = view.findViewById(R.id.contentContainer)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentShown(shown = false, animate = false)
        mEmptyTextView?.setOnClickListener { onEmptyTextClicked() }
        mSwipeRefreshLayout?.setOnRefreshListener { onRefresh() }
    }

    override fun onDestroyView() {
        mSwipeRefreshLayout = null
        mRecyclerView = null
        mContentShown = false
        mProgressContainer = null
        mContentContainer = null
        mEmptyTextView = null
        super.onDestroyView()
    }

    fun setRecyclerAdapter(recyclerAdapter: RecyclerView.Adapter<*>?) {
        mAdapter = recyclerAdapter
        mRecyclerView?.adapter = recyclerAdapter
    }

    fun setEmptyText(@StringRes resId: Int) {
        mEmptyTextView?.setText(resId)
    }

    fun setEmptyText(text: CharSequence?) {
        mEmptyTextView?.text = text
    }

    fun setContentShown(shown: Boolean) = setContentShown(shown, animate = true)

    fun setContentShownNoAnimation(shown: Boolean) = setContentShown(shown, animate = false)

    open fun onRefresh() {
    }

    open fun onEmptyTextClicked() {
    }

    fun startRefresh() {
        mSwipeRefreshLayout?.isRefreshing = true
    }

    fun stopRefresh() {
        mSwipeRefreshLayout?.isRefreshing = false
    }

    fun setScrollInterceptor(interceptor: SmartSwipeRefreshLayout.ScrollInterceptor?) {
        mSwipeRefreshLayout?.scrollInterceptor = interceptor
    }

    private fun setContentShown(shown: Boolean, animate: Boolean) {
        val progress = mProgressContainer ?: return
        val content = mContentContainer ?: return
        val noContent = (mAdapter?.itemCount ?: 0) == 0
        if (mContentShown == shown) {
            mEmptyTextView?.visibility = if (noContent) View.VISIBLE else View.GONE
            return
        }
        mContentShown = shown
        if (animate) {
            progress.startAnimation(AnimationUtils.loadAnimation(activity, if (shown) android.R.anim.fade_out else android.R.anim.fade_in))
            content.startAnimation(AnimationUtils.loadAnimation(activity, if (shown) android.R.anim.fade_in else android.R.anim.fade_out))
        } else {
            progress.clearAnimation()
            content.clearAnimation()
        }
        progress.visibility = if (shown) View.GONE else View.VISIBLE
        content.visibility = if (shown) View.VISIBLE else View.GONE
        if (shown) {
            mEmptyTextView?.visibility = if (noContent) View.VISIBLE else View.GONE
        }
    }
}
