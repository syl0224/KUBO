package com.kubo.bo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.provider.Settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kubo.bo.utils.CmdUtils;
import com.kubo.bo.utils.FileUtils;
import com.kubo.bo.utils.InfoUtils;
import com.kubo.bo.utils.JSONParser;
import com.kubo.bo.utils.RandomDataUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private TelephonyManager tm;

    private BroadcastReceiver mBatInfoReceiver;	//battery receiver
    private BroadcastReceiver mOpenGLReceiver;	//openGLinfo receiver

    private GLSurfaceView glSurface;
    private MyOpenGLRenderer gl_render;

    private static final String OPENGL_ACTION = "com.kubo.opengl";
    private String phoneInfo = "";	//android info
    private String imei = "";
    private String systemInfo = "";	// system/etc,lib,bin
    private String procInfo = "";	// /proc/version, /proc/mounts, /proc/filesystems, /proc/diskstats
    private String javaInfo = "";	//java info

    private JSONParser jsonParser;
    private static String host = "http://kubooo.duapp.com/";
    private static String url = host + "addinfo.php";	// /Users/sunyl/Documents/DevInfo/appidvd5542dzqj
    private static final String UPLOAD_TAG_MESSAGE = "message";

    private static final String TAG = "xxxxxx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gl_render = new MyOpenGLRenderer();
        glSurface = (GLSurfaceView)this.findViewById(R.id.glSurface);
        glSurface.setEGLContextClientVersion(2);
        glSurface.setRenderer(gl_render);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        jsonParser = new JSONParser();

        init();
        getProp();	// phoneInfo, imei, systemInfo, procInfo, javaInfo
        //FileUtils.writeInfoTosdcard(phoneInfo + imei+ systemInfo + procInfo);	//save all phone info ,except openGl

        //for (int i = 0; i < 3; i++) {	//upload retry 3 times
        //new Upload().execute();	//upload all phone info ,except openGl
        //}
        saveData();	//save data in sharedpreference
        addClick(R.id.rimei, R.id.rimsi, R.id.rtel, R.id.rsimSerialNum, R.id.rwifimac, R.id.rbluetoothmac,
             R.id.rANDROID_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy() {
        if (mBatInfoReceiver != null) {
            this.unregisterReceiver(mBatInfoReceiver);
            mBatInfoReceiver = null;
        }
        if (mOpenGLReceiver != null) {
            this.unregisterReceiver(mOpenGLReceiver);
            mOpenGLReceiver = null;
        }
        super.onDestroy();
    }

    //set EditText
    private void setEditText(int id, String s) {
        ((EditText)this.findViewById(id)).setText(s);
    }

    //get EditText
    private String getEditText(int id) {
        return ((EditText)this.findViewById(id)).getText().toString();
    }

    @SuppressLint("MissingPermission")
    private void init() {
        Log.e(TAG, "init.......");
        Log.e(TAG, "init  phoneInfo " +phoneInfo);
        try {
            setEditText(R.id.uuid, UUID.randomUUID().toString());
            setEditText(R.id.screen_density, getScreenDensity_ByResources());
            setEditText(R.id.build_model, android.os.Build.MODEL);	//cannot  modify on d4
            setEditText(R.id.build_manufacturer, android.os.Build.MANUFACTURER);	//cannot  modify on d4
            setEditText(R.id.build_board, android.os.Build.BOARD);	//cannot  modify on d4
            setEditText(R.id.build_serial, android.os.Build.SERIAL);	//cannot  modify on d4
            setEditText(R.id.build_brand, android.os.Build.BRAND);	//cannot  modify on d4
            setEditText(R.id.build_device, android.os.Build.DEVICE);	//cannot  modify on d4
            setEditText(R.id.build_hardware, android.os.Build.HARDWARE);	//cannot  modify on d4
            setEditText(R.id.build_host, android.os.Build.HOST);	//cannot  modify on d4
            setEditText(R.id.build_display, android.os.Build.DISPLAY);	//cannot  modify on d4
            setEditText(R.id.build_product, android.os.Build.PRODUCT);	//cannot modify on d4
            setEditText(R.id.build_bootloader, android.os.Build.BOOTLOADER);
            setEditText(R.id.build_fingerprint, android.os.Build.FINGERPRINT);
            setEditText(R.id.build_cpu_abi_1, android.os.Build.CPU_ABI);
            setEditText(R.id.build_cpu_abi_2, android.os.Build.CPU_ABI2);
            setEditText(R.id.build_supported_abis, InfoUtils.getSupABIS());	//android.os.Build.SUPPORTED_ABIS;
            setEditText(R.id.build_radio, android.os.Build.RADIO);
            setEditText(R.id.build_time, String.valueOf(android.os.Build.TIME));	//cannot modify on d4
            setEditText(R.id.build_type, android.os.Build.TYPE);	//cannot modify on d4
            setEditText(R.id.build_tags, android.os.Build.TAGS);	//cannot modify on d4
            setEditText(R.id.build_version_codes_base, String.valueOf(android.os.Build.VERSION_CODES.BASE));//cannot modify on d4
            setEditText(R.id.build_version_sdk, android.os.Build.VERSION.SDK);	//cannot modify on d4
            setEditText(R.id.build_version_sdk_int, String.valueOf(android.os.Build.VERSION.SDK_INT));	//cannot modify on d4
            setEditText(R.id.build_version_release, android.os.Build.VERSION.RELEASE);	//cannot modify on d4
            setEditText(R.id.build_version_codename, android.os.Build.VERSION.CODENAME);	//cannot modify on d4
            setEditText(R.id.build_version_incremental, android.os.Build.VERSION.INCREMENTAL);	//cannot modify on d4
            setEditText(R.id.build_id, android.os.Build.ID);	//cannot modify on d4
            setEditText(R.id.build_user, android.os.Build.USER);	//cannot modify on d4

            setEditText(R.id.networkOperator, tm.getNetworkOperator());
            setEditText(R.id.networkOperatorName, tm.getNetworkOperatorName());
            setEditText(R.id.networkCountryIso, tm.getNetworkCountryIso());
            setEditText(R.id.simSerialNum, tm.getSimSerialNumber());
            String imsi = tm.getSubscriberId();
            setEditText(R.id.imsi, imsi);
            int mcc = this.getResources().getConfiguration().mcc;
            int mnc = this.getResources().getConfiguration().mnc;
            setEditText(R.id.mcc, String.valueOf(mcc));
            setEditText(R.id.mnc, String.valueOf(mnc));
            String country = Locale.getDefault().getCountry();
            setEditText(R.id.sys_language, country);

            setEditText(R.id.simOperator, tm.getSimOperator());
            setEditText(R.id.simOperatorName, tm.getSimOperatorName());
            setEditText(R.id.imei, tm.getDeviceId());
            setEditText(R.id.tel, tm.getLine1Number());
            setEditText(R.id.softwareVersion, tm.getDeviceSoftwareVersion());
            setEditText(R.id.simCountryIso, tm.getSimCountryIso());

            setEditText(R.id.baseband, InfoUtils.getBaseBandVersion());	//cannot modify
            setEditText(R.id.ANDROID_ID, Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

            setEditText(R.id.wifimac, InfoUtils.getMacAddress(wifiInfo));
            setEditText(R.id.bluetoothmac, InfoUtils.getBluetoothMacAddress());
            setEditText(R.id.ip, InfoUtils.getIpAddress(wifiInfo));
            setEditText(R.id.bssid, wifiInfo.getBSSID());
            setEditText(R.id.ssid, wifiInfo.getSSID());
            setEditText(R.id.description, CmdUtils.execCommand("getprop ro.build.description"));
            setEditText(R.id.useragent, InfoUtils.getUserAgent(this));

            setEditText(R.id.sensor, InfoUtils.getSensorInfo(this));	//modify failed
            setEditText(R.id.installedApp, InfoUtils.getInstalledApp(this));	//modify failed
            setEditText(R.id.runningApp, InfoUtils.getRunningApp(this));	//modify failed
            setEditText(R.id.proc_version, CmdUtils.execCommand("cat /proc/version"));	//cannot modify
            setEditText(R.id.proc_mounts, CmdUtils.execCommand("cat /proc/mounts"));	//cannot modify
            setEditText(R.id.proc_filesystems, CmdUtils.execCommand("cat /proc/filesystems"));	//cannot modify
            setEditText(R.id.proc_diskstats, CmdUtils.execCommand("cat /proc/diskstats"));	//cannot modify
            setEditText(R.id.proc_cpuinfo_model_name, InfoUtils.getCpuInfo());	//cannot modify
            setEditText(R.id.system_etc, CmdUtils.execCommand("ls /system/etc"));	//cannot modify
            setEditText(R.id.dev, CmdUtils.execCommand("ls /dev"));	//cannot modify
            setEditText(R.id.system_bin, CmdUtils.execCommand("ls /system/bin"));	//cannot modify
            setEditText(R.id.system_lib, CmdUtils.execCommand("ls /system/lib"));	//cannot modify
            setEditText(R.id.path, System.getenv("PATH"));
            setEditText(R.id.intent_main_action, InfoUtils.getIntentMainAction(this));
            //setEditText(R.id.broadcast_receivers, mr.getBroadcastInfo());	//�����쳣
            setEditText(R.id.javainfo, InfoUtils.getJavaInfo());
            //setEditText(R.id.glinfo, glinfo);	//set in GLReceiver.onReceive��
            setEditText(R.id.buildprop, CmdUtils.execCommand("cat /system/build.prop"));
            setEditText(R.id.getprop, CmdUtils.execCommand("getprop"));
            setEditText(R.id.netinter, InfoUtils.getNetworkInterface(this));
            setEditText(R.id.cellInfo, InfoUtils.getMyCellInfo(tm));
            getBatteryInfo();
            getOpenGLInfo();

        } catch(Exception e) {
            Toast.makeText(this, getPackageName() + "->" + Thread.currentThread().getStackTrace()[2].getMethodName() + " "
                 + e.getStackTrace(), Toast.LENGTH_LONG).show();
            Log.d(TAG, getPackageName() + "->" + Thread.currentThread().getStackTrace()[2].getMethodName() + " "
                 + e.getStackTrace());
        }
    }

    public void getBatteryInfo() {
        mBatInfoReceiver = new BroadcastReceiver() {
            int BatteryN, BatteryV, BatteryT;
            String BatteryStatus, BatteryTemp;
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                /*
                * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
                */
                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    BatteryN = intent.getIntExtra("level", 0);    //目前电量
                    BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
                    BatteryT = intent.getIntExtra("temperature", 0);  //电池温度

                    switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)) {
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            BatteryStatus = "充电状态";
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            BatteryStatus = "放电状态";
                            break;
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            BatteryStatus = "未充电";
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            BatteryStatus = "充满电";
                            break;
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            BatteryStatus = "未知道状态";
                            break;
                    }

                    switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
                        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                            BatteryTemp = "未知错误";
                            break;
                        case BatteryManager.BATTERY_HEALTH_GOOD:
                            BatteryTemp = "状态良好";
                            break;
                        case BatteryManager.BATTERY_HEALTH_DEAD:
                            BatteryTemp = "电池没有电";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                            BatteryTemp = "电池电压过高";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                            BatteryTemp =  "电池过热";
                            break;
                    }
                    setEditText(R.id.battery, "{ 电量:" + BatteryN + "% --- " + BatteryStatus + " }\n" + "{ 电压:" + BatteryV + "mV --- " + BatteryTemp + " }\n" + "{ 温度:" + (BatteryT*0.1) + "℃ }");
                }
            }
        };
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    class MyOpenGLRenderer implements GLSurfaceView.Renderer {
        private static final String TAG_RENDER = "pppppp";
        private String glinfo = "";

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.e(TAG_RENDER, "MyOpenGLRenderer onSurfaceCreated....");
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            if (egl.eglInitialize(eglDisplay, version)) {
                glinfo += "{ GL_VERSION: " + version[0] + "." + version[1] + " }\n";
            }
            glinfo += "{ GL_RENDERER: " + gl.glGetString(GL10.GL_RENDERER) + " }\n";
            glinfo += "{ GL_VENDOR: " + gl.glGetString(GL10.GL_VENDOR) + " }\n";
            glinfo += "{ GL_VERSION: " + gl.glGetString(GL10.GL_VERSION) + " }\n";
            glinfo += "{ GL_EXTENSIONS: " + gl.glGetString(GL10.GL_EXTENSIONS) + " }\n";
            glinfo += "{ GL_SHADING_LANGUAGE_VERSION: " + gl.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION) + " }\n";
            glinfo += "{ egl vendor: " + egl.eglQueryString(eglDisplay, EGL10.EGL_VENDOR) + " }\n";
            glinfo += "{ egl version: " + egl.eglQueryString(eglDisplay, EGL10.EGL_VERSION) + " }\n";
            glinfo += "{ egl extension: " + egl.eglQueryString(eglDisplay, EGL10.EGL_EXTENSIONS) + " }\n";
            Log.d(TAG_RENDER, glinfo);

            Intent intent = new Intent(OPENGL_ACTION);
            intent.putExtra("glinfo", glinfo);
            sendBroadcast(intent);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }

        @Override
        public void onDrawFrame(GL10 gl) {
        }
    }

    public void getOpenGLInfo() {
        //activity_main.xml add android.opengl.GLSurfaceView

        mOpenGLReceiver = new BroadcastReceiver() {
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                Log.e(TAG, "getOpenGLInfo receive......");
                String glinfo = arg1.getStringExtra("glinfo");
                setEditText(R.id.glinfo, glinfo);
                phoneInfo += "glinfo: " + glinfo + "\n";
                //save all phone info ,include openGl
                FileUtils.writeInfoTosdcard(phoneInfo + imei+ systemInfo + procInfo);
                //new Upload().execute();	//upload all phone info ,include openGl
            }
        };
        this.registerReceiver(mOpenGLReceiver, new IntentFilter(OPENGL_ACTION));
    }

    public void getProp() {
        Log.e(TAG, "getProp......");
        Log.e(TAG, "getProp  phoneInfo " +phoneInfo);
        phoneInfo += "uuid:" + getEditText(R.id.uuid) + "\n";
        phoneInfo += "screen density:" + getEditText(R.id.screen_density) + "\n";
        phoneInfo += "Build.MODEL: " + getEditText(R.id.build_model) + "\n";
        phoneInfo += "Build.MANUFACTURER: " + getEditText(R.id.build_manufacturer) + "\n";
        phoneInfo += "Build.BOARD: " + getEditText(R.id.build_board) + "\n";
        phoneInfo += "Build.SERIAL: " + getEditText(R.id.build_serial) + "\n";
        phoneInfo += "Build.BRAND: " + getEditText(R.id.build_brand) + "\n";
        phoneInfo += "Build.DEVICE: " + getEditText(R.id.build_device) + "\n";
        phoneInfo += "Build.HARDWARE: " + getEditText(R.id.build_hardware) + "\n";
        //serial confirm
        phoneInfo += "simSerialNum: " + getEditText(R.id.simSerialNum) + "\n";
        phoneInfo += "Build.HOST: " + getEditText(R.id.build_host) + "\n";
        //displayid confirm
        phoneInfo += "Build.DISPLAY: " + getEditText(R.id.build_display) + "\n";
        phoneInfo += "Build.PRODUCT: " + getEditText(R.id.build_product) + "\n";
        phoneInfo += "Build.BOOTLOADER: " + getEditText(R.id.build_bootloader) + "\n";
        phoneInfo += "baseband: " + getEditText(R.id.baseband) + "\n";
        phoneInfo += "mcc: " + getEditText(R.id.mcc) + "\n";
        phoneInfo += "mnc: " + getEditText(R.id.mnc) + "\n";
        phoneInfo += "system language: " + getEditText(R.id.sys_language) + "\n";

        //op_alpha confirm below two   //getNetworkOperator()
        phoneInfo += "networkOperator: " + getEditText(R.id.networkOperator) + "\n";
        //phoneInfo += "networkOperatorName: " + tm.getNetworkOperatorName() + "\n";
        try {
            phoneInfo += "networkOperatorName: " + new String(getEditText(R.id.networkOperatorName).
                    getBytes("UTF-8"), "UTF-8") + "\n";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        phoneInfo += "networkCountryIso: " + getEditText(R.id.networkCountryIso) + "\n";
        phoneInfo += "simOperator: " + getEditText(R.id.simOperator) + "\n";//310260
        phoneInfo += "simOperatorName: " + getEditText(R.id.simOperatorName) + "\n";//Android

        imei += "imei: " + getEditText(R.id.imei) + "\n";
        phoneInfo += "imsi: " + getEditText(R.id.imsi) + "\n";
        //modem.serial confirm
        phoneInfo += "simSerialNum: " + getEditText(R.id.simSerialNum) + "\n";
        phoneInfo += "tel: " + getEditText(R.id.tel) + "\n";
        phoneInfo += "Secure.ANDROID_ID: " + getEditText(R.id.ANDROID_ID) + "\n";
        phoneInfo += "wifimac: " + getEditText(R.id.wifimac) + "\n";
        phoneInfo += "bluetoothmac: " + getEditText(R.id.bluetoothmac) + "\n";
        phoneInfo += "ip: " + getEditText(R.id.ip) + "\n";
        phoneInfo += "bssid: " + getEditText(R.id.bssid) + "\n";
        phoneInfo += "ssid: " + getEditText(R.id.ssid) + "\n";
        //description confirm
        phoneInfo += "description: " + getEditText(R.id.description) + "\n";
        phoneInfo += "userAgent: " + getEditText(R.id.useragent) + "\n";
        phoneInfo += "Build.FINGERPRINT: " + getEditText(R.id.build_fingerprint) + "\n\n";

        phoneInfo += "Sensor: \n" + getEditText(R.id.sensor) + "\n";
        //getInstalledApp, confirm
        phoneInfo += "InstalledApp: \n" + getEditText(R.id.installedApp) + "\n";
        phoneInfo += "\nRunningTask: \n" + getEditText(R.id.runningApp) + "\n";
        //	/proc/version  getProcInfo("/proc/version")
        //phoneInfo += "/proc/version: \n" + getLinuxKernelInfo()+ "\n\n";
        procInfo += "/proc/version: \n" + getEditText(R.id.proc_version)+ "\n";
        procInfo += "/proc/mounts: \n" + getEditText(R.id.proc_mounts)+ "\n";
        procInfo += "/proc/filesystems: \n" + getEditText(R.id.proc_filesystems)+ "\n";
        procInfo += "/proc/diskstats: \n" + getEditText(R.id.proc_diskstats)+ "\n";
        phoneInfo += "Build.CPU_ABI_1: " + getEditText(R.id.build_cpu_abi_1) + "\n";
        phoneInfo += "Build.CPU_ABI_2: " + getEditText(R.id.build_cpu_abi_2) + "\n";

        phoneInfo += "softwareVersion: " + getEditText(R.id.softwareVersion) + "\n";
        phoneInfo += "simCountryIso: " + getEditText(R.id.simCountryIso)+ "\n";
        //cpu info
        phoneInfo += "/proc/cpuinfo.model_name: " + getEditText(R.id.proc_cpuinfo_model_name)+ "\n";
        phoneInfo += "Build.RADIO: " + getEditText(R.id.build_radio)+ "\n";
        phoneInfo += "Build.TIME: " + getEditText(R.id.build_time)+ "\n";
        phoneInfo += "Build.TYPE: " + getEditText(R.id.build_type)+ "\n";
        phoneInfo += "Build.TAGS: " + getEditText(R.id.build_tags) + "\n";
        phoneInfo += "Build.VERSION_CODES.BASE: " + getEditText(R.id.build_version_codes_base) + "\n";
        phoneInfo += "Build.VERSION.SDK: " + getEditText(R.id.build_version_sdk) + "\n";
        phoneInfo += "Build.VERSION.SDK_INT: " + getEditText(R.id.build_version_sdk_int)+ "\n";
        phoneInfo += "Build.VERSION.RELEASE: " + getEditText(R.id.build_version_release) + "\n";
        phoneInfo += "Build.VERSION.CODENAME: " + getEditText(R.id.build_version_codename)+ "\n";
        phoneInfo += "Build.VERSION.INCREMENTAL: " + getEditText(R.id.build_version_incremental)+ "\n";
        phoneInfo += "Build.ID: " + getEditText(R.id.build_id) + "\n";
        phoneInfo += "Build.USER: " + getEditText(R.id.build_user) + "\n\n";
        phoneInfo += "/dev: \n" + getEditText(R.id.dev) + "\n";

        //java properties
        phoneInfo += "javainfo: " + getEditText(R.id.javainfo);
        phoneInfo += "intent_main_action: \n" + getEditText(R.id.intent_main_action) + "\n";
        //phoneInfo += "broadcast_receivers: " + mr.getBroadcastInfo() + "\n\n";
        //phoneInfo += "glinfo: " + getEditText(R.id.glinfo) + "\n";
        phoneInfo += "build.prop: \n" + getEditText(R.id.buildprop) + "\n";
        phoneInfo += "getprop: \n" + getEditText(R.id.getprop) + "\n";
        phoneInfo += "PATH: \n" + getEditText(R.id.path) + "\n\n";
        phoneInfo += "AllCellInfo: \n" + getEditText(R.id.cellInfo) + "\n";
        phoneInfo += "networkInterface:\n" + getEditText(R.id.netinter) + "\n";

        systemInfo += "/system/etc: \n" + getEditText(R.id.system_etc) + "\n";
        systemInfo += "/system/bin: \n" + getEditText(R.id.system_bin) + "\n";
        systemInfo += "/system/lib: \n" + getEditText(R.id.system_lib) + "\n";

        //t.setText(phoneInfo);
    }

    class Upload extends AsyncTask<String, String, String> {
        @Override
        //upload device info
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "upload onPreExecute......");
            Toast.makeText(getApplicationContext(), "upload onPreExecute", Toast.LENGTH_SHORT);
        }
        protected String doInBackground(String... args) {
            Log.e(TAG, "upload doInBackground......");
            String phoneInfo_utf8 = "";
            try {
                phoneInfo_utf8 = new String(phoneInfo.getBytes("UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            List<NameValuePair> params = new ArrayList<>();
            if (imei.equals("") || imei == null)
                Log.d(TAG, "imei is null");
            if (phoneInfo.equals("") || phoneInfo == null)
                Log.d(TAG, "phoneInfo is null");
            if (systemInfo.equals("") || systemInfo == null)
                Log.d(TAG, "systemInfo is null");
            if (procInfo.equals("") || procInfo == null)
                Log.d(TAG, "procInfo is null");

            params.add(new BasicNameValuePair("imei", imei));
            params.add(new BasicNameValuePair("info", phoneInfo));
            params.add(new BasicNameValuePair("systemInfo", systemInfo));
            params.add(new BasicNameValuePair("procInfo", procInfo));

            try {
                Log.e(TAG, "makeHttpRequest......");
                JSONObject json = jsonParser.makeHttpRequest(url,
                     "POST", params);
                //String message = json.getString(TAG_MESSAGE);
                Log.d(TAG, "json: "+json);
                String message = json.optString(UPLOAD_TAG_MESSAGE);
                return message;
            }catch(Exception e){
                e.printStackTrace();
                return "";
            }
        }
        protected void onPostExecute(String message) {
            Log.e(TAG, "upload onPostExecute......"+message);
            Toast.makeText(getApplicationContext(), "onPostExecute", Toast.LENGTH_SHORT);
            //pDialog.setMessage("onPostExecute");
            //pDialog.dismiss();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
    * save data in SharedPreferences for xposed
    */
    private void saveData() {
     Log.e(TAG, "save data in sharedpreferences...");
     try {
         SharedPreferences sh = this.getSharedPreferences("prefs", Context.MODE_WORLD_READABLE);
         SharedPreferences.Editor pre = sh.edit();

         pre.putString("screen_density", this.getEditText(R.id.screen_density));
         pre.putString("build_model", this.getEditText(R.id.build_model));
         pre.putString("build_manufacturer", this.getEditText(R.id.build_manufacturer));
         pre.putString("build_board", this.getEditText(R.id.build_board));
         pre.putString("build_serial", this.getEditText(R.id.build_serial));
         pre.putString("build_brand", this.getEditText(R.id.build_brand));
         pre.putString("build_device", this.getEditText(R.id.build_device));
         pre.putString("build_hardware", this.getEditText(R.id.build_hardware));
         pre.putString("build_host", this.getEditText(R.id.build_host));
         pre.putString("build_display", this.getEditText(R.id.build_display));
         pre.putString("build_product", this.getEditText(R.id.build_product));
         pre.putString("build_bootloader", this.getEditText(R.id.build_bootloader));
         pre.putString("build_fingerprint", this.getEditText(R.id.build_fingerprint));
         pre.putString("build_cpu_abi_1", this.getEditText(R.id.build_cpu_abi_1));
         pre.putString("build_cpu_abi_2", this.getEditText(R.id.build_cpu_abi_2));
         pre.putString("build_radio", this.getEditText(R.id.build_radio));
         pre.putString("build_time", this.getEditText(R.id.build_time));
         pre.putString("build_type", this.getEditText(R.id.build_type));
         pre.putString("build_tags", this.getEditText(R.id.build_tags));
         pre.putString("build_version_codes_base", this.getEditText(R.id.build_version_codes_base));
         pre.putString("build_version_sdk", this.getEditText(R.id.build_version_sdk));
         pre.putString("build_version_sdk_int", this.getEditText(R.id.build_version_sdk_int));
         pre.putString("build_version_release", this.getEditText(R.id.build_version_release));
         pre.putString("build_version_codename", this.getEditText(R.id.build_version_codename));
         pre.putString("build_version_incremental", this.getEditText(R.id.build_version_incremental));
         pre.putString("build_id", this.getEditText(R.id.build_id));
         pre.putString("build_user", this.getEditText(R.id.build_user));
         pre.putString("build_supported_abis", this.getEditText(R.id.build_supported_abis));

         pre.putString("networkOperator", this.getEditText(R.id.networkOperator));
         pre.putString("networkOperatorName", this.getEditText(R.id.networkOperatorName));
         pre.putString("networkCountryIso", this.getEditText(R.id.networkCountryIso));
         pre.putString("simSerialNum", this.getEditText(R.id.simSerialNum));
         pre.putString("imsi", this.getEditText(R.id.imsi));
         pre.putString("mcc", this.getEditText(R.id.mcc));
         pre.putString("mnc", this.getEditText(R.id.mnc));
         pre.putString("simOperator", this.getEditText(R.id.simOperator));
         pre.putString("simOperatorName", this.getEditText(R.id.simOperatorName));
         pre.putString("imei", this.getEditText(R.id.imei));
         pre.putString("tel", this.getEditText(R.id.tel));
         pre.putString("softwareVersion", this.getEditText(R.id.softwareVersion));
         pre.putString("simCountryIso", this.getEditText(R.id.simCountryIso));

         pre.putString("baseband", this.getEditText(R.id.baseband));
         pre.putString("ANDROID_ID", this.getEditText(R.id.ANDROID_ID));

         pre.putString("wifimac", this.getEditText(R.id.wifimac));
         pre.putString("ip", this.getEditText(R.id.ip));
         pre.putString("bssid", this.getEditText(R.id.bssid));
         pre.putString("ssid", this.getEditText(R.id.ssid));

         pre.putString("sensor", this.getEditText(R.id.sensor));
         pre.putString("installedApp", this.getEditText(R.id.installedApp));
         pre.putString("runningApp", this.getEditText(R.id.runningApp));
         pre.putString("proc_version", this.getEditText(R.id.proc_version));
         pre.putString("proc_mounts", this.getEditText(R.id.proc_version));
         pre.putString("proc_filesystems", this.getEditText(R.id.proc_filesystems));
         pre.putString("proc_diskstats", this.getEditText(R.id.proc_diskstats));
         pre.putString("proc_cpuinfo_model_name", this.getEditText(R.id.proc_cpuinfo_model_name));
         pre.putString("system_etc", this.getEditText(R.id.system_etc));
         pre.putString("dev", this.getEditText(R.id.dev));
         pre.putString("system_bin", this.getEditText(R.id.system_bin));
         pre.putString("system_lib", this.getEditText(R.id.system_lib));
         pre.putString("path", this.getEditText(R.id.path));

         pre.apply();
     }catch (Throwable e) {
         Log.d(TAG, "save data failed");
         e.printStackTrace();
     }
    }

    private void addClick(int... ids) {
        if (ids != null) {
            for (int id : ids) {
             this.findViewById(id).setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rimei:
                setEditText(R.id.imei, RandomDataUtils.randomNum(15));
                break;
            case R.id.rimsi:
                setEditText(R.id.imsi, RandomDataUtils.randomNum(15));
                break;
            case R.id.rtel:
                setEditText(R.id.tel, RandomDataUtils.randomPhone());
                break;
            case R.id.rsimSerialNum:
                setEditText(R.id.simSerialNum, RandomDataUtils.randomNum(20));
                break;
            case R.id.rwifimac:
                setEditText(R.id.wifimac, RandomDataUtils.randomMac());
                break;
            case R.id.rbluetoothmac:
                setEditText(R.id.bluetoothmac, RandomDataUtils.randomMac1());
                break;
            case R.id.rANDROID_ID:
                setEditText(R.id.ANDROID_ID, RandomDataUtils.randomABC(16));
                break;
        }
    }

    public String getScreenDensity_ByResources() {
        DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        float density = mDisplayMetrics.density;
        int densityDpi = mDisplayMetrics.densityDpi;
        String screenDensity = "";
        screenDensity += "["+width+"x"+height+"][density="+density+"][densityDpi="+densityDpi+"]";
        //screenDensity += "Screen mDisplayMetrics: "+mDisplayMetrics;
        //Log.d(TAG,"Screen Ratio: ["+width+"x"+height+"],density="+density+",densityDpi="+densityDpi);
        //Log.d(TAG,"Screen mDisplayMetrics: "+mDisplayMetrics);
        return screenDensity;
    }

    public void getScreenDensity_ByWindowManager(){
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();//��Ļ�ֱ�������
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        float density = mDisplayMetrics.density;
        int densityDpi = mDisplayMetrics.densityDpi;
        //Log.d(TAG,"Screen Ratio: ["+width+"x"+height+"],density="+density+",densityDpi="+densityDpi);
        //Log.d(TAG,"Screen mDisplayMetrics: "+mDisplayMetrics);
    }
}
