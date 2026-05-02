package com.star.play.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import com.star.play.controller.PlayerController;
import com.star.play.controller.IControlComponent;
import com.star.play.controller.PlayerConstants;

public class StarEpisodeView extends FrameLayout implements IControlComponent {

    private PlayerController mPlayerController;

    private LinearLayout mPanelView;
    private View mDimView;
    private FrameLayout mContentContainer;
    private RecyclerView mRecyclerView;
    private MaterialButton mCloseButton;
    private TextView mTitleView;

    private RecyclerView.Adapter<?> mAdapter;
    private StarDefaultEpisodeAdapter mDefaultAdapter;
    private boolean mIsShowing = false;
    private boolean mUseCustomAdapter = false;
    private boolean mUseCustomContent = false;

    public interface OnEpisodeSelectListener {
        void onEpisodeSelect(int index, String title);
    }
    private OnEpisodeSelectListener mOnEpisodeSelectListener;

    public void setOnEpisodeSelectListener(OnEpisodeSelectListener l) {
        mOnEpisodeSelectListener = l;
    }

    public StarEpisodeView(@NonNull Context context) {
        this(context, null);
    }

    public StarEpisodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarEpisodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_episode_view, this, true);

        mDimView           = findViewById(R.id.dim);
        mPanelView         = findViewById(R.id.panel);
        mContentContainer  = findViewById(R.id.content_container);
        mRecyclerView      = findViewById(R.id.recycler_view);
        mCloseButton       = findViewById(R.id.btn_close);
        mTitleView         = findViewById(R.id.tv_title);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDefaultAdapter = new StarDefaultEpisodeAdapter();
        mDefaultAdapter.setOnEpisodeSelectListener((index, title) -> {
            if (mOnEpisodeSelectListener != null) {
                mOnEpisodeSelectListener.onEpisodeSelect(index, title);
            }
            hide();
        });
        mAdapter = mDefaultAdapter;
        mRecyclerView.setAdapter(mAdapter);

        mDimView.setOnClickListener(v -> hide());
        mCloseButton.setOnClickListener(v -> hide());
        mPanelView.setOnClickListener(v -> { });
    }

    public void setEpisodes(java.util.List<String> episodes, int currentIndex) {
        if (mUseCustomAdapter) return;
        mDefaultAdapter.setData(episodes, currentIndex);
    }

    public void setCurrentIndex(int index) {
        if (mUseCustomAdapter) return;
        mDefaultAdapter.setCurrentIndex(index);
        mRecyclerView.scrollToPosition(Math.max(0, index));
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        mUseCustomAdapter = true;
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * 设置面板标题
     */
    public void setPanelTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * 设置面板标题文字颜色
     */
    public void setPanelTitleColor(int color) {
        if (mTitleView != null) {
            mTitleView.setTextColor(color);
        }
    }

    /**
     * 设置标题栏可见性
     */
    public void setTitleBarVisibility(int visibility) {
        View titleBar = mTitleView != null ? (View) mTitleView.getParent() : null;
        if (titleBar != null) {
            titleBar.setVisibility(visibility);
        }
    }

    /**
     * 设置关闭按钮可见性
     */
    public void setCloseButtonVisibility(int visibility) {
        if (mCloseButton != null) {
            mCloseButton.setVisibility(visibility);
        }
    }

    /**
     * 替换内容区域为自定义 View
     * 调用后默认的 RecyclerView 会被移除
     */
    public void setCustomContentView(View view) {
        if (mContentContainer == null || view == null) return;
        mUseCustomContent = true;
        mContentContainer.removeAllViews();
        mContentContainer.addView(view);
    }

    /**
     * 通过布局资源 ID 替换内容区域
     */
    public void setCustomContentView(int layoutResId) {
        if (mContentContainer == null) return;
        mUseCustomContent = true;
        mContentContainer.removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(layoutResId, mContentContainer, false);
        mContentContainer.addView(view);
    }

    /**
     * 恢复默认的 RecyclerView 内容
     */
    public void restoreDefaultContent() {
        if (mContentContainer == null) return;
        mUseCustomContent = false;
        mContentContainer.removeAllViews();
        mContentContainer.addView(mRecyclerView);
    }

    /**
     * 获取内容容器，可用于动态添加子 View
     */
    public FrameLayout getContentContainer() {
        return mContentContainer;
    }

    public void show() {
        if (mIsShowing) return;
        mIsShowing = true;
        setVisibility(VISIBLE);
        bringToFront();
        TranslateAnimation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        slideIn.setDuration(250);
        mPanelView.startAnimation(slideIn);

        mDimView.setAlpha(0f);
        mDimView.animate().alpha(1f).setDuration(250).start();

        if (mPlayerController != null) mPlayerController.hide();
    }

    public void hide() {
        if (!mIsShowing) return;
        mIsShowing = false;

        TranslateAnimation slideOut = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        slideOut.setDuration(200);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) { }
            @Override public void onAnimationRepeat(Animation a) { }
            @Override public void onAnimationEnd(Animation a) { setVisibility(GONE); }
        });
        mPanelView.startAnimation(slideOut);
        mDimView.animate().alpha(0f).setDuration(200).start();
    }

    public boolean isEpisodeShowing() { return mIsShowing; }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsShowing;
    }

    @Override
    public void attach(@NonNull PlayerController controlWrapper) {
        mPlayerController = controlWrapper;
    }

    @Nullable
    @Override
    public View getView() { return this; }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) { }

    @Override
    public void onPlayStateChanged(int playState) {
        if (mIsShowing &&
                (playState == PlayerConstants.STATE_IDLE || playState == PlayerConstants.STATE_ERROR)) {
            hide();
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void setProgress(int duration, int position) { }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked && mIsShowing) hide();
    }
}
