package com.founder.socketcopy.server;

import com.founder.socketcopy.client.Client;
import com.founder.socketcopy.session.Session;
import com.founder.socketcopy.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.util.List;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(ServerHandler.class);
    private SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        try {
            //将消息转发给启动的客户端
            Session session = sessionManager.findByChannel(ctx.channel());
            List<String> sessionIdList = session.getSessionIdList();
            if(sessionIdList != null){
                //判断所有通道是否已经激活，如果未激活需要等待
                int index = 0;//重试次数，防止进入死循环
                boolean isActive = true;
                while (true && index<25){
                    isActive = true;
                    for(String sessionId:sessionIdList){
                        Session clientSession = sessionManager.findBySessionId(sessionId);
                        if(clientSession != null){
                            Channel channel = clientSession.getChannel();
                            isActive = isActive && channel.isActive();
                        }
                    }
                    if(isActive){
                        break;
                    }
                    Thread.sleep(200);
                    index ++;
                }
                if(!isActive){
                    log.error("通道尚未开启");
                    ctx.channel().close();
                }else {
                    for(String sessionId:sessionIdList){
                        Session clientSession = sessionManager.findBySessionId(sessionId);
                        if(clientSession != null){
                            buf.retain();
                            Channel channel = clientSession.getChannel();
                            if(channel.isActive()){
                                ChannelFuture future = channel.writeAndFlush(buf).sync();
                                if (!future.isSuccess()) {
                                    log.error("发送数据出错:{}", future.cause());
                                }
                            }
                        }
                    }
                }

            }
        }catch (Exception e){
            log.error("服务端处理信息异常", e);
        }finally {
            buf.release();
        }
    }

    /**
     * 新客户端接入
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session session = Session.buildSession(ctx.channel());
        String sessionIdServer = session.getId();
        sessionManager.put(sessionIdServer, session);
        new Client(sessionIdServer).run();
    }

    /**
     * 客户端断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.remove(Session.buildId(ctx.channel()));
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常", cause);
    }
}
