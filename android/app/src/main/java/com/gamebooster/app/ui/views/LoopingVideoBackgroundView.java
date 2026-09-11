package com.gamebooster.app.ui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

/**
 * LoopingVideoBackgroundView — Hardware-accelerated TextureView video player.
 *
 * Designed for background video animations:
 * - Silent playback (muted audio)
 * - Seamless zero-black-frame infinite looping
 * - CenterCrop aspect ratio scaling without distortion
 * - Battery-friendly lifecycle hooks (pause / resume / release)
 */
public class LoopingVideoBackgroundView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "LoopingVideoView";

    private MediaPlayer mediaPlayer;
    private Surface surface;
    private int rawResId = 0;
    private Uri videoUri = null;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private boolean isPrepared = false;
    private boolean shouldPlayWhenReady = true;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;
    private boolean isMuted = false;

    public LoopingVideoBackgroundView(@NonNull Context context) {
        super(context);
        init();
    }

    public LoopingVideoBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoopingVideoBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (mediaPlayer != null) {
            try {
                if (muted) {
                    mediaPlayer.setVolume(0f, 0f);
                } else {
                    mediaPlayer.setVolume(leftVolume, rightVolume);
                }
            } catch (Throwable ignored) {}
        }
    }

    public void setVolume(float left, float right) {
        this.leftVolume = left;
        this.rightVolume = right;
        this.isMuted = false;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setVolume(left, right);
            } catch (Throwable ignored) {}
        }
    }

    public void setVideoRawResource(@RawRes int rawResId) {
        this.rawResId = rawResId;
        this.videoUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + rawResId);
        openVideo();
    }

    public void setVideoUri(@NonNull Uri uri) {
        this.rawResId = 0;
        this.videoUri = uri;
        openVideo();
    }

    private synchronized void openVideo() {
        if (videoUri == null || surface == null) {
            return;
        }
        releaseMediaPlayer();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(surface);
            mediaPlayer.setDataSource(getContext().getApplicationContext(), videoUri);
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f);
            } else {
                mediaPlayer.setVolume(leftVolume, rightVolume);
            }
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                updateTextureMatrix();
                if (shouldPlayWhenReady) {
                    try {
                        mp.start();
                    } catch (Throwable t) {
                        Log.w(TAG, "Error starting MediaPlayer onPrepared: " + t.getMessage());
                    }
                }
            });

            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                if (width > 0 && height > 0) {
                    videoWidth = width;
                    videoHeight = height;
                    updateTextureMatrix();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.w(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                isPrepared = false;
                return true; // Handled
            });

            mediaPlayer.prepareAsync();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to open video: " + t.getMessage(), t);
        }
    }

    public synchronized void play() {
        shouldPlayWhenReady = true;
        if (mediaPlayer != null && isPrepared) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            } catch (Throwable t) {
                Log.w(TAG, "Error resuming video: " + t.getMessage());
            }
        } else if (mediaPlayer == null && surface != null && videoUri != null) {
            openVideo();
        }
    }

    public synchronized boolean isPlaying() {
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.isPlaying();
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public synchronized void pause() {
        shouldPlayWhenReady = false;
        if (mediaPlayer != null && isPrepared) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Throwable t) {
                Log.w(TAG, "Error pausing video: " + t.getMessage());
            }
        }
    }

    public synchronized void release() {
        shouldPlayWhenReady = false;
        releaseMediaPlayer();
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                isPrepared = false;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Throwable ignored) {
            } finally {
                mediaPlayer = null;
            }
        }
    }

    private void updateTextureMatrix() {
        if (videoWidth <= 0 || videoHeight <= 0) return;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) return;

        float viewAspect = (float) viewWidth / (float) viewHeight;
        float videoAspect = (float) videoWidth / (float) videoHeight;

        float scaleX = 1.0f;
        float scaleY = 1.0f;

        if (viewAspect > videoAspect) {
            // View is wider than video (Crop top/bottom)
            scaleY = (float) viewWidth / ((float) viewHeight * videoAspect);
        } else {
            // View is taller than video (Crop left/right)
            scaleX = (float) (viewHeight * videoAspect) / (float) viewWidth;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, viewWidth / 2.0f, viewHeight / 2.0f);
        setTransform(matrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateTextureMatrix();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            if (shouldPlayWhenReady) {
                play();
            }
        } else {
            pause();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) {
            if (shouldPlayWhenReady) {
                play();
            }
        } else {
            pause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    @SuppressWarnings("deprecation")
    public void trimMemory(int level) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
                || level >= android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            release();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SurfaceTextureListener
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        if (surface != null) {
            surface.release();
        }
        surface = new Surface(surfaceTexture);
        if (videoUri != null && shouldPlayWhenReady) {
            openVideo();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        updateTextureMatrix();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        // Frame rendered
    }
}
