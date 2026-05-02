package com.star.play.media3;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.datasource.DefaultHttpDataSource;

import xyz.doikki.videoplayer.player.AbstractPlayer;

import java.util.Map;

/**
 * Media3 ExoPlayer adapter for DKPlayer.
 * Uses {@code androidx.media3.exoplayer.ExoPlayer} which has better
 * HLS buffered-position tracking than ExoPlayer 2.x.
 * <p>
 * Aggressive buffering: max 1-hour buffer, continues loading even when paused.
 */

@SuppressLint("UnsafeOptInUsageError")
public class Media3Player extends AbstractPlayer implements Player.Listener {

    @Nullable
    private ExoPlayer mPlayer;
    private android.content.Context mAppContext;
    private String mUrl;
    private Map<String, String> mHeaders;
    private int mVideoWidth;
    private int mVideoHeight;

    public void setAppContext(android.content.Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void initPlayer() {
        mPlayer = new ExoPlayer.Builder(mAppContext)
                .setLoadControl(new EagerLoadControl())
                .build();
        mPlayer.addListener(this);
    }

    @Override
    public void setDataSource(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
        if (mPlayer != null) {
            mPlayer.setMediaSource(buildMediaSource());
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        // Not used; ignore
    }

    private MediaSource buildMediaSource() {
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        if (mHeaders != null && !mHeaders.isEmpty()) {
            dataSourceFactory.setDefaultRequestProperties(mHeaders);
        }

        MediaItem mediaItem = MediaItem.fromUri(mUrl);
        if (mUrl != null && mUrl.contains(".m3u8")) {
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        }
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem);
    }

    @Override
    public void start() {
        if (mPlayer != null) mPlayer.play();
    }

    @Override
    public void pause() {
        if (mPlayer != null) mPlayer.pause();
    }

    @Override
    public void stop() {
        if (mPlayer != null) mPlayer.stop();
    }

    @Override
    public void prepareAsync() {
        if (mPlayer != null) {
            mPlayer.prepare();
            mPlayer.play();
        }
    }

    @Override
    public void reset() {
        if (mPlayer != null) mPlayer.stop();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    @Override
    public void seekTo(long pos) {
        if (mPlayer != null) mPlayer.seekTo(pos);
    }

    @Override
    public void release() {
        if (mPlayer != null) {
            mPlayer.removeListener(this);
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer != null ? mPlayer.getCurrentPosition() : 0;
    }

    @Override
    public long getDuration() {
        return mPlayer != null ? mPlayer.getDuration() : 0;
    }

    @Override
    public int getBufferedPercentage() {
        return mPlayer != null ? mPlayer.getBufferedPercentage() : 0;
    }

    @Override
    public void setSurface(Surface surface) {
        if (mPlayer != null) mPlayer.setVideoSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (mPlayer != null) mPlayer.setVideoSurfaceHolder(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        if (mPlayer != null) mPlayer.setVolume(v1);
    }

    @Override
    public void setLooping(boolean looping) {
        if (mPlayer != null) mPlayer.setRepeatMode(
                looping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
    }

    @Override
    public void setOptions() {
    }

    @Override
    public void setSpeed(float speed) {
        if (mPlayer != null) mPlayer.setPlaybackSpeed(speed);
    }

    @Override
    public float getSpeed() {
        return mPlayer != null ? mPlayer.getPlaybackParameters().speed : 1f;
    }

    @Override
    public long getTcpSpeed() {
        return 0;
    }

    // ── Player.Listener ──

    @Override
    public void onPlaybackStateChanged(int state) {
        switch (state) {
            case Player.STATE_READY:
                if (mPlayerEventListener != null) {
                    mPlayerEventListener.onPrepared();
                    mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_END, 0);
                    mPlayerEventListener.onInfo(MEDIA_INFO_RENDERING_START, 0);
                }
                break;
            case Player.STATE_BUFFERING:
                if (mPlayerEventListener != null) {
                    mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_START, 0);
                }
                break;
            case Player.STATE_ENDED:
                if (mPlayerEventListener != null) {
                    mPlayerEventListener.onCompletion();
                }
                break;
            case Player.STATE_IDLE:
                break;
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        mVideoWidth = videoSize.width;
        mVideoHeight = videoSize.height;
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onVideoSizeChanged(mVideoWidth, mVideoHeight);
        }
    }

    // ── 激进缓冲 LoadControl：暂停也继续加载 ──

    @SuppressWarnings("deprecation")
    private static class EagerLoadControl extends DefaultLoadControl {

        EagerLoadControl() {
            super(
                new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MIN_BUFFER_FOR_LOCAL_PLAYBACK_MS,
                3_600_000, // maxBufferMs 1小时
                DefaultLoadControl.DEFAULT_MAX_BUFFER_FOR_LOCAL_PLAYBACK_MS,
                50_000,    // bufferForPlaybackMs
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_FOR_LOCAL_PLAYBACK_MS,
                50_000,    // bufferForPlaybackAfterRebufferMs
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_FOR_LOCAL_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
                DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS_FOR_LOCAL_PLAYBACK,
                DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
                DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME,
                com.google.common.collect.ImmutableMap.of()
            );
        }

        @Override
        public boolean shouldContinueLoading(long playbackPositionUs,
                                              long bufferedDurationUs,
                                              float playbackSpeed) {
            if (bufferedDurationUs < 3_600_000_000L) {
                return true;
            }
            return super.shouldContinueLoading(playbackPositionUs, bufferedDurationUs, playbackSpeed);
        }
    }
}
