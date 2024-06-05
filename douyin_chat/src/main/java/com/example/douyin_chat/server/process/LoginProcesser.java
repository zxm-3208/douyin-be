package com.example.douyin_chat.server.process;

import com.example.douyin_chat.entity.ChatUserDTO;
import com.example.douyin_chat.protocol.bean.ProtoMsgOuterClass;
import com.example.douyin_chat.protocol.constant.ProtoInstant;
import com.example.douyin_chat.protocol.protoBuilder.LoginResponceBuilder;
import com.example.douyin_chat.server.handler.LoginRequestHandler;
import com.example.douyin_chat.server.session.LocalSession;
import com.example.douyin_chat.server.session.service.SessionManger;
import com.example.douyin_commons.constant.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/6/5 - 15:39
 * @Description: com.example.douyin_chat.server.process
 * @version: 1.0
 */
@Data
@Slf4j
@Service
public class LoginProcesser extends AbstractServerProcesser{

    @Autowired
    LoginResponceBuilder loginResponceBuilder;
    @Autowired
    SessionManger sessionManger;

    @Value("${token.secret}")
    private String secret;

    @Override
    public ProtoMsgOuterClass.ProtoMsg.HeadType op() {
        return ProtoMsgOuterClass.ProtoMsg.HeadType.LOGIN_REQUEST;
    }

    @Override
    public Boolean action(LocalSession session, ProtoMsgOuterClass.ProtoMsg.Message proto) {
        // 取出token进行验证
        ProtoMsgOuterClass.ProtoMsg.LoginRequest info = proto.getLoginRequest();
        long seqNo = proto.getSequence();

        ChatUserDTO user = ChatUserDTO.fromMsg(info);   // 根据Info内的数据创建user

        // 检查用户
        boolean isValidUser = checkUser(user);

        if(!isValidUser){
            ProtoInstant.ResultCodeEnum resultCodeEnum = ProtoInstant.ResultCodeEnum.NO_TOKEN;
            ProtoMsgOuterClass.ProtoMsg.Message response = loginResponceBuilder.loginResponce(resultCodeEnum, seqNo, "-1");
            // 发现之后断开连接
            session.writeAndClose(response);
            return false;
        }

        session.setUser(user);
        /**
         * 绑定session
         */
        session.bind();
        sessionManger.addLocalSession(session);

        /**
         * 通知客户端：登录成功
         */
        ProtoInstant.ResultCodeEnum resultCodeEnum = ProtoInstant.ResultCodeEnum.SUCCESS;
        ProtoMsgOuterClass.ProtoMsg.Message response = loginResponceBuilder.loginResponce(resultCodeEnum, seqNo, session.getSessionId());
        session.writeAndFlush(response);
        return true;
    }

    private boolean checkUser(ChatUserDTO user){
        String token = user.getToken();
        if(!StringUtil.isEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
            token = token.replace(Constants.TOKEN_PREFIX, "");
        // 判断token是否存在
        if(StringUtil.isBlank(token)){
            return false;
        }
        // 判断token是否有效
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            Long exp = (Long)claims.get(Constants.EXP);
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis > exp){
                log.info("token已过期");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
