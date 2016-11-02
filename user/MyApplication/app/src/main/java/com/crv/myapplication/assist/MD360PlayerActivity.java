package com.crv.myapplication.assist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.Global;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDHotspotPlugin;
import com.asha.vrlib.plugins.MDWidgetPlugin;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.crv.myapplication.Activity.RecodeActivity;
import com.crv.myapplication.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public abstract class MD360PlayerActivity extends Activity {

    private static final String TAG = "MD360PlayerActivity";

    private static final SparseArray<String> sDisplayMode = new SparseArray<>();
    private static final SparseArray<String> sInteractiveMode = new SparseArray<>();
    private static final SparseArray<String> sProjectionMode = new SparseArray<>();
    private static final SparseArray<String> sAntiDistortion = new SparseArray<>();

    public boolean isP=false;

    public static ImageButton cBtn;

    static {
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_NORMAL,"NORMAL");

        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH,"M & T");

        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE,"SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL,"MULTI FISH EYE HORIZONTAL");

        sAntiDistortion.put(0,"ANTI-DISABLE");
    }


    public static void startVideo(Context context, Uri uri){
        start(context, uri, RecodeActivity.class);
    }

    private static void start(Context context, Uri uri, Class<? extends Activity> clz){
        Intent i = new Intent(context,clz);
        i.setData(uri);
        context.startActivity(i);
    }

    private MDVRLibrary mVRLibrary;

    private List<MDAbsPlugin> plugins = new LinkedList<>();

    private MDPosition logoPosition = MDPosition.newInstance().setY(-8.0f).setYaw(-90.0f);

    private MDPosition[] positions = new MDPosition[]{
            MDPosition.newInstance().setZ(-8.0f).setYaw(-45.0f),
            MDPosition.newInstance().setZ(-18.0f).setYaw(15.0f).setAngleX(15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-10.0f).setAngleX(-15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(30.0f).setAngleX(30),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-30.0f).setAngleX(-30),
            MDPosition.newInstance().setZ(-5.0f).setYaw(30.0f).setAngleX(60),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45).setAngleY(45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(0.0f).setAngleX(90),
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set content view
        setContentView(R.layout.activity_md_using_surface_view);

        cBtn = (ImageButton) findViewById(R.id.controlBtn);

        cBtn.setOnClickListener(mClickListener);
        //isP=isitP();

        SetCtrlBtnImage(1);


        // init VR Library
        mVRLibrary = createVRLibrary();

        final List<View> hotspotPoints = new LinkedList<>();
        hotspotPoints.add(findViewById(R.id.hotspot_point1));
        hotspotPoints.add(findViewById(R.id.hotspot_point2));

        SpinnerHelper.with(this)
                .setData(sDisplayMode)
                .setDefault(mVRLibrary.getDisplayMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchDisplayMode(MD360PlayerActivity.this, key);
                        int i = 0;
                        for (View point : hotspotPoints){
                            point.setVisibility(i < mVRLibrary.getScreenSize() ? View.VISIBLE : View.GONE);
                            i++;
                        }
                    }
                })
                .init(R.id.spinner_display);

        SpinnerHelper.with(this)
                .setData(sInteractiveMode)
                .setDefault(mVRLibrary.getInteractiveMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchInteractiveMode(MD360PlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_interactive);

        SpinnerHelper.with(this)
                .setData(sProjectionMode)
                .setDefault(mVRLibrary.getProjectionMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchProjectionMode(MD360PlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_projection);

        SpinnerHelper.with(this)
                .setData(sAntiDistortion)
                .setDefault(mVRLibrary.isAntiDistortionEnabled() ? 1 : 0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.setAntiDistortionEnabled(key != 0);
                    }
                })
                .init(R.id.spinner_distortion);

        findViewById(R.id.button_add_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int index = (int) (Math.random() * 100) % positions.length;
                MDPosition position = positions[index];
                MDHotspotBuilder builder = MDHotspotBuilder.create()
                        .size(4f,4f)
                        .provider(0, new AndroidDrawableProvider(android.R.drawable.star_off))
                        .provider(1, new AndroidDrawableProvider(android.R.drawable.star_on))
                        .provider(10, new AndroidDrawableProvider(android.R.drawable.checkbox_off_background))
                        .provider(11, new AndroidDrawableProvider(android.R.drawable.checkbox_on_background))
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                if (hitHotspot instanceof MDWidgetPlugin){
                                    MDWidgetPlugin widgetPlugin = (MDWidgetPlugin) hitHotspot;
                                    widgetPlugin.setChecked(!widgetPlugin.getChecked());
                                }
                            }
                        })
                        .title("star" + index)
                        .position(position)
                        .status(0,1)
                        .checkedStatus(10,11);

                MDWidgetPlugin plugin = new MDWidgetPlugin(builder);

                plugins.add(plugin);
                getVRLibrary().addPlugin(plugin);
                Toast.makeText(MD360PlayerActivity.this, "add plugin position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_add_plugin_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDHotspotBuilder builder = MDHotspotBuilder.create()
                        .size(4f,4f)
                        .provider(new MDVRLibrary.IBitmapProvider() {
                            @Override
                            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moredoo_logo);
                                callback.texture(bitmap);
                            }
                        })
                        .title("logo")
                        .position(logoPosition)
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                Toast.makeText(MD360PlayerActivity.this, "click logo", Toast.LENGTH_SHORT).show();
                            }
                        });
                MDHotspotPlugin plugin = new MDHotspotPlugin(builder);
                plugins.add(plugin);
                getVRLibrary().addPlugin(plugin);
                Toast.makeText(MD360PlayerActivity.this, "add plugin logo" , Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_remove_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plugins.size() > 0){
                    MDAbsPlugin plugin = plugins.remove(plugins.size() - 1);
                    getVRLibrary().removePlugin(plugin);
                }
            }
        });

        findViewById(R.id.button_remove_plugins).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plugins.clear();
                getVRLibrary().removePlugins();
            }
        });

        final TextView hotspotText = (TextView) findViewById(R.id.hotspot_text);
        getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IEyePickListener() {
            @Override
            public void onHotspotHit(IMDHotspot hotspot, long hitTimestamp) {
                String text = hotspot == null ? "nop" : String.format(Locale.CHINESE, "%s  %fs", hotspot.getTitle(), (System.currentTimeMillis() - hitTimestamp) / 1000.0f );
                hotspotText.setText(text);

                if (System.currentTimeMillis() - hitTimestamp > 5000){
                    getVRLibrary().resetEyePick();
                }
            }
        });
    }




    ImageButton.OnClickListener mClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            if (Global.video_state == 3 && MediaPlayerWrapper.mPlayer.isPlaying())
            {
                onPause();
            }
            else
            {
                onResume();
            }
            if(Global.video_state==3 && !MediaPlayerWrapper.mPlayer.isPlaying()) {
                MediaPlayerWrapper.mPlayer.start();
            }
        }
    };
    abstract protected MDVRLibrary createVRLibrary();

    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //cBtn.setImageResource(R.drawable.pausebtn_white);
        mVRLibrary.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //cBtn.setImageResource(R.drawable.startbtn_white);
        mVRLibrary.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cBtn.setImageResource(R.drawable.startbtn_white);
        mVRLibrary.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }

    protected Uri getUri() {
        Intent i = getIntent();
        if (i == null || i.getData() == null){
            return null;
        }
        return i.getData();
    }

    public void cancelBusy(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    private class AndroidDrawableProvider implements MDVRLibrary.IBitmapProvider{

        private int res;

        public AndroidDrawableProvider(int res) {
            this.res = res;
        }

        @Override
        public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), this.res);
            callback.texture(bitmap);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder alert = new AlertDialog.Builder(MD360PlayerActivity.this);
                alert.setMessage("영상을 종료 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'

                                finish();
                                //android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return ;
                            }
                        });
                AlertDialog alert_t = alert.create();
                alert.show();
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //TODO Auto-generated method stub
        //Toast.makeText(getApplicationContext(),"WTF",Toast.LENGTH_SHORT).show();

        return super.onTouchEvent(event);
    }

    public static void SetCtrlBtnImage(int x){
        int st = x;

        if(st==1)
        {
            cBtn.setImageResource(R.drawable.pausebtn_white);
        }
        else if(st==2)
        {
            cBtn.setImageResource(R.drawable.refreshbtn_white);
        }
        else
        {
            cBtn.setImageResource(R.drawable.startbtn_white);
        }
    }


}