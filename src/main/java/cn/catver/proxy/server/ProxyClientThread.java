package cn.catver.proxy.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.UUID;

public class ProxyClientThread{
    Socket client;
    Socket proxy;

    ClientThread ct;
    ProxyThread pt;
    boolean stopSignal = false;
    int port;
    public UUID id;
    KillThread kt;


    public ProxyClientThread(Socket socket,UUID id){
        try{
            System.out.println("a client connect id: "+id);
            client = socket;
            this.id = id;
        }catch (Exception e){
            throw new RuntimeException("error to create proxyclientthread");
        }
    }

    class ClientThread extends Thread{

        @Override
        public void run() {
            try{
                InputStream is = client.getInputStream();

                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String l = br.readLine();
                    try{
                        int port = Integer.parseInt(l);
                        if(!ProxyServer.isValid(port)) throw new RuntimeException(); //端口不合法，停止
                        proxy = new Socket("127.0.0.1",port);
                        ProxyClientThread.this.port = port;
                        ProxyClientThread.this.pt.start(); //唤起proxythread
                        System.out.println("client "+id+" connect to "+port);
                    }catch (Exception e){
                        client.close();
                        stopSignal = true;
                        onStop();
                        return;
                    }
                }
                OutputStream os = proxy.getOutputStream();

                int data;
                while(true){
                    data = is.read();
                    if(ProxyServer.stopSignal || stopSignal){
                        onStop();
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
                if(e.getMessage().equalsIgnoreCase("Socket closed") && (stopSignal || ProxyServer.stopSignal)){
                    return;
                }
                e.printStackTrace();
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
                        onStop();
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

    class KillThread extends Thread{
        @Override
        public void run() {
            while (true){
                if(stopSignal || ProxyServer.stopSignal || Thread.interrupted()){
                    System.out.println("a client disconnect id: "+id);
                    break;
                }
            }
            try{
                Thread.sleep(ProxyServer.KILLTHREADATWHATTIMES);
                ct.stop();
                pt.stop();
            }catch (Exception e){

            }
        }
    }

    public void start() throws Exception {
        try{
            ct = new ClientThread();
            pt = new ProxyThread();
            ct.start();
            kt = new KillThread();
            kt.start();
            //pt.start();
        }catch (Exception e){
            throw new RuntimeException("start error msg: "+e.getMessage());
        }
    }

    private void onStop(){
        ProxyServer.threads.remove(id,this);
        kt.interrupt();
    }
}
