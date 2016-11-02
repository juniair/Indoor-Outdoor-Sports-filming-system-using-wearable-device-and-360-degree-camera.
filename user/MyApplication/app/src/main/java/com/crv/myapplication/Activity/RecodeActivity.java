package com.crv.myapplication.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.Global;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.crv.myapplication.R;
import com.crv.myapplication.assist.CustomProjectionFactory;
import com.crv.myapplication.assist.MD360PlayerActivity;
import com.crv.myapplication.assist.MediaPlayerWrapper;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class RecodeActivity extends MD360PlayerActivity {
    public String VideoUrl;
    public String file;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaPlayerWrapper.init();

        //setContentView(R.layout.activity_recode);

        Global.video_state = mMediaPlayerWrapper.mStatus;

        Log.d("mStatus11"," === "+mMediaPlayerWrapper.mStatus);
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
            }
        });
        Log.d("mStatus22"," === "+mMediaPlayerWrapper.mStatus);

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(RecodeActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        ChangeImage thr = new ChangeImage();
        thr.setDaemon(true);
        thr.start();

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });

        Uri uri = getUri();
        if (uri != null){
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }

    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.getPlayer().setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(RecodeActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchEnabled(true)
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(R.id.gl_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerWrapper.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayerWrapper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.onResume();
    }

    class ChangeImage extends Thread{
        @Override
        public void run(){
            while (true) {

                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if(mMediaPlayerWrapper.mStatus==3 && !mMediaPlayerWrapper.mPlayer.isPlaying()){
                            MD360PlayerActivity.SetCtrlBtnImage(2);
                        }
                        else if(mMediaPlayerWrapper.mStatus==3 && mMediaPlayerWrapper.mPlayer.isPlaying()){
                            MD360PlayerActivity.SetCtrlBtnImage(1);
                        }
                        else{
                            MD360PlayerActivity.SetCtrlBtnImage(3);
                        }
                    }
                });
            }
        }
    }
}

