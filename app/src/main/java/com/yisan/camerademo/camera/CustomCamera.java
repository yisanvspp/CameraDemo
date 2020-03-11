package com.yisan.camerademo.camera;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yisan.camerademo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author：wzh
 * @description: 自定义相机
 * @packageName: com.yisan.camerademo.camera
 * @date：2020/3/11 0011 下午 1:44
 */
public class CustomCamera extends AppCompatActivity implements SurfaceHolder.Callback {


    private Button btnTakePic;
    private SurfaceView surfaceView;
    private Camera camera;
    private SurfaceHolder surfaceHolder;


    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //图片返回的二进制数据data
            try {
                String tempPath = Environment.getExternalStorageDirectory().getPath() + "/camerademo/"
                        + System.currentTimeMillis() + ".png";
                File tempFile = new File(tempPath);
                tempFile.getParentFile().mkdirs();
                tempFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(data);
                fos.close();

                //回传
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("IMAGE_PATH", tempPath);
                intent.putExtras(bundle);
                CustomCamera.this.setResult(RESULT_OK, intent);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_camera_activity);

        btnTakePic = findViewById(R.id.btn_take);
        surfaceView = findViewById(R.id.surface_view);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic();
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //自动对焦
                camera.autoFocus(null);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = getCamera();
            if (surfaceHolder != null) {
                setStartPreview(camera, surfaceHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * 获得相机
     */
    private Camera getCamera() {

        try {
            if (camera == null) {
                camera = Camera.open();
            }
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 拍照
     */
    private void takePic() {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewSize(800, 400);
        //自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.takePicture(null, null, pictureCallback);
                }
            }
        });


    }

    /**
     * 释放
     */
    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 开始预览相机内容
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            //camera和surfaceholder关联
            camera.setPreviewDisplay(holder);
            //设置预览的角度（默认是横屏显示）
            camera.setDisplayOrientation(90);
            //开启预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(camera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.stopPreview();
        setStartPreview(camera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
