package client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import protocol.Packet;
import protocol.PacketCodec;
import protocol.request.LoginRequestPacket;
import protocol.response.LoginResponsePacket;
import protocol.response.MessageResponsePacket;
import util.LoginUtil;

import java.util.Date;
import java.util.UUID;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ": 客户端开始登录");

        //创建登录对象
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setUserId(UUID.randomUUID().toString());
        loginRequestPacket.setUsername("谢家豪");
        loginRequestPacket.setPassword("4396");

        //编码
        ByteBuf byteBuf = PacketCodec.INSTANCE.encode(loginRequestPacket);

        //写数据
        ctx.channel().writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        Packet packet = PacketCodec.INSTANCE.decode(byteBuf);

        if (packet instanceof LoginResponsePacket) {
            LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

            if (loginResponsePacket.isSuccess()) {
                System.out.println(new Date() + ": 客户端登录成功");
                LoginUtil.markAsLogin(ctx.channel());
            }else {
                System.out.println(new Date() + ": 客户端登录失败, 原因: " + loginResponsePacket.getReason());
            }
        } else if (packet instanceof MessageResponsePacket){
            MessageResponsePacket messageResponsePacket = (MessageResponsePacket)packet;

            System.out.println(new Date() + "：收到服务端消息：" + messageResponsePacket.getMessage());
        }
    }
}
