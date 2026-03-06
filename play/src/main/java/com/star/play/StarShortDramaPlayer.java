package com.star.play;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;

public class StarShortDramaPlayer extends StarVideoPlayer {

    public StarShortDramaPlayer(@NonNull Context context) {
        this(context, null);
    }

    public StarShortDramaPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarShortDramaPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPlayerFactory(ExoMediaPlayerFactory.create());
        hideShortDramaUI();
    }

    private void hideShortDramaUI() {
        setSelectButtonVisibility(GONE);
        setPreviousButtonVisibility(GONE);
        setNextButtonVisibility(GONE);
    }
}
