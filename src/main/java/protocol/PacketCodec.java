package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import protocol.request.LoginRequestPacket;
import protocol.request.MessageRequestPacket;
import protocol.response.LoginResponsePacket;
import protocol.response.MessageResponsePacket;
import serialize.Serializer;
import serialize.impl.JSONSerializer;

import java.util.HashMap;
import java.util.Map;

import static protocol.command.Command.*;

public class PacketCodec {

    public static final int MAGIC_NUMBER = 0x12345678;
    public static final PacketCodec INSTANCE = new PacketCodec();

    private final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private final Map<Byte, Serializer> serializerMap;

    private PacketCodec() {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);


        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializerAlgorithm(), serializer);

    }

    /**
     * 将对象转二级制
     * @param packet //传入java对象
     */
    public ByteBuf encode(Packet packet){
        //1、创建ByteBuf对象
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();


        //2、序列化Java对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        //3、实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

    /**
     * 将对象转二级制
     * @param byteBufAllocator
     * @param packet
     */
    public ByteBuf encode(ByteBufAllocator byteBufAllocator,Packet packet) {
        // 1. 创建 ByteBuf 对象
        ByteBuf byteBuf = byteBufAllocator.ioBuffer();
        // 2. 序列化 java 对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        // 3. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }

    /**
     * 将对象转二级制
     * @param byteBuf  ByteBuf对象
     * @param packet   java对象
     * @return
     */
    public ByteBuf encode(ByteBuf byteBuf,Packet packet) {
        // 1. 序列化 java 对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        // 2. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }


    public Packet decode(ByteBuf byteBuf) {
        // 跳过 magic number
        byteBuf.skipBytes(4);

        // 跳过版本号
        byteBuf.skipBytes(1);

        // 序列化算法
        byte serializeAlgorithm = byteBuf.readByte();

        // 指令
        byte command = byteBuf.readByte();

        // 数据包长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializeAlgorithm);

        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }

        return null;
    }




    private Serializer getSerializer(byte serializeAlgorithm) {

        return serializerMap.get(serializeAlgorithm);
    }

    //这个Class<? extends Packet> 代表返回的类是一个Packet的子类，
    //具体通过容器的映射值来说明  packetTypeMap
    private Class<? extends Packet> getRequestType(byte command) {

        return packetTypeMap.get(command);
    }
}
