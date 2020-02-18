package com.example.administrator.rtmpscreen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.MediaCodecInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codec.SurfaceFactory;
import com.example.codec.codec.MediaCodecHelper;
import com.example.codec.codec.MediaCodecLogHelper;
import com.example.codec.sender.Sender;
import com.example.codec.surface.MediaCodecSurface;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.administrator.rtmpscreen.R.id.et_ip;

public class RecordActivity extends AppCompatActivity implements Sender.OpenCallback {

    private MediaProjectionManager mPrMgr;
    private SurfaceFactory factory;
    private int width = 720;
    private int height = 1280;
    private MediaProjection mPr;
    private Button mBtn;
    private int mPrCode;
    private Intent mPrBundle;
    //    String url = "rtmp://localhost/live/";
//    String url = "rtmp://localhost/live/STREAM_NAME";
//    String url = "rtmp://169.254.8.220/live/STREAM_NAME";
    //实际的流地址!
    String url = "";
    private Timer timer;
    private TextView mCountText;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);

        //先请求permission
        int writePm = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int InternetPm = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (writePm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        et = findViewById(R.id.et_ip);
        mBtn =  findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et.getText())){
                    Toast.makeText(RecordActivity.this,"请输入RTMP推流地址",Toast.LENGTH_SHORT).show();
                    return;
                }
                url = et.getText().toString();
                if (mPr == null && mPrCode == 0 && mPrBundle == null) {
                    Sender.getInstance().open(url, width, height,RecordActivity.this);
                } else {
                    if (mPr != null) {
                        releaseProjection();
                        releaseTimer();
                    }
                }
            }
        });

        factory = new MediaCodecSurface();

        String mimeType = MediaCodecHelper.VIDEO_AVC;


       /* ArrayList<MediaCodecInfo> adaptiveEncoderCodec = MediaCodecHelper.getAdaptiveEncoderCodec(mimeType);
        String s = MediaCodecLogHelper.printVideoCodecCap(adaptiveEncoderCodec, mimeType);
        TextView textView = (TextView) findViewById(com.example.codec.R.id.tv);
        textView.setText(s);*/


        mCountText = findViewById(R.id.count_);
        mCountText.setVisibility(View.GONE);
    }

    private void startTimer() {
        timer = new Timer();
        long startTime = System.currentTimeMillis();
        mCountText.setVisibility(View.VISIBLE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    long curTime = System.currentTimeMillis();
                    long l = (curTime - startTime) / 1000;
                    mCountText.setText("" + l);
                });
            }
        }, 0, 1000);
    }

    private void releaseTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            mCountText.setVisibility(View.GONE);
        }
    }

    private void releaseProjection() {
        mPr.stop();
        mPr = null;
        mPrCode = 0;
        mPrBundle = null;
        factory.stop();
        mBtn.setText("start");
        Sender.getInstance().close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission not granted!!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mPrCode = resultCode;
        this.mPrBundle = data;
        mPr = mPrMgr.getMediaProjection(this.mPrCode, this.mPrBundle);
        if (mPr != null) {
            Surface surface = factory.createSurface(width, height);
            if (surface == null) {
                releaseProjection();
                releaseTimer();
                Toast.makeText(this, "Can not create surface", Toast.LENGTH_SHORT).show();
            } else {
                startTimer();
                mPr.createVirtualDisplay("display-", width, height, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);
//            mediaProjection.registerCallback();
                mBtn.setText("stop");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sender.getInstance().close();
        releaseTimer();

    }

    @Override
    public void openSuccess() {
        mPrMgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mPrMgr.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, 1);
    }

    @Override
    public void openFailed() {
        runOnUiThread(() -> Toast.makeText(this,"无法连接到指定的RTMP流",Toast.LENGTH_SHORT).show());
    }
}
