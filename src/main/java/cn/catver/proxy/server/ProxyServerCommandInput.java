package cn.catver.proxy.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import java.io.IOException;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class ProxyServerCommandInput {
    static CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
    public ProxyServerCommandInput(){
        { // exit指令：退出服务器
            dispatcher.register(literal("exit").executes(commandContext -> {
                ProxyServer.stopServer();
                return 1;
            }));
        }

        {
            dispatcher.register(literal("foo").executes(commandContext -> {
                System.out.println("foo!");
                return 1;
            }));
        }

        { //list 指令 : 列出已连接的数量
            dispatcher.register(literal("list").executes(commandContext -> {
                System.out.println(ColorText.CYAN(String.format("已连接%d个", ProxyServer.threads.size())));
                return 1;
            }));
        }

        System.out.println("register command finish");
    }
    public static void run(String l){
        try{
            dispatcher.execute(l,null);
        }catch (CommandSyntaxException e){
            System.out.println(ColorText.RED(e.getMessage()));
        }
    }
}
