package com.founder.socketcopy;

import com.founder.socketcopy.dto.IpPort;
import com.founder.socketcopy.server.Server;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App {

    private static final Logger log = Logger.getLogger(App.class);

    private static int serverPort;
    public static List<IpPort> clientList;

    /**
     * 读取配置文件
     * @return
     */
    public static void readConfigFile() {
        try {
            InputStream in = App.class.getClassLoader().getResource("config.properties").openStream();
            Properties prop = new Properties();
            prop.load(in);
            serverPort = Integer.valueOf(prop.getProperty("server.port"));
            clientList = new ArrayList<IpPort>();
            String clients = prop.getProperty("client.list");
            String[] clientArray = clients.split(",");
            for(String client:clientArray){
                client = client.replace("：",":");
                String[] ipPortArray = client.split(":");
                IpPort ipPort = new IpPort();
                ipPort.setIp(ipPortArray[0]);
                ipPort.setPort(Integer.valueOf(ipPortArray[1]));
                clientList.add(ipPort);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        readConfigFile();
        log.debug(clientList);
        try {
            new Server(serverPort).run();
        } catch (Exception e) {
            log.error("服务启动异常",e);
        }
    }
}
