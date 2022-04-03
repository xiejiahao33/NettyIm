package client;

import client.handler.ClientHandler;
import client.handler.LoginResponseHandler;
import client.handler.MessageResponseHandler;
import codec.PacketDecoder;
import codec.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import protocol.PacketCodec;
import protocol.request.MessageRequestPacket;
import protocol.response.LoginResponsePacket;
import server.handler.LoginRequestHandler;
import server.handler.MessageRequestHandler;
import util.LoginUtil;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final int MAX_RETRY = 5;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap
                //1.指定线程模型
                .group(workerGroup)
                //2.指定IO类型为NIO
                .channel(NioSocketChannel.class)
                //3.IO处理逻辑
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ch.pipeline().addLast(new PacketDecoder());
                        ch.pipeline().addLast(new LoginResponseHandler());
                        ch.pipeline().addLast(new MessageResponseHandler());
                        ch.pipeline().addLast(new PacketEncoder());
                    }
                });
        //4.建立连接
        connect(bootstrap, HOST, PORT, MAX_RETRY);
    }


    private static void connect(Bootstrap bootstrap, String host, int port, int retry){
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()){
                System.out.println("服务器连接成功");
                Channel channel = ((ChannelFuture)future).channel();
                startConsoleThread(channel);
            }else if (retry == 0){
                System.out.println("重试次数已经用完，放弃连接！");
            }else {
                //第几次连接
                int order = (MAX_RETRY - retry) + 1;
                //本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ":连接失败, 第" + order + "次重连...");
                //bootstrap.config() 返回一个BootstrapConfig 对Bootstrap配置参数的抽象
                //bootstrap.config().group() 返回开始配置的线程模型workerGroup
                //调用workerGroup的schedule方法即可实现定时任务逻辑
                bootstrap.config().group().schedule( ()->connect(bootstrap, host, port, retry-1), delay, TimeUnit.SECONDS);
            }
        });
    }

    private static void startConsoleThread(Channel channel){
        new Thread(()->{
            while (!Thread.interrupted()){
                if (LoginUtil.hasLogin(channel)) {
                    System.out.println("输入消息发送到服务端: ");
                    Scanner sc = new Scanner(System.in);
                    String line = sc.nextLine();

                    MessageRequestPacket packet = new MessageRequestPacket();
                    packet.setMessage(line);
                    ByteBuf byteBuf = PacketCodec.INSTANCE.encode(packet);
                    channel.writeAndFlush(byteBuf);
                }
            }
        }).start();

    }
}
