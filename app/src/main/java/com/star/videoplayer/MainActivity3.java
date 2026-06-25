package com.star.videoplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.star.play.StarVideoPlayer;
import com.star.play.controller.StarEpisodeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.star.play.StarVideoPlayer;

/**
 * 测试页面：多播放源 + 自定义选集面板
 * 每个播放源16集视频
 */
public class MainActivity3 extends AppCompatActivity {

    private StarVideoPlayer videoView;

    // 播放源数据
    private final List<VideoSource> videoSources = new ArrayList<>();
    private int currentSourceIndex = 0;
    private int currentEpisodeIndex = 0;

    // 自定义面板中的视图
    private TextView tvSourceTitle;
    private MaterialButton btnSourceSelect;
    private RecyclerView rvEpisodes;
    private EpisodeAdapter episodeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        initData();
        initPlayer();
        setupCustomEpisodePanel();
    }

    /**
     * 初始化测试数据：3个播放源，每个16集
     */
    private void initData() {
        videoSources.add(new VideoSource("播放源1", "https://vv.jisuzyv.com/play/negox6je/index.m3u8", 16));
        videoSources.add(new VideoSource("播放源2", "https://vv.jisuzyv.com/play/negox6je/index.m3u8", 16));
        videoSources.add(new VideoSource("播放源3", "https://vv.jisuzyv.com/play/negox6je/index.m3u8", 16));
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        videoView = findViewById(R.id.player);

        // 设置第一个播放源第一集
        VideoSource source = videoSources.get(0);
        videoView.setUrl(source.getEpisodeUrl(0));
        videoView.addDefaultControlComponent(source.name, false);

        // 按钮可见性设置
        videoView.setVisibilityBottomNormal(
                View.VISIBLE, View.GONE, View.GONE, View.GONE,
                View.VISIBLE, View.GONE
        );
        videoView.setVisibilityBottomFullscreen(
                View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE,
                View.VISIBLE, View.VISIBLE
        );

        videoView.start();

        // 上一集/下一集监听
        videoView.setOnUpSetClickListener(v -> playEpisode(currentEpisodeIndex - 1));
        videoView.setOnDownSetClickListener(v -> playEpisode(currentEpisodeIndex + 1));
    }

    /**
     * 设置自定义选集面板
     */
    private void setupCustomEpisodePanel() {
        // 加载自定义布局
        View customView = LayoutInflater.from(this)
                .inflate(R.layout.custom_episode_panel, null);

        // 绑定视图
        tvSourceTitle = customView.findViewById(R.id.tv_source_title);
        btnSourceSelect = customView.findViewById(R.id.btn_source_select);
        rvEpisodes = customView.findViewById(R.id.rv_episodes);

        // 设置播放源标题
        updateSourceTitle();

        // 播放源选择按钮
        btnSourceSelect.setOnClickListener(v -> showSourcePopupMenu(v));

        // 设置剧集列表
        setupEpisodeList();

        // 设置自定义内容到选集面板
        videoView.setEpisodeCustomContentView(customView);

        // 隐藏默认标题栏（因为我们自定义了标题区域）
        videoView.setEpisodePanelTitleBarVisibility(View.GONE);

        // 选集监听
        videoView.setOnEpisodeSelectListener((index, title) -> {
            playEpisode(index);
            videoView.hideEpisodePanel();
        });
    }

    /**
     * 设置剧集列表
     */
    private void setupEpisodeList() {
        ArrayList<HashMap<String, Object>> episodeItems = new ArrayList<>();
        VideoSource source = videoSources.get(currentSourceIndex);

        for (int i = 0; i < source.episodeCount; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", "第" + (i + 1) + "集");
            map.put("index", i);
            episodeItems.add(map);
        }

        episodeAdapter = new EpisodeAdapter(episodeItems);
        episodeAdapter.setOnItemClickListener((view, position, item) -> {
            playEpisode(position);
            videoView.hideEpisodePanel();
        });

        // 使用网格布局，每行4个
        rvEpisodes.setLayoutManager(new GridLayoutManager(this, 4));
        rvEpisodes.setAdapter(episodeAdapter);
    }

    /**
     * 显示播放源选择 PopupMenu
     */
    private void showSourcePopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        for (int i = 0; i < videoSources.size(); i++) {
            popupMenu.getMenu().add(0, i, i, videoSources.get(i).name);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            if (index != currentSourceIndex) {
                switchSource(index);
            }
            return true;
        });

        popupMenu.show();
    }

    /**
     * 切换播放源
     */
    private void switchSource(int sourceIndex) {
        currentSourceIndex = sourceIndex;
        currentEpisodeIndex = 0;

        updateSourceTitle();
        setupEpisodeList();

        // 播放新源的第一集
        VideoSource source = videoSources.get(currentSourceIndex);
        videoView.setUrl(source.getEpisodeUrl(0));
        videoView.start();

        Toast.makeText(this, "已切换到" + source.name, Toast.LENGTH_SHORT).show();
    }

    /**
     * 播放指定集数
     */
    private void playEpisode(int episodeIndex) {
        VideoSource source = videoSources.get(currentSourceIndex);

        if (episodeIndex < 0 || episodeIndex >= source.episodeCount) {
            Toast.makeText(this, "没有更多集数了", Toast.LENGTH_SHORT).show();
            return;
        }

        currentEpisodeIndex = episodeIndex;
        videoView.setUrl(source.getEpisodeUrl(episodeIndex));
        videoView.start();

        Toast.makeText(this, "正在播放：" + source.name + " 第" + (episodeIndex + 1) + "集", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新播放源标题显示
     */
    private void updateSourceTitle() {
        if (tvSourceTitle != null) {
            tvSourceTitle.setText(videoSources.get(currentSourceIndex).name);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }

    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    /**
     * 播放源数据类
     */
    private static class VideoSource {
        String name;
        String baseUrl;
        int episodeCount;

        VideoSource(String name, String baseUrl, int episodeCount) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.episodeCount = episodeCount;
        }

        String getEpisodeUrl(int index) {
            // 实际项目中这里返回真实的视频地址
            return "https://vv.jisuzyv.com/play/negox6je/index.m3u8";
        }
    }
}
