package com.example.douyin_chat.distributed;

import com.example.douyin_chat.constants.ServerConstants;
import com.example.douyin_chat.distributed.zookeeper.CuratorZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;

/**
 * @author : zxm
 * @date: 2024/6/2 - 19:48
 * @Description: 分布式计数器
 * @version: 1.0
 */
@Data
@Slf4j
public class OnlineCounter {
    private static final String PATH = ServerConstants.COUNTER_PATH;

    // Zk客户端
    private CuratorFramework client = null;

    // 单例模式
    private volatile static OnlineCounter singleInstance = null;    // volatile 保证多个线程能够正确地感知实例的创建过程

    DistributedAtomicLong distributedAtomicLong = null;
    private Long curValue;

    public static OnlineCounter getInst()
    {
        if (null == singleInstance)
        {
            synchronized (OnlineCounter.class) {
                if (null == singleInstance) {
                    singleInstance = new OnlineCounter();
                    singleInstance.client = CuratorZKclient.instance.getClient();
                    singleInstance.init();
                }
            }
        }
        return singleInstance;
    }

    private void init(){
        // 分布式计数器，失败时重试10次，每次间隔30ms
        distributedAtomicLong = new DistributedAtomicLong(client, PATH, new RetryNTimes(10, 30));
    }

    private OnlineCounter()
    {

    }

    /**
     * 增加计数
     */
    public boolean increment(){
        boolean result = false;
        AtomicValue<Long> val = null;
        try{
            val = distributedAtomicLong.increment();
            result = val.succeeded();
            log.info("old cnt:{}, new cnt:{}, result:{}",val.preValue(),val.postValue(), val.succeeded());
            curValue = val.postValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 减少计数
     */
    public boolean decrement(){
        boolean result = false;
        AtomicValue<Long> val = null;
        try{
            val = distributedAtomicLong.decrement();
            result = val.succeeded();
            log.info("old cnt:{}, new cnt:{}, result:{}",val.preValue(),val.postValue(), val.succeeded());
            curValue = val.postValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
