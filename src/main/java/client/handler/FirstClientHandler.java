package client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.Date;

public class FirstClientHandler extends ChannelInboundHandlerAdapter {

    //该方法在客户端连接建立成功后被调用
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println(new Date() + ": 客户端写出数据");

        //1、获取数据
        ByteBuf buffer = getByteBuf(ctx);

        //2、写数据到服务端
        ctx.channel().writeAndFlush(buffer);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        //1、获取Netty对二进制的抽象 ByteBuff
        //1.1 ctx.alloc() 返回一个ByteBuf的内存管理器，作用是分配一个ByteBuf
        //1.2 然后把字符串的二进制数据填充到ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();

        //2、准备数据，指定字符串的字符集为UTF-8
        byte[] bytes = "你好，谢家豪!".getBytes(Charset.forName("utf-8"));

        //3、填充数据到 byteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }
}
