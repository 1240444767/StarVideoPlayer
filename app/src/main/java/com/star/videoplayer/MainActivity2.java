package com.star.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.star.play.StarShortDramaPlayer;
import com.star.play.StarVideoPlayer;
import com.star.play.controller.StarEpisodeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;

public class MainActivity2 extends AppCompatActivity {
    private StarShortDramaPlayer videoView;
    private  String URL = "https://vv.jisuzyv.com/play/negox6je/index.m3u8";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main2);
        videoView = findViewById(R.id.video_player);
        videoView.setUrl(URL);
        videoView.addDefaultControlComponent("短剧", false);
        videoView.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //暂停播放
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //继续播放
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放播放器
        videoView.release();
    }


    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}