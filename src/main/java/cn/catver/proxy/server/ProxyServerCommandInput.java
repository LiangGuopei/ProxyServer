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
        { // quit exit stop指令：退出服务器
            CommandNode<Object> node = dispatcher.register(literal("quit").executes(commandContext -> {
                System.out.println("server try to stop");
                ProxyServer.stopSignal = true;
                try {
                    ProxyServer.serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return 1;
            }));
            dispatcher.register(literal("exit").redirect(node));
            dispatcher.register(literal("stop").redirect(node));

        }

        {
            dispatcher.register(literal("foo").executes(commandContext -> {
                System.out.println("foo!");
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
