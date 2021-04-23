package com.founder.socketcopy.client;

import com.founder.socketcopy.session.Session;
import com.founder.socketcopy.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ClientHandler.class);
    private SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        //对应服务端session
        Session sessionServer = sessionManager.findByClientChannel(ctx.channel());
        if(sessionServer != null){
            sessionServer.getChannel().writeAndFlush(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session clientSession = sessionManager.findByChannel(ctx.channel());
        String ip = "";
        if(clientSession != null){
            ip = clientSession.getIpPort().getIp();
        }
//        log.debug(ip+"客户端通道关闭");
        Session serverSession = sessionManager.findByClientChannel(ctx.channel());
        if(serverSession != null){
            sessionManager.remove(serverSession.getId());
        }
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Session clientSession = sessionManager.findByChannel(ctx.channel());
        log.error(clientSession.getIpPort().getIp()+"异常",cause);
    }

}
