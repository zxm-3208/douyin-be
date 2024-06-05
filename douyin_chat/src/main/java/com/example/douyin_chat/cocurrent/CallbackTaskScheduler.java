package com.example.douyin_chat.cocurrent;

import com.example.douyin_chat.util.ThreadUtil;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.handler.codec.http2.Http2NoMoreStreamIdsException;
import lombok.extern.slf4j.Slf4j;

import javax.security.auth.callback.Callback;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : zxm
 * @date: 2024/6/5 - 17:33
 * @Description: com.example.douyin_chat.cocurrent
 * @version: 1.0
 */
@Slf4j
public class CallbackTaskScheduler {

    static ListeningExecutorService gPool = null;

    static{
        ExecutorService jPool = ThreadUtil.getMixedTargetThreadPool();
        gPool = MoreExecutors.listeningDecorator(jPool);        // 包装成ListeningExecutorService
    }

    private CallbackTaskScheduler(){

    }

    /**
     * 添加任务
     */
    public static <R> void add(CallbackTask<R> executeTask){
        ListenableFuture<R> future = gPool.submit(new Callable<R>() {
            public R call() throws Exception {

                R r = executeTask.execute();
                return r;
            }

        });

        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                executeTask.onBack(r);
            }

            public void onFailure(Throwable t) {
                executeTask.onException(t);
            }
        });

    }

}
