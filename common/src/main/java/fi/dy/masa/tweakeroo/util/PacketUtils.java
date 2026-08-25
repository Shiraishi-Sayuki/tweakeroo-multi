package fi.dy.masa.tweakeroo.util;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;

import fi.dy.masa.tweakeroo.Tweakeroo;

// パケット送信ヘルパー - YarnとMojMapでメソッド名が違う(sendPacket/send)のでリフレクションで両対応する
public class PacketUtils
{
    public static boolean sendPacket(@Nullable ClientPlayNetworkHandler handler, Packet<?> packet)
    {
        if (handler == null)
        {
            return false;
        }

        try
        {
            Method method = handler.getClass().getMethod("sendPacket", Packet.class);
            method.invoke(handler, packet);
            return true;
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                Method method = handler.getClass().getMethod("send", Packet.class);
                method.invoke(handler, packet);
                return true;
            }
            catch (Exception e2)
            {
                Tweakeroo.LOGGER.warn("PacketUtils#sendPacket: failed to send packet [{}]", packet.getClass().getName(), e2);
            }
        }
        catch (Exception e)
        {
            Tweakeroo.LOGGER.warn("PacketUtils#sendPacket: failed to send packet [{}]", packet.getClass().getName(), e);
        }

        return false;
    }

    // インスタンス化させない - staticだけのクラスだから
    private PacketUtils() {}
}
