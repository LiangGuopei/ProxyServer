package cn.catver.proxy.server;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
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

    public void onRecvDataFromClient(int data) throws IOException {
        proxy.getOutputStream().write(data);
    }

    public void onRecvDataFromProxy(int data) throws IOException{
        client.getOutputStream().write(data);
    }

    private void onStop(){
        ProxyServer.threads.remove(id,this);
        kt.interrupt();
        stopSignal = true;
    }

    class ClientThread extends Thread{

        @Override
        public void run() {
            try{
                InputStream is = client.getInputStream();

                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder l = new StringBuilder();
                    String n;
                    while (true){
                        n = br.readLine();
                        if(n.equalsIgnoreCase("---end---")){
                            break;
                        }else{
                            l.append(n).append("\n");
                        }
                    }
                    try{
                        Properties properties = new Properties();
                        properties.load(new ByteArrayInputStream(l.toString().getBytes()));
                        String version = properties.getOrDefault("version","none").toString();
                        if(!version.equalsIgnoreCase(ProxyServer.version)){
                            throw new RuntimeException("版本不一致");
                        }
                        int port = Integer.getInteger(properties.getOrDefault("port",0).toString(),0);
                        //int port = Integer.parseInt(l);
                        if(!ProxyServer.isValid(port)) throw new RuntimeException(); //端口不合法，停止
                        proxy = new Socket("127.0.0.1",port);
                        ProxyClientThread.this.port = port;
                        ProxyClientThread.this.pt.start(); //唤起proxythread
                        System.out.println("client "+id+" connect to "+port);
                    }catch (Exception e){
                        client.close();
                        kt.interrupt();
                        stopSignal = true;
                        return;
                    }
                }

                int data;
                while(!stopSignal){
                    data = is.read();
                    if(data != -1){
                        onRecvDataFromClient(data);
                    }else {
                        kt.interrupt();
                        stopSignal = true;
                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class ProxyThread extends Thread{

        @Override
        public void run() {
            try{
                InputStream is = proxy.getInputStream();
                int data;
                while (!stopSignal){
                    data = is.read();
                    if(data != -1){
                        onRecvDataFromProxy(data);
                    }else{
                        kt.interrupt();
                        stopSignal = true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class KillThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    try {
                        client.close();
                        proxy.close();
                    } catch (IOException ex) {

                    }
                    System.out.println("a client disconnect id: "+id);
                    onStop();
                    break;
                }
            }
            ct.stop();
            pt.stop(); //KillThread能醒其实这俩早退出了
        }
    }

    public void start() throws Exception {
        try{
            ct = new ClientThread();
            pt = new ProxyThread();
            kt = new KillThread();
            ct.start(); // ClientThread启动
            kt.start(); // KillThread开始睡觉
            //pt.start();
        }catch (Exception e){
            throw new RuntimeException("start error msg: "+e.getMessage());
        }
    }
}
