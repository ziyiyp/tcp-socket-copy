package com.founder.socketcopy.session;

import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Logger log = Logger.getLogger(SessionManager.class);
    private static volatile SessionManager instance = null;
    // netty生成的sessionID和Session的对应关系
    private Map<String, Session> sessionIdMap;
    // 客户端sessionID和服务端sessionID对应关系
    private Map<String, String> sessionListMap;
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public SessionManager() {
        this.sessionIdMap = new ConcurrentHashMap<String, Session>();
        this.sessionListMap = new ConcurrentHashMap<String, String>();
    }

    public boolean containsKey(String sessionId) {
        return sessionIdMap.containsKey(sessionId);
    }

    public boolean containsSession(Session session) {
        return sessionIdMap.containsValue(session);
    }

    public Session findByChannel(Channel channel) {
        return findBySessionId(Session.buildId(channel));
    }

    public Session findBySessionId(String id) {
        return sessionIdMap.get(id);
    }

    public Session findByClientChannel(Channel channel) {
        return findByClientSessionId(Session.buildId(channel));
    }

    public Session findByClientSessionId(String id) {
        String serverSessionId = sessionListMap.get(id);
        if(serverSessionId == null){
            serverSessionId = "";
        }
        Session session = sessionIdMap.get(serverSessionId);
        return session;
    }

    /**
     * 主要用于服务端将session存入内存
     * @param key sessionId
     * @param value session
     * @return
     */
    public synchronized Session put(String key, Session value) {
        return sessionIdMap.put(key, value);
    }

    /**
     * 主要用户客户端将session存入内存
     * @param key 客户端sessionId
     * @param value 客户端session
     * @param sessionIdServer 服务端sessionId
     * @return
     */
    public synchronized Session put(String key, Session value, String sessionIdServer) {
        Session session = sessionIdMap.put(key, value);
        sessionListMap.put(key, sessionIdServer);
        Session serverSession = findBySessionId(sessionIdServer);
        if(serverSession != null){
            //将客户端session存入服务端session对象中
            List<String> sessionIdList = serverSession.getSessionIdList();
            if(sessionIdList == null){
                sessionIdList = new ArrayList<String>();
            }
            sessionIdList.add(key);
            serverSession.setSessionIdList(sessionIdList);
        }else{
            for(Map.Entry<String, String> m :sessionListMap.entrySet()) {
                if (m.getValue().equals(sessionIdServer)) {
                    sessionListMap.remove(m.getKey());
                    sessionIdMap.remove(m.getKey());
                }
            }
        }

        return session;
    }

    /**
     * 主要用户服务端将session移除内存
     * @param sessionId 服务端sessionId
     */
    public synchronized void remove(String sessionId) {
        if (sessionId == null){
            return;
        }
        Session session = sessionIdMap.remove(sessionId);
        if (session == null){
            return;
        }
        try {
            if (session.getChannel() != null) {
                if (session.getChannel().isActive() || session.getChannel().isOpen()) {
                    session.getChannel().close();
                }
            }
            if (session.getSessionIdList() != null) {
                for(String s:session.getSessionIdList()){
                    Session clientSession = findBySessionId(s);
                    if (clientSession.getChannel().isActive() || clientSession.getChannel().isOpen()) {
                        clientSession.getChannel().close();
                    }
                    clientSession.getChannel().eventLoop().shutdownGracefully();
                    clientSession.getChannel().eventLoop().parent().shutdownGracefully();
                    sessionListMap.remove(s);
                    sessionIdMap.remove(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("关闭后当前Id数："+sessionIdMap.size()+"====List数："+sessionListMap.size());
    }

    public Map<String, Session> getSessionIdMap() {
        return sessionIdMap;
    }

    public void setSessionIdMap(Map<String, Session> sessionIdMap) {
        this.sessionIdMap = sessionIdMap;
    }

    public Map<String, String> getSessionListMap() {
        return sessionListMap;
    }

    public void setSessionListMap(Map<String, String> sessionListMap) {
        this.sessionListMap = sessionListMap;
    }
}
