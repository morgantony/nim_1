package com.bhm.sdk.rxlibrary.rxjava;

import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack;
import com.bhm.sdk.rxlibrary.utils.RxUtils;

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
    private RxBuilder rxBuilder;

    public UpLoadRequestBody(RequestBody requestBody, RxBuilder builder) {
        this.mRequestBody = requestBody;
        this.rxBuilder = builder;
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

        CountingSink mCountingSink = new CountingSink(sink);
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
            if (null != rxBuilder && null != rxBuilder.getListener() &&
                    rxBuilder.getListener() instanceof RxUpLoadCallBack) {
                if (bytesWritten == 0) {
                    rxBuilder.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rxBuilder.getListener().onStart();
                            RxUtils.Logger(rxBuilder, "upLoad-- > ", "begin upLoad");
                        }
                    });
                }
                bytesWritten += byteCount;
                final int progress = (int) (bytesWritten * 100 / contentLength());
                rxBuilder.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            rxBuilder.getListener().onProgress(progress, bytesWritten, contentLength());
                        } catch (IOException e) {
                            rxBuilder.getListener().onFail(e.getMessage());
                        }
                    }
                });
            }
        }
    }
}
