package client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import protocol.request.LoginRequestPacket;
import protocol.response.LoginResponsePacket;
import util.LoginUtil;

import java.util.Date;
import java.util.UUID;

public class LoginResponseHandler extends SimpleChannelInboundHandler<LoginResponsePacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginResponsePacket loginResponsePacket) throws Exception {
        if(loginResponsePacket.isSuccess()){
            System.out.println(new Date() + ":客户端登录成功！");
            LoginUtil.markAsLogin(ctx.channel());
        }else{
            System.out.println(new Date() + ":客户端登录失败，原因：" + loginResponsePacket.getCommand());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        //创建登录请求

        System.out.println("LoginResponseHandler 的channelActive方法");
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setPassword("sunday");
        loginRequestPacket.setUsername("sunday");
        loginRequestPacket.setUserId(UUID.randomUUID().toString());



        //写数据
        ctx.channel().writeAndFlush(loginRequestPacket);
    }

}
