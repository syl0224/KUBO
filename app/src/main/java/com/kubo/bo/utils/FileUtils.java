package com.kubo.bo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.os.Environment;
import android.util.Log;

public class FileUtils {
    private static final String TAG = "xxxxxx";
	public static void writeInfoTosdcard(String info_sd) {
		Log.d(TAG, "writeInfoTosdcard......");
    	//String info_sd = "";
    	//info_sd = phoneInfo + imei+ systemInfo + procInfo;
    	File file_info = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/info");
    	if (file_info.exists())
            file_info.delete();

        //Create the new file
        try {
            file_info.createNewFile();
            FileOutputStream fos = new FileOutputStream(file_info);
            Writer os = new OutputStreamWriter(fos, "GBK");
            os.write(info_sd);
            os.flush();
            fos.close();
        }catch (IOException e) {          
            throw new IllegalStateException("Failed to create " + file_info.toString());
        } 
    }

}
