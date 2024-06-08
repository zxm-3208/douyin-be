package com.example.douyin_chat_commons.cocurrent;

import com.example.douyin_chat_commons.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : zxm
 * @date: 2024/6/6 - 11:09
 * @Description: com.example.douyin_chat.cocurrent
 * @version: 1.0
 */
@Slf4j
public class FutureTaskScheduler {
    static ThreadPoolExecutor mixPool = null;

    static{
        mixPool = ThreadUtil.getMixedTargetThreadPool();
    }

    private FutureTaskScheduler(){

    }

    /**
     * 添加任务
     */
    public static void add(Runnable executeTask){
        mixPool.submit(()->{executeTask.run();});
    }

}
