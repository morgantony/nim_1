package com.bhm.sdk.demo.tools;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhm on 2018/5/29.
 */

public class Utils {

    public static File getFile(){
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "RxLibrary/1122.jpg");
    }

    public static List<File> getFiles(){
        List<File> files = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "RxLibrary/1122.jpg");
        files.add(file);
        files.add(file);
        files.add(file);
        files.add(file);
        return files;
    }
}
