package com.kubo.bo.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.os.Debug;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;


public class InfoUtils {
	public static String getSupABIS() {
		//reflect
		String SupABIS = "";
		try {
			Class clazz = Class.forName("android.os.Build");
			Object obj = clazz.newInstance();
			Field field = clazz.getDeclaredField("SUPPORTED_ABIS");
			Field field_32 = clazz.getDeclaredField("SUPPORTED_32_BIT_ABIS");
			Field field_64 = clazz.getDeclaredField("SUPPORTED_64_BIT_ABIS");

			field.setAccessible(true);
			SupABIS += "{ SUPPORTED_ABIS : " + Arrays.toString( (String[]) field.get(obj) ) + " }\n";
			SupABIS += "{ SUPPORTED_32_BIT_ABIS : " + Arrays.toString( (String[]) field_32.get(obj) ) + " }\n";
			SupABIS += "{ SUPPORTED_64_BIT_ABIS : " + Arrays.toString( (String[]) field_64.get(obj) ) + " }\n";
			//Log.d("xxxxxx", (String) field.get(obj));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return "null";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return SupABIS;
	}

	public static String getBaseBandVersion() {
		String version = "";
		try {
			Class clazz= Class.forName("android.os.SystemProperties");
			Object object = clazz.newInstance();
			Method method = clazz.getMethod("get", new Class[]{String.class, String.class});
			Object result = method.invoke(object, new Object[]{"gsm.version.baseband", "unknown"});
			version = (String) result;
		} catch (Exception e) {
		}
		return version;
	}

	public static String getBluetoothMacAddress() {
		//get bluetooth mac
		int i =1;
		BluetoothAdapter btAda = BluetoothAdapter.getDefaultAdapter();
//       if (btAda.isEnabled() == false) {
//            if (btAda.enable()) {
//                while (btAda.getState() == BluetoothAdapter.STATE_TURNING_ON
//                        || btAda.getState() != BluetoothAdapter.STATE_ON) {
//                    try {
//                       i++;
//                       if(i >= 5)
//                          return "off";
//                        Thread.sleep(100);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
		if (btAda.getState() == BluetoothAdapter.STATE_TURNING_ON
				|| btAda.getState() != BluetoothAdapter.STATE_ON) {
			return btAda.getAddress();
		}
		return "off";
	}

	public static String getIpAddress(WifiInfo wifiInfo) {
		int ipAddress = wifiInfo.getIpAddress();
		String ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
				+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
		return ipString;
	}

	public static String getMacAddress(WifiInfo wifiInfo) {
		return wifiInfo.getMacAddress();
	}

	public static String getUserAgent(Context context) {
		WebView webview;
		webview = new WebView(context);
		webview.layout(0, 0, 0, 0);
		WebSettings settings = webview.getSettings();
		String ua = settings.getUserAgentString();
		//Log.e("HJJ", "User Agent:" + ua);
		return ua;
	}

	public static String getSensorInfo(Context context) {
		String sensorInfo = "";
		SensorManager sm = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> allSensors = sm.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s : allSensors)
		{
			sensorInfo += "{ name: " + s.getName() + " | Vendor: " + s.getVendor() + " | Type: " + s.getType()
					+ " | version: " + s.getVersion() + " | power: " + s.getPower() +
					" | resolution: " + s.getResolution() + " }\n";
		}
		return sensorInfo;
	}

	public static String getInstalledApp(Context context) {
		String result = "";
		String labelname, packagename;
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo i : packages) {
			if ((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				labelname = i.applicationInfo.loadLabel(context.getPackageManager()).toString();
				packagename = i.applicationInfo.packageName;
				result += "{ " + labelname + " : " + packagename + " }\n";
			}
		}
		return result.substring(0, result.length() - 1);
	}

	public static String getRunningApp(Context context) {
		String runningApp = "";
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
				mActivityManager.getRunningAppProcesses();
		for (int i = 0; i < runningAppProcesses.size(); i++) {
			ActivityManager.RunningAppProcessInfo info = runningAppProcesses.get(i);
			int pid = info.pid;
			int uid = info.uid;
			String processName = info.processName;
			int[] memoryPid = new int[]{pid};
			Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(memoryPid);
			int memeorySize = memoryInfo[0].getTotalPss();
			runningApp += "{ " + pid + " : " + processName + " }\n";
//       	  AMProcessInfo amProcessInfo = new AMProcessInfo();
//            amProcessInfo.setPid("" + pid);
//            amProcessInfo.setUid("" + uid);
//            amProcessInfo.setProcessName(processName);
//            amProcessInfo.setMemorySize("" + memeorySize);
//            amProcessInfos.add(amProcessInfo);
		}
		return runningApp;
	}

	public static String getCpuInfo() {
		try {
			FileReader fileReader = new FileReader("/proc/cpuinfo");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String info;
			while ((info = bufferedReader.readLine()) != null) {
				String[] array = info.split(":");
				if(array[0].trim().equals("model name")) {
					return "{ " + array[1] + " }";
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getIntentMainAction(Context context) {
		String intent_main_action = "";
		PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm
				.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
		Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
		for (ResolveInfo reInfo : resolveInfos) {
			String activityName = reInfo.activityInfo.name;
			String pkgName = reInfo.activityInfo.packageName;
			String appLabel = (String) reInfo.loadLabel(pm);
			//intent_main_action += "{ ActivityName: " + activityName + ", PckName: " +pkgName+ "}\n";
			intent_main_action += "{ " + pkgName + " }\n";
		}
		return intent_main_action;
	}

	public static String getJavaInfo() {
		String javaInfo = "";
		//java properties
		javaInfo = "java_version: " + System.getProperty("java.version") + "\n";
		javaInfo += "java_vendor: " + System.getProperty("java.vendor") + "\n";
		javaInfo += "java_vendor_url: " + System.getProperty("java.vendor.url") + "\n";
		javaInfo += "java_home: " + System.getProperty("java.home") + "\n";
		javaInfo += "java_class_version: " + System.getProperty("java.class.version") + "\n";
		javaInfo += "java_class_path: " + System.getProperty("java.class.path") + "\n";
		javaInfo += "os_name: " + System.getProperty("os.name") + "\n";
		javaInfo += "os_arch: " + System.getProperty("os.arch") + "\n";
		javaInfo += "os_version: " + System.getProperty("os.version") + "\n";
		javaInfo += "user_name: " + System.getProperty("user.name") + "\n";
		javaInfo += "user_home: " + System.getProperty("user.home") + "\n";
		javaInfo += "user_dir: " + System.getProperty("user.dir") + "\n";
		javaInfo += "java_vm_specification_version: "
				+ System.getProperty("java.vm.specification.version") + "\n";
		javaInfo += "java_vm_specification_vendor: "
				+ System.getProperty("java.vm.specification.vendor") + "\n";
		javaInfo += "java_vm_specification_name: "
				+ System.getProperty("java.vm.specification.name") + "\n";
		javaInfo += "java_vm_version: " + System.getProperty("java.vm.version") + "\n";
		javaInfo += "java_vm_vendor: " + System.getProperty("java.vm.vendor") + "\n";
		javaInfo += "java_vm_name: " + System.getProperty("java.vm.name") + "\n";
		javaInfo += "java_ext_dirs: " + System.getProperty("java.ext.dirs") + "\n";
		javaInfo += "file_separator: " + System.getProperty("file.separator") + "\n";
		javaInfo += "path_separator: " + System.getProperty("path.separator") + "\n";
		javaInfo += "line_separator: " + System.getProperty("line.separator") + "\n";

		return javaInfo;
	}

	public static String getNetworkInterface(Context context) throws SocketException, UnknownHostException {
		String ni_name = "";
		String IP = "";
		Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements()) {
			NetworkInterface ni=e.nextElement();
			Toast.makeText(context, ni.getName(), Toast.LENGTH_LONG);
			ni_name += "{" + ni.getName() + "}\n";
//			ni.getInetAddresses();
//			IP = InetAddress.getLocalHost().getHostAddress();//
//          byte[]mac=ni.getHardwareAddress();
//          if(mac != null) {
//               displayMac(mac);
//          }else {
//               System.out.println("mac is null");
//          }
		}
		return ni_name;
	}

	//unused
	public static void displayMac(byte[] mac) {
		for(int i=0;i<mac.length;i++) {
			byte b=mac[i];
			int intValue=0;
			if(b>=0)
				intValue=b;
			else
				intValue=256+b;
			Log.d("xxxxxx", Integer.toHexString(intValue));
			if(i!=mac.length-1) {
				Log.d("xxxxxx", "-");
			}
		}
		System.out.println();
	}

	public static String getMyCellInfo(TelephonyManager tm) {
		@SuppressLint("MissingPermission")
		List<CellInfo> cellInfoList = tm.getAllCellInfo(); //getAllCellInfo() require at least sdk 17
		//Log.d("xxxxxx empty ", " is "+ b);
		String str = "";
		if( cellInfoList!=null ){  //cellInfoList.size() > 0
			for (CellInfo cellInfo : cellInfoList) {
				Log.d("xxxxxx", cellInfo.toString());
				str += "# " + cellInfo.toString() + " #\n";
			}
			return str;
		}
		return "no cell info\n";
	}

}
