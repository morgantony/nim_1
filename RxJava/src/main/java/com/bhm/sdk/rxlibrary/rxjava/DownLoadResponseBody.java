package com.bhm.sdk.rxlibrary.rxjava;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/** 下载请求体
 * Created by bhm on 2018/5/11.
 */

public class DownLoadResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private RxDownLoadListener downloadListener;
    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private BufferedSource bufferedSource;

    public DownLoadResponseBody(ResponseBody responseBody, RxDownLoadListener downloadListener) {
        this.responseBody = responseBody;
        this.downloadListener = downloadListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (null != downloadListener) {
                    if(totalBytesRead == 0 && bytesRead != -1) {
                        downloadListener.onStartDownload();
                    }
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (bytesRead != -1) {
                        int progress = (int) (totalBytesRead * 100 / responseBody.contentLength());
                        downloadListener.onProgress(progress);
                        if(totalBytesRead == responseBody.contentLength()){
                            downloadListener.onProgress(100);
                        }
                    }
                }
                return bytesRead;
            }
        };
    }
}
