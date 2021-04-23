package com.founder.socketcopy.session;

import com.founder.socketcopy.dto.IpPort;
import io.netty.channel.Channel;

import java.util.List;

public class Session {
    private String id;
    private Channel channel;
    private IpPort ipPort;
    private List<String> sessionIdList;

    public Session() {
    }

    public static String buildId(Channel channel) {
        return channel.id().asLongText();
    }

    public static Session buildSession(Channel channel){
        Session session = new Session();
        session.setChannel(channel);
        session.setId(buildId(channel));
        return session;
    }

    public static Session buildSession(Channel channel, IpPort ipPort){
        Session session = new Session();
        session.setChannel(channel);
        session.setId(buildId(channel));
        session.setIpPort(ipPort);
        return session;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public IpPort getIpPort() {
        return ipPort;
    }

    public void setIpPort(IpPort ipPort) {
        this.ipPort = ipPort;
    }

    public List<String> getSessionIdList() {
        return sessionIdList;
    }

    public void setSessionIdList(List<String> sessionIdList) {
        this.sessionIdList = sessionIdList;
    }
}
