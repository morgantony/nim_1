package com.bhm.sdk.rxlibrary.rxjava;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by bhm on 2018/5/28.
 */

public class UpLoadRequestBody extends RequestBody {
    private RequestBody mRequestBody;
    private RxUpLoadListener mUploadListener;
    private CountingSink mCountingSink;

    public UpLoadRequestBody(RequestBody requestBody, RxUpLoadListener uploadListener) {
        mRequestBody = requestBody;
        mUploadListener = uploadListener;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return mRequestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;

        mCountingSink = new CountingSink(sink);
        bufferedSink = Okio.buffer(mCountingSink);

        mRequestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if(bytesWritten == 0) {
                mUploadListener.onStartUpload();
            }
            bytesWritten += byteCount;
            mUploadListener.onProgress(bytesWritten, contentLength());
        }
    }
}
