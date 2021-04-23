package com.founder.socketcopy.client;

import com.founder.socketcopy.App;
import com.founder.socketcopy.dto.IpPort;
import com.founder.socketcopy.session.Session;
import com.founder.socketcopy.session.SessionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

public class Client {

    private static Logger log = Logger.getLogger(Client.class);
    private SessionManager sessionManager = SessionManager.getInstance();

    private EventLoopGroup worker = new NioEventLoopGroup();
    private String sessionId = "";

    public Client(String sessionId){
        this.sessionId = sessionId;
    }

    public void run() {
        //服务类
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture cf = null;
        try {
            //设置线程池
            bootstrap.group(worker);
            //设置socket工厂
            bootstrap.channel(NioSocketChannel.class);
            //设置管道
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ClientHandler());
                }
            });
            for(IpPort ipPort: App.clientList){
                cf = bootstrap.connect(ipPort.getIp(), ipPort.getPort());
                Session session = Session.buildSession(cf.channel(), ipPort);
                sessionManager.put(session.getId(), session, sessionId);
            }
            log.info("当前Id数："+sessionManager.getSessionIdMap().size()+"====List数："+sessionManager.getSessionListMap().size());
        } catch (Exception e) {
            log.error("客户端启动异常",e);
            if(cf != null){
                Session serverSession = sessionManager.findByClientChannel(cf.channel());
                if(serverSession != null){
                    sessionManager.remove(serverSession.getId());
                }
                cf.channel().close();
            }
        }
    }
}
