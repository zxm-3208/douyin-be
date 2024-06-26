package com.example.douyin_chat.client.service.impl;

import com.example.douyin_chat.client.client.ClientSession;
import com.example.douyin_chat.client.client.NettyClient;
import com.example.douyin_chat.client.domain.vo.ChatUser;
import com.example.douyin_chat.client.domain.vo.SendChat;
import com.example.douyin_chat.client.sender.ChatSender;
import com.example.douyin_chat.client.sender.LoginSender;
import com.example.douyin_chat.client.service.ClientService;
import com.example.douyin_chat.cocurrent.FutureTaskScheduler;
import com.example.douyin_chat.distributed.ImLoadBalance;
import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.entity.ImNode;
import com.example.douyin_chat.entity.LoginBack;
import com.example.douyin_chat.util.JsonUtil;
import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/6/7 - 17:08
 * @Description: com.example.douyin_chat.client.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ImLoadBalance imLoadBalance;

    @Autowired
    private NettyClient nettyClient;

    @Autowired
    private LoginSender loginSender;

    @Value("${token.secret}")
    private String secret;

    @Autowired
    private ChatSender chatSender;

    private int reConnectCount = 0;
    private boolean connectFlag = false;
    private ChatUserDTO user;
    private Channel channel;
    private ClientSession session;

    // TODO: 该线程是否循环
    @Override
    public BaseResponse login(ChatUser userVO) {
        Thread.currentThread().setName("命令线程");
        if(connectFlag == false){
            ChatUserDTO user = new ChatUserDTO();
            user.setUserId(userVO.getUserName());
            user.setToken(userVO.getToken());
            user.setDevId(userVO.getDevId());
            user.setUserName(userVO.getUserName());

            log.info("step1: 开始登录WEB GATE");
            LoginBack back = new LoginBack();
            /**
             * 取得所有的节点
             */
            List<ImNode> allWorker = imLoadBalance.getWorkers();
            back.setImNodeList(allWorker);
            back.setUserDTO(user);
            back.setToken(userVO.getToken());

            //获取服务器节点信息，选择负载最低的节点连接(客户端选择，选择的节点可能已经挂掉，但是zk超时的原因节点信息还在，连接不了，需要重试其他节点)
            List<ImNode> nodeList = back.getImNodeList();
            log.info("step1 zookeeper中的node节点列表是：{}", JsonUtil.pojoToJson(nodeList));

            log.info("step2：开始连接Netty 服务节点");
            ImNode bestNode = null;
            if (nodeList.size() > 0)
            {
                // 根据balance值由小到大排序
                Collections.sort(nodeList);

            } else
            {
                log.error("step2-1：服务器节点为空，无法连接");
            }
            nettyClient.setConnectedListener(connectedListener);

            for(int i=0;i<nodeList.size();i++){
                // 返回balance值最小的那个
                bestNode = nodeList.get(i);
                log.info("尝试连接最佳的节点:{}", JsonUtil.pojoToJson(bestNode));
                nettyClient.setHost(bestNode.getHost());
                nettyClient.setPort(bestNode.getPort());
                nettyClient.doConnect();
                waitCommandThread();
                if (connectFlag) {
                    break;
                }
                if (i == nodeList.size()) {
                    log.error("尝试所有节点连接失败");
                    return BaseResponse.fail("连接失败");
                }
            }
            log.info("step2：Netty 服务节点连接成功");

            log.info("step3：开始登录Netty 服务节点");
            this.user = user;
            session.setUser(user);
            loginSender.setUser(user);
            loginSender.setSession(session);
            loginSender.sendLoginMsg();
            waitCommandThread();

            connectFlag = true;
        }
        return BaseResponse.success("登录成功！");
    }

    public synchronized void waitCommandThread()
    {

        //休眠，命令收集线程
        try
        {
            this.wait();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    public void startConnectServer()
    {
        FutureTaskScheduler.add(() ->
        {
            nettyClient.setConnectedListener(connectedListener);
            nettyClient.doConnect();
        });
    }

    @Override
    public void setConnectFlag(Boolean x) {
        connectFlag = x;
    }

    @Override
    public void sendChat(SendChat sendChat) {
        if (null == session)
        {
            log.info("session is null");
        }
        chatSender.setSession(session);
        chatSender.setUser(user);
        chatSender.sendChatMsg(sendChat.getToUserId(), sendChat.getMessage());
    }

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) ->{
        log.info("{}：连接已经断开", new Date());
        channel = f.channel();

        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.close();

        //唤醒用户线程
        notifyCommandThread();
    };

    GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f)->{
        final EventLoop eventLoop = f.channel().eventLoop();
        if(!f.isSuccess() && ++reConnectCount<3){
            log.info("连接失败！在10秒之后尝试第{}次重连",reConnectCount);
            eventLoop.schedule(()->nettyClient.doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        }else if(f.isSuccess()){
            connectFlag = true;
            log.info("IM服务器连接成功");
            channel = f.channel();

            // 创建会话
            session = new ClientSession(channel);
            session.setConnected(true);
            channel.closeFuture().addListener(closeListener);

            // 唤醒用户线程
            notifyCommandThread();
        }else{
            log.info("IM服务器多次连接失败！");
            connectFlag = false;
            // 唤醒用户线程
            notifyCommandThread();
        }
    };

    public synchronized void notifyCommandThread()
    {
        //唤醒，命令收集程
        this.notify();
    }

}
