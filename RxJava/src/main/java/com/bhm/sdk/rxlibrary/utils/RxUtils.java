package com.bhm.sdk.rxlibrary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by bhm on 2018/11/26.
 */

public class RxUtils {

    /**
     * 将输入流写入文件
     *
     * @param inputString
     * @param filePath
     * @param fileName
     * @param mIsDeleteOldFile
     */
    public static void writeFile(InputStream inputString, String filePath, String fileName,
                                 boolean mIsDeleteOldFile) throws Exception {
        File fileDir = new File(filePath);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        File file;
        if(filePath.endsWith("/")) {
            file = new File(filePath + fileName);
        }else{
            file = new File(filePath + "/" + fileName);
        }
        if (file.exists() && mIsDeleteOldFile) {
            file.delete();
        }
        FileOutputStream fos = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int len;
        while ((len = inputString.read(b)) != -1) {
            fos.write(b,0,len);
        }
        inputString.close();
        fos.close();
    }
}
