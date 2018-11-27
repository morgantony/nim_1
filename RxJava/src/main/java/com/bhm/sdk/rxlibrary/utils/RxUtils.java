package com.bhm.sdk.rxlibrary.utils;

import android.text.TextUtils;
import android.util.Log;

import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bhm on 2018/11/26.
 */

public class RxUtils {

    /**
     * 将输入流写入文件
     *
     * @param inputString
     * @param rxBuilder
     */
    public static synchronized void writeFile(InputStream inputString, RxBuilder rxBuilder) throws IOException {
        FileOutputStream fos = new FileOutputStream(checkFile(rxBuilder), true);
        byte[] b = new byte[1024];
        int len;
        while ((len = inputString.read(b)) != -1) {
            fos.write(b,0,len);
        }
        fos.flush();
        inputString.close();
        fos.close();
    }

    /** 下载前，如果不支持断点下载，或者已经下载过，那会把已下载的文件删除
     * @param rxBuilder
     * @param contentLength
     * @throws IOException
     */
    public static synchronized void deleteFile(RxBuilder rxBuilder, long contentLength) throws IOException {
        File file = checkFile(rxBuilder);
        if(file.exists()){
            if(!rxBuilder.isAppendWrite() || file.length() >= contentLength){
                file.delete();
            }
        }
    }

    /** 生成文件
     * @param rxBuilder
     * @return
     * @throws IOException
     */
    private static synchronized File checkFile(RxBuilder rxBuilder) throws IOException{
        if(TextUtils.isEmpty(rxBuilder.getFilePath()) || TextUtils.isEmpty(rxBuilder.getFileName())){
            throw new IOException("filePath or fileName is null!");
        }
        File fileDir = new File(rxBuilder.getFilePath());
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        File file;
        if(rxBuilder.getFilePath().endsWith("/")) {
            file = new File(rxBuilder.getFilePath() + rxBuilder.getFileName());
        }else{
            file = new File(rxBuilder.getFilePath() + "/" + rxBuilder.getFileName());
        }
        return file;
    }

    public static void Logger(RxBuilder rxBuilder, String tag, String msg){
        if(rxBuilder.isLogOutPut()){
            Log.e(tag, msg);
        }
    }
}
