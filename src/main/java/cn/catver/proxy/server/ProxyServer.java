package cn.catver.proxy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class ProxyServer {

    public static final int KILLTHREADATWHATTIMES = 3; //3秒后杀死pt与ct

    public static ServerSocket serverSocket;
    public static boolean stopSignal = false;

    public static HashMap<UUID,ProxyClientThread> threads = new HashMap<>();

    public static void main(String[] args) {
        new ProxyServerCommandInput();
        try{
            serverSocket = new ServerSocket(25500);
            System.out.println("server start at 25500");
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                String line;
                while (!stopSignal){
                    line = scanner.nextLine();
                    ProxyServerCommandInput.run(line);
                }
            }).start();
            while (!stopSignal){
                UUID id = UUID.randomUUID();
                ProxyClientThread pct = new ProxyClientThread(serverSocket.accept(),id);
                try{
                    pct.start();
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
                threads.put(id,pct);
            }
        }catch (Exception e){
            e.printStackTrace();
            if(e.getMessage().equalsIgnoreCase("socket closed")){
                System.out.println("info: this is not a error");
                System.exit(0);
            }
        }
    }

    public static boolean isValid(int p){ //判断端口是否有效，防止映射敏感端口
        return p >= 30000 && p < 30051;
    }
}
