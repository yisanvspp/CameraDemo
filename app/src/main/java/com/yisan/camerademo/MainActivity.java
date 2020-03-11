package com.yisan.camerademo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.yisan.camerademo.camera.CustomCamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * 学习Android相机的调用
 * 自定义相机
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    /**
     * 请求权限CODE
     */
    private static final int REQUEST_PERMISSION_CODE = 1;
    /**
     * 请求相机CODE
     */
    private static final int REQUEST_SYSTEM_CAMERA_2 = 2;
    /**
     * 请求相机CODE
     */
    private static final int REQUEST_SYSTEM_CAMERA_1 = 3;

    /**
     * 请求自定义相册
     */
    private static final int REQUEST_CUSTOM_CAMERA = 4;

    private String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private ImageView ivPic;
    private String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ivPic = findViewById(R.id.iv_pic);
        findViewById(R.id.btn_system_camera_1).setOnClickListener(this);
        findViewById(R.id.btn_system_camera_2).setOnClickListener(this);
        findViewById(R.id.btn_custom_camera).setOnClickListener(this);

        checkPermission();


        filePath = Environment.getExternalStorageDirectory().getPath();
        filePath = filePath + File.separator + "temp.png";

    }

    private void checkPermission() {
        boolean hasPermissions = EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);
        //没有权限
        if (!hasPermissions) {
            //申请权限
            EasyPermissions.requestPermissions(this, "需要相机权限才能使用该功能", REQUEST_PERMISSION_CODE, permissions);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /**
     * 请求权限成功
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * 请求权限失败
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_system_camera_2) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //更改系统默认的照片存储路径 - api大于24的时候，已经修改了Uri.fromFile方式
            //Uri uri = Uri.fromFile(new File(filePath));
            //修改uri 将file://修改成content://
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(filePath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQUEST_SYSTEM_CAMERA_2);

        } else if (v.getId() == R.id.btn_system_camera_1) {
            //调用系统相机返回的图片是压缩过的
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_SYSTEM_CAMERA_1);

        } else if (v.getId() == R.id.btn_custom_camera) {
            //自定义相机
            Intent intent = new Intent(MainActivity.this, CustomCamera.class);
            startActivityForResult(intent, REQUEST_CUSTOM_CAMERA);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_SYSTEM_CAMERA_1) {
                //压缩过的图片的二进制流、存放到Bundle中
                Bundle bundle = data.getExtras();
                //压缩过的二进制图片数据
                Bitmap bitmap = (Bitmap) bundle.get("data");
                ivPic.setImageBitmap(bitmap);

            } else if (requestCode == REQUEST_SYSTEM_CAMERA_2) {
                try {
                    FileInputStream fis = new FileInputStream(filePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    ivPic.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CUSTOM_CAMERA) {

                String image_path = data.getStringExtra("IMAGE_PATH");
                //Android系统默认的显示的横屏显示。CustomCamera的预览角度
                //camera.setDisplayOrientation(90);
                Bitmap bitmap = BitmapFactory.decodeFile(image_path);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, false);
                ivPic.setImageBitmap(bitmap);
            }
        }

    }
}
