package cn.catver.proxy.server;

public class ColorText {
    public static String RED(String l){
        return "\033[31m"+l+"\033[0m";
    }

    public static String CYAN(String l){
        return "\033[36m"+l+"\033[0m";
    }

    public static String GREEN(String l){
        return "\033[32m"+l+"\033[0m";
    }
}
