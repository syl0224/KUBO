package com.kubo.bo.xposed;

import java.util.HashMap;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class MainHook implements IXposedHookLoadPackage {
	private static boolean init = false;
	private static boolean first_open = true;  //model first open
	private static final String TAG = "xxxxxx";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		Log.d(TAG, "handleLoadPackage");
		if("".equals(lpparam.packageName)) {	//system
			Log.d(TAG, "system skip");
			return;
		}
		if("android".equals(lpparam.packageName)) { //android
			Log.d(TAG, "android skip");
			return;
		}

		String pack[] = {"com.android.launcher",
				"com.android.providers.settings",
				"com.android.keychain",
				"me.haima.androidassist",
				"com.android.defcontainer",
				"com.svox.pico",
				"com.android.systemui",
				"com.android.providers.contacts",
				"com.android.providers.applications",
				"com.example.android.softkeyboard",
				"com.android.providers.userdictionary",
				"com.google.android.location",
				"com.android.location.fused",
				"com.android.phone",
				"com.google.android.gsf",
				"com.google.android.syncadapters.contacts",
				"eu.chainfire.supersu",
				"com.android.vending",
				"com.google.android.gms",
				"com.android.process.gallery3d",
				"com.haimawan.push",
				"com.android.smspush",
				"com.android.settings",
				"com.android.providers.telephony",
				"com.android.providers.drm",
				"com.android.providers.media",
				"com.android.providers.downloads",

				"de.robv.android.xposed.installer"};
		for (String p : pack) {
			if (p.equals(lpparam.packageName)) {
				Log.d(TAG, lpparam.packageName+" skip");
				return;
			}
		}

		readData(lpparam);
	}

	/**
	 * read SharedPreferences
	 */
	private void readData(LoadPackageParam lpparam) {
		if (init) {
			Log.d(TAG, "sharedpreferences null");
			return;
		}

		Log.d(TAG, "init " + init);
		try {
			String packageName_xposed = this.getClass().getPackage().getName();//com.kubo.bo.xposed
			Log.d(TAG, "packageName_xposed:" + packageName_xposed);
			String[] str = packageName_xposed.split("\\.");
			String packageName_kubo = str[0]+"."+str[1]+"."+str[2];    //com.kubo.bo
			Log.d(TAG, "packageName_kubo:" + packageName_kubo);


			XSharedPreferences pre = new XSharedPreferences(packageName_kubo, "prefs");

			if (!pre.getFile().exists()) {
				Log.d(TAG, "prefs.xml doesn't exist...exit");
				return;
			}
			Log.d(TAG, "prefs getFile:"+pre.getFile().toString()+" exists?"+pre.getFile().exists());
			Log.d(TAG, "getName: " +this.getClass().getPackage().getName());
			Log.d(TAG, "pres.xml:"+pre.toString());
			String ks[] = {"build_model", "build_manufacturer", "build_board", "build_serial", "build_brand",
					"build_device", "build_hardware", "build_host", "build_display", "build_product", "build_bootloader",
					"build_fingerprint", "build_cpu_abi_1", "build_cpu_abi_2", "build_radio", "build_time", "build_type",
					"build_tags", "build_version_codes_base", "build_version_sdk", "build_version_sdk_int",
					"build_version_release", "build_version_codename", "build_version_incremental", "build_id", "build_user",
					"build_supported_abis",
					"networkOperator", "networkOperatorName", "networkCountryIso", "simSerialNum", "imsi", "simOperator",
					"simOperatorName", "imei", "tel", "softwareVersion", "simCountryIso",
					"wifimac", "ip", "bssid", "ssid",
					"sensor", "installedApp", "ANDROID_ID"};   //"baseband"   runningApp
			HashMap<String, String> maps = new HashMap<String, String>();
			for (String k : ks) {
				String v = pre.getString(k, null);
				Log.d(TAG+"readData", "key=" + k + " value=" + v);
				maps.put(k, v);
				if (TextUtils.isEmpty(v)) {
					Log.d(TAG, lpparam.packageName + " read shared preferences failed: " + k + " is NULL");
					continue;
				}
			}
			if (maps.isEmpty()) {
				Log.d(TAG, "{" + lpparam.packageName + "}read shared preferences failed: SharedPreferences is NULL");
			} else {
				HookAll(maps);
			}

		}catch (Throwable e) {
			Log.d(TAG, "{" + lpparam.packageName + "}read sharedpreferences failed" + e.getMessage());
			//Toast.makeText(this, lpparam.packageName + "->" + Thread.currentThread().getStackTrace()[2].getMethodName() + " "
			//    + e.getStackTrace(), Toast.LENGTH_LONG).show();
		}
		//first_open = false;
	}

	private void HookAll(final HashMap<String, String> map) {
		try {
			XposedHelpers.findField(android.os.Build.class, "MODEL").set(null, map.get("build_model"));
			XposedHelpers.findField(android.os.Build.class, "MANUFACTURER").set(null, map.get("build_manufacturer"));
			XposedHelpers.findField(android.os.Build.class, "BOARD").set(null, map.get("build_board"));
			XposedHelpers.findField(android.os.Build.class, "SERIAL").set(null, map.get("build_serial"));
			XposedHelpers.findField(android.os.Build.class, "BRAND").set(null, map.get("build_brand"));
			XposedHelpers.findField(android.os.Build.class, "DEVICE").set(null, map.get("build_device"));
			XposedHelpers.findField(android.os.Build.class, "HARDWARE").set(null, map.get("build_hardware"));
			XposedHelpers.findField(android.os.Build.class, "HOST").set(null, map.get("build_host"));
			XposedHelpers.findField(android.os.Build.class, "DISPLAY").set(null, map.get("build_display"));
			XposedHelpers.findField(android.os.Build.class, "PRODUCT").set(null, map.get("build_product"));
			XposedHelpers.findField(android.os.Build.class, "BOOTLOADER").set(null, map.get("build_bootloader"));
			XposedHelpers.findField(android.os.Build.class, "FINGERPRINT").set(null, map.get("build_fingerprint"));
			XposedHelpers.findField(android.os.Build.class, "CPU_ABI").set(null, map.get("build_cpu_abi_1"));
			XposedHelpers.findField(android.os.Build.class, "CPU_ABI2").set(null, map.get("build_cpu_abi_2"));
			XposedHelpers.findField(android.os.Build.class, "RADIO").set(null, map.get("build_radio"));
			XposedHelpers.findField(android.os.Build.class, "TIME").set(null, Integer.parseInt(map.get("build_time")));
			XposedHelpers.findField(android.os.Build.class, "TYPE").set(null, map.get("build_type"));
			XposedHelpers.findField(android.os.Build.class, "TAGS").set(null, map.get("build_tags"));
			XposedHelpers.findField(android.os.Build.class, "VERSION_CODES.BASE").set(null, Integer.parseInt(map.get("build_version_codes_base")));
			XposedHelpers.findField(android.os.Build.class, "VERSION.SDK").set(null, map.get("build_version_sdk"));
			XposedHelpers.findField(android.os.Build.class, "VERSION.SDK_INT").set(null, Integer.parseInt(map.get("build_version_sdk_int")));
			XposedHelpers.findField(android.os.Build.class, "VERSION.RELEASE").set(null, map.get("build_version_release"));
			XposedHelpers.findField(android.os.Build.class, "VERSION.CODENAME").set(null, map.get("build_version_codename"));
			XposedHelpers.findField(android.os.Build.class, "VERSION.INCREMENTAL").set(null, map.get("build_version_incremental"));
			XposedHelpers.findField(android.os.Build.class, "ID").set(null, map.get("build_id"));
			XposedHelpers.findField(android.os.Build.class, "USER").set(null, map.get("build_user"));

		} catch (Throwable e) {
			Log.d(TAG, "modify Build failed!" + e.getMessage());
		}

		try {
			XposedHelpers.findAndHookMethod(
					android.provider.Settings.Secure.class, "getString",
					new Object[] { ContentResolver.class, String.class,
							new XC_MethodHook()
							{
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable
								{
									if (param.args[1] == "android_id") {
										param.setResult(map.get("ANDROID_ID"));
									}
								}
							} });
		} catch (Throwable e) {
			Log.d(TAG, "modify androidid failed!" + e.getMessage());
		}

		HookMethod(TelephonyManager.class, "getDeviceId", map.get("imei"));
		HookMethod(TelephonyManager.class, "getSubscriberId", map.get("imsi"));
		HookMethod(TelephonyManager.class, "getSimSerialNumber", map.get("simSerialNum"));
		HookMethod(TelephonyManager.class, "getSimOperator", map.get("simOperator"));
		HookMethod(TelephonyManager.class, "getSimOperatorName", map.get("simOperatorName"));
		HookMethod(TelephonyManager.class, "getLine1Number", map.get("tel"));
		HookMethod(TelephonyManager.class, "getDeviceSoftwareVersion", map.get("softwareVersion"));
		HookMethod(TelephonyManager.class, "getSimCountryIso", map.get("simCountryIso"));
		HookMethod(TelephonyManager.class, "getNetworkOperator", map.get("networkOperator"));
		HookMethod(TelephonyManager.class, "getNetworkOperatorName", map.get("networkOperatorName"));
		HookMethod(TelephonyManager.class, "getNetworkCountryIso", map.get("networkCountryIso"));

		HookMethod(WifiInfo.class, "getMacAddress", map.get("wifimac"));
		HookMethod2(WifiInfo.class, "getIpAddress", Integer.parseInt(map.get("ip")));
		HookMethod(WifiInfo.class, "getBSSID", map.get("bssid"));
		HookMethod(WifiInfo.class, "getSSID", map.get("ssid"));

		HookMethod(SensorManager.class, "getSensorList", map.get("sensor"));
		HookMethod(PackageManager.class, "getInstalledPackages", map.get("installedApp"));
		//HookMethod(ActivityManager.class, "getRunningAppProcesses", map.get("runningApp"));
	}

	private void HookMethod (final Class cl, final String method, final String result) {
		try {
			XposedHelpers.findAndHookMethod(cl, method, new Object[] { new XC_MethodHook()
			{
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					param.setResult(result);
				}
			} });
		} catch (Throwable e) {
			Log.d(TAG, "modify " + method + " failed!" + e.getMessage());
		}
	}

	//value type is int
	private void HookMethod2 (final Class cl, final String method, final int result) {
		try {
			XposedHelpers.findAndHookMethod(cl, method, new Object[] { new XC_MethodHook()
			{
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					param.setResult(result);
				}
			} });
		} catch (Throwable e) {
			Log.d(TAG, "modify " + method + " failed!" + e.getMessage());
		}
	}
}
