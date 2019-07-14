package com.kubo.bo.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;

public class CmdUtils {
	public static String execCommand(String command) throws IOException// throws IOException 
    {
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command);
		try {
			if (proc.waitFor() != 0) {
				System.err.println("command = " + command);
				System.err.println("exit value = " + proc.exitValue());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			StringBuffer stringBuffer = new StringBuffer();
			String line = null;
			while ((line = in.readLine()) != null) {
				stringBuffer.append("{ "+line+" }\n");
			}
			//Log.d("xxxxxx", stringBuffer.toString());
			return stringBuffer.toString();

		} catch (InterruptedException e) {
			System.err.println(e);
		}
		return "";
	}
	
	public static String excuteSuCMD(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec("/system/droid4x/bin/su");	//system/xbin/su
			DataOutputStream dos = new DataOutputStream((OutputStream) process.getOutputStream());
			dos.writeBytes((String) "export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
			dos.flush();
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			process.waitFor();
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));
			String sb = "";
			String line;
			while ((line = reader.readLine()) != null) {
				sb += "{ " + line + " }\n";
			}
			return sb;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
