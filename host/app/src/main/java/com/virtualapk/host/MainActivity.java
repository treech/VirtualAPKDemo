package com.virtualapk.host;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.didi.virtualapk.PluginManager;
import com.squareup.picasso.Picasso;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.functions.Consumer;

import static com.virtualapk.host.AssetsManager.PLUGIN_DIR;

/**
 * Created by piglet696 on 17/7/5.
 */

public class MainActivity extends Activity implements View.OnClickListener {

    private String TAG = "MainActivity";
    public static final String PLUGIN_PKG_NAME = "com.virtualapk.imageplugin";
    public static final String IMG1_URL = "http://pic.wenwen.soso.com/p/20130809/20130809170922-693425734.jpg";
    public static final String IMG2_URL = "http://img2.imgtn.bdimg.com/it/u=2881489320,987765159&fm=214&gp=0.jpg";
//    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView homeIconIv1 = (ImageView) findViewById(R.id.iv_home_icon1);
        ImageView homeIconIv2 = (ImageView) findViewById(R.id.iv_home_icon2);
//        textView = (TextView) findViewById(R.id.tv);
        Button imageBrowserBtn = (Button) findViewById(R.id.btn_image_browser);

        Picasso.with(this).load(IMG1_URL).into(homeIconIv1);
        Picasso.with(this).load(IMG2_URL).into(homeIconIv2);
        requestPermissions();
        imageBrowserBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (PluginManager.getInstance(this).getLoadedPlugin(PLUGIN_PKG_NAME) == null) {
            Toast.makeText(getApplicationContext(),
                    "插件未加载,请尝试重启APP", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setClassName("com.virtualapk.imageplugin", "com.virtualapk.imageplugin.ImageBrowserActivity");
        startActivity(intent);
//        textView.setVisibility(View.INVISIBLE);

        Intent intent1 = new Intent();
        intent1.setClassName("com.virtualapk.imageplugin", "com.virtualapk.imageplugin.ImageService");
        startService(intent1);

    }

    private void loadPlugin(Context base) {
        PluginManager pluginManager = PluginManager.getInstance(base);
//        File apkPath = getDir(APK_DIR, Context.MODE_PRIVATE);

        File apk_encrypt = new File(PLUGIN_DIR, "plugin-apk");
        File apk_origin = new File(PLUGIN_DIR, "plugin.apk");
        apk_encrypt.renameTo(apk_origin);
        Log.e(TAG, "apk:" + apk_origin);
        if (apk_origin.exists()) {
            try {
                Log.e(TAG, "plugin loading...");
                pluginManager.loadPlugin(apk_origin);
                Log.e(TAG, "plugin loaded...");
                apk_origin.renameTo(apk_encrypt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "apk不存在...");
            Toast.makeText(getApplicationContext(),
                    "SDcard根目录未检测到plugin.apk插件", Toast.LENGTH_SHORT).show();
        }
    }

    private int count = 0;

    private void requestPermissions() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission
                .requestEach(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            Log.e(TAG, permission.name + " is granted.");
                            count++;
                            if (count == 2) {
                                AssetsManager.copyAllAssetsApk(MainActivity.this);
                                loadPlugin(MainActivity.this);
                            }
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            Log.d(TAG, permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            Log.d(TAG, permission.name + " is denied.");
                        }
                    }
                });
    }

}
