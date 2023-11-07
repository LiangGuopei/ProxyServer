package cn.catver.proxy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class ProxyServer {
    static ServerSocket serverSocket;
    public static boolean stopSignal = false;

    public static String HOST = "127.0.0.1";
    public static int PORT = 82;

    public static void main(String[] args) {

        if(args.length != 2){
            System.out.println("start args error");
        }else{
            HOST = args[0];
            PORT = Integer.parseInt(args[1]);
        }


        try{
            serverSocket = new ServerSocket(25500);
            System.out.println("server start at 25500");
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                String line;
                while (!stopSignal){
                    line = scanner.nextLine();
                    line = line.trim();
                    if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("stop")){
                        System.out.println("try to stop server");
                        stopSignal = true;
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("the server has error to close serversocket");
                        }
                    }
                }
            }).start();
            while (!stopSignal){
                new ProxyClientThread(serverSocket.accept()).start();
            }
        }catch (Exception e){
            e.printStackTrace();
            if(e.getMessage().equalsIgnoreCase("socket closed")){
                System.out.println("info: this is not a error");
                System.exit(0);
            }
        }
    }
}
