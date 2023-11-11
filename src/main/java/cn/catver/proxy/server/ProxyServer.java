package cn.catver.proxy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ProxyServer {

    public static final int KILLTHREADATWHATTIMES = 3; //3秒后杀死pt与ct

    public static ServerSocket serverSocket;
    public static boolean stopSignal = false;

    public static HashMap<UUID,ProxyClientThread> threads = new HashMap<>();
    public static int PORT = 25500;

    public static void main(String[] args) {
        if(args.length == 1){
            try{
                PORT = Integer.parseInt(args[0]);
            }catch (NumberFormatException e){

            }
        }
        new ProxyServerCommandInput();
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println(String.format("server start at %d", PORT));
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

    public static void sendStopSignalToChildrenThread(){
        for (Map.Entry<UUID, ProxyClientThread> entry : threads.entrySet()) {
            entry.getValue().kt.interrupt(); //发送中断信号，让正在睡觉的线程被唤醒
        }
        threads.clear();
    }

    public static boolean isValid(int p){ //判断端口是否有效，防止映射敏感端口
        return p >= 30000 && p < 30051;
    }

    public static void stopServer(){
        System.out.println("server try to stop");
        ProxyServer.stopSignal = true;
        ProxyServer.sendStopSignalToChildrenThread();
        try {
            ProxyServer.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
