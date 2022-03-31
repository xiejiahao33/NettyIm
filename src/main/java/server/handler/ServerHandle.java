package server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import protocol.Packet;
import protocol.PacketCodec;
import protocol.request.LoginRequestPacket;
import protocol.request.MessageRequestPacket;
import protocol.response.LoginResponsePacket;
import protocol.response.MessageResponsePacket;

import java.util.Date;

public class ServerHandle extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf byteBuf = (ByteBuf) msg;


        //解码
        Packet packet = PacketCodec.INSTANCE.decode(byteBuf);
        LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
        if (packet instanceof LoginRequestPacket) {
            loginResponsePacket.setVersion(packet.getVersion());
            //登录校验
            if (valid(packet)) {
                System.out.println(new Date() + ":登陆成功！");
                loginResponsePacket.setSuccess(true);
            }else {
                //校验失败
                loginResponsePacket.setReason("账号密码校验失败");
                loginResponsePacket.setSuccess(false);
            }
            byteBuf = PacketCodec.INSTANCE.encode(loginResponsePacket);
            ctx.channel().writeAndFlush(byteBuf);

        }else if (packet instanceof MessageRequestPacket) {
            //处理消息
            MessageRequestPacket messageRequestPacket = (MessageRequestPacket) packet;
            System.out.println(new Date() + ": 收到客户端消息" + messageRequestPacket.getMessage());

            MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
            messageResponsePacket.setMessage("服务端回复【"+messageRequestPacket.getMessage()+"】");
            byteBuf = PacketCodec.INSTANCE.encode(messageResponsePacket);
            ctx.channel().writeAndFlush(byteBuf);
        }else {
            System.out.println("没有任何消息或者登录请求");
        }
    }

    private boolean valid(Packet packet){
        return true;
    }
}
