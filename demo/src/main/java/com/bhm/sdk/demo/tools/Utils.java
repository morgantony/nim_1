package com.bhm.sdk.demo.tools;

import android.os.Environment;

import java.io.File;

/**
 * Created by bhm on 2018/5/29.
 */

public class Utils {

    public static File getFile(){
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "RxLibrary/1122.jpg");
    }
}
