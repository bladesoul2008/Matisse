/*
 * Copyright 2017 Zhihu Inc.
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
package com.zhihu.matisse.internal.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Album;
import com.zhihu.matisse.internal.utils.Platform;

public class AlbumsSpinner {

    private static final int MAX_SHOWN_COUNT = 6;
    private CursorAdapter mAdapter;
    private TextView mSelected;
    private ListPopupWindow mListPopupWindow;
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener;
    private Activity activity;

    public AlbumsSpinner(@NonNull Context context, final Toolbar toolbar) {
        activity = (Activity) context;
        mListPopupWindow = new ListPopupWindow(context, null, R.attr.listPopupWindowStyle);
        mListPopupWindow.setModal(true);
        final float density = context.getResources().getDisplayMetrics().density;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mListPopupWindow.setContentWidth(width);
        mListPopupWindow.setVerticalOffset(0);
//        mListPopupWindow.setHorizontalOffset((int) (16 * density));

        mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(activity,1f);
            }
        });


        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumsSpinner.this.onItemSelected(parent.getContext(), position);
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(parent, view, position, id);
                }
            }
        });
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public void setSelection(Context context, int position) {
        mListPopupWindow.setSelection(position);
        onItemSelected(context, position);
    }

    private void onItemSelected(Context context, int position) {
        mListPopupWindow.dismiss();
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        Album album = Album.valueOf(cursor);
        String displayName = album.getDisplayName(context);
        if (mSelected.getVisibility() == View.VISIBLE) {
            mSelected.setText(displayName);
        } else {
            if (Platform.hasICS()) {
                mSelected.setAlpha(0.0f);
                mSelected.setVisibility(View.VISIBLE);
                mSelected.setText(displayName);
                mSelected.animate().alpha(1.0f).setDuration(context.getResources().getInteger(
                        android.R.integer.config_longAnimTime)).start();
            } else {
                mSelected.setVisibility(View.VISIBLE);
                mSelected.setText(displayName);
            }

        }
    }

    public void setAdapter(CursorAdapter adapter) {
        mListPopupWindow.setAdapter(adapter);
        mAdapter = adapter;
    }

    public void setSelectedTextView(TextView textView) {
        mSelected = textView;
        // tint dropdown arrow icon
        Drawable[] drawables = mSelected.getCompoundDrawables();
        Drawable right = drawables[2];
        TypedArray ta = mSelected.getContext().getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        right.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        mSelected.setVisibility(View.GONE);
        mSelected.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int itemHeight = v.getResources().getDimensionPixelSize(R.dimen.album_item_height);
                mListPopupWindow.setHeight(
                        mAdapter.getCount() > MAX_SHOWN_COUNT ? itemHeight * MAX_SHOWN_COUNT
                                : itemHeight * mAdapter.getCount());
                mListPopupWindow.show();
                setBackgroundAlpha(activity,0.3f);
            }
        });
        mSelected.setOnTouchListener(mListPopupWindow.createDragToOpenListener(mSelected));
    }

    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    public void setPopupAnchorView(View view) {
        mListPopupWindow.setAnchorView(view);
    }

}
