package cn.catver.proxy.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ProxyClientThread{
    Socket client;
    Socket proxy;

    ClientThread ct;
    ProxyThread pt;
    boolean stopSignal = false;

    public ProxyClientThread(Socket socket){
        try{
            System.out.println("a client connect");
            client = socket;
            proxy = new Socket(ProxyServer.HOST,ProxyServer.PORT);
        }catch (Exception e){
            throw new RuntimeException("error to create proxyclientthread");
        }
    }

    class ClientThread extends Thread{

        @Override
        public void run() {
            try{
                OutputStream os = proxy.getOutputStream();

                InputStream is = client.getInputStream();
                int data;
                while(true){
                    data = is.read();
                    if(ProxyServer.stopSignal || stopSignal){
                        System.out.println("a client disconnect");
                        client.close();
                        proxy.close();
                        return;
                    }
                    if(data != -1){
                        os.write(data);
                    }else {
                        stopSignal = true;
                    }
                }


            }catch (Exception e){
                if(!e.getMessage().equalsIgnoreCase("Socket closed") && (stopSignal || ProxyServer.stopSignal)){
                    e.printStackTrace();
                }
            }
        }
    }

    class ProxyThread extends Thread{

        @Override
        public void run() {
            try{
                OutputStream os = client.getOutputStream();

                InputStream is = proxy.getInputStream();
                int data;
                while (true){
                    data = is.read();
                    if(ProxyServer.stopSignal || stopSignal){
                        proxy.close();
                        client.close();
                        return;
                    }
                   // System.out.println(data);
                    if(data != -1){
                        os.write(data);
                    }else{
                        stopSignal = true;
                    }
                }
            }catch (Exception e){
                if(!e.getMessage().equalsIgnoreCase("Socket closed") && (stopSignal || ProxyServer.stopSignal)){
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() throws Exception {
        try{
            ct = new ClientThread();
            pt = new ProxyThread();
            ct.start();
            pt.start();
        }catch (Exception e){
            throw new RuntimeException("start error msg: "+e.getMessage());
        }
    }
}
