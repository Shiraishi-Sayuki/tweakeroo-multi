package fi.dy.masa.tweakeroo.network;

import java.util.List;

import io.netty.buffer.Unpooled;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import fi.dy.masa.malilib.network.ClientPacketChannelHandler;
import fi.dy.masa.malilib.network.IPluginChannelHandler;
import fi.dy.masa.malilib.network.PacketSplitter;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.EntityDataManager;

public class ServuxTweaksHandler implements IPluginChannelHandler
{
    private static final ServuxTweaksHandler INSTANCE = new ServuxTweaksHandler() {
    };
    public static ServuxTweaksHandler getInstance() { return INSTANCE; }

    public static final Identifier CHANNEL_ID = new Identifier("servux", "tweaks");

    private boolean servuxRegistered;
    private boolean payloadRegistered = false;
    private int failures = 0;
    private static final int MAX_FAILURES = 4;
    private long readingSessionKey = -1;

    public Identifier getPayloadChannel() { return CHANNEL_ID; }

    public boolean isPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            return payloadRegistered;
        }

        return false;
    }

    public void setPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            this.payloadRegistered = true;
        }
    }

    public void decodeClientData(Identifier channel, ServuxTweaksPacket packet)
    {

        if (!channel.equals(CHANNEL_ID))
        {
            return;
        }
        switch (packet.getType())
        {
            case PACKET_S2C_METADATA ->
            {
                if (EntityDataManager.getInstance().receiveServuxMetadata(packet.getCompound()))
                {
                    this.servuxRegistered = true;
                }
            }
            case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> EntityDataManager.getInstance().handleBlockEntityData(packet.getPos(), packet.getCompound(), null);
            case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> EntityDataManager.getInstance().handleEntityData(packet.getEntityId(), packet.getCompound());
            case PACKET_S2C_NBT_RESPONSE_DATA ->
            {
                // 1.20.1では再組立はmalilibのPacketSplitterが済ませている
                try
                {
                    EntityDataManager.getInstance().handleBulkEntityData(packet.getTransactionId(), packet.getCompound());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksHandler#decodeClientData(): Tweaks Data: error reading buffer [{}]", e.getLocalizedMessage());
                }
            }
            default -> Tweakeroo.LOGGER.warn("ServuxTweaksHandler#decodeClientData(): received unhandled packetType {} of size {} bytes.", packet.getPacketType(), packet.getTotalSize());
        }
    }

    public void reset(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID) && this.servuxRegistered)
        {
            this.servuxRegistered = false;
            this.failures = 0;
            this.readingSessionKey = -1;
        }
    }

    public void resetFailures(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID) && this.failures > 0)
        {
            this.failures = 0;
        }
    }

    // ---- IPluginChannelHandler (malilib 1.20.1) ----

    @Override
    public List<Identifier> getChannels()
    {
        return ImmutableList.of(CHANNEL_ID);
    }

    @Override
    public boolean registerToServer()
    {
        return true;
    }

    @Override
    public boolean usePacketSplitter()
    {
        return true;
    }

    @Override
    public void onPacketReceived(PacketByteBuf buf)
    {
        try
        {
            ServuxTweaksPacket packet = ServuxTweaksPacket.fromPacket(buf);

            if (packet != null)
            {
                this.decodeClientData(CHANNEL_ID, packet);
            }
            else if (buf.isReadable())
            {
                // フレーム無しのバルクNBT(varint txId + NBT)の場合
                int txId = buf.readVarInt();
                NbtCompound nbt = buf.readNbt();
                EntityDataManager.getInstance().handleBulkEntityData(txId, nbt);
            }
        }
        catch (Exception e)
        {
            Tweakeroo.LOGGER.error("ServuxTweaksHandler#onPacketReceived: error decoding packet [{}]", e.getLocalizedMessage());
        }
    }

    // ---- registration / lifecycle kept for EntityDataManager ----

    public void registerPlayReceiver()
    {
        if (this.payloadRegistered == false)
        {
            ClientPacketChannelHandler.getInstance().registerClientChannelHandler(this);
            this.payloadRegistered = true;
        }
    }

    public void unregisterPlayReceiver()
    {
        if (this.payloadRegistered)
        {
            ClientPacketChannelHandler.getInstance().unregisterClientChannelHandler(this);
            this.payloadRegistered = false;
        }
    }

    public void sendPlayRequest(ServuxTweaksPacket packet)
    {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();

        if (handler == null)
        {
            this.onSendFailure();
            return;
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.toPacket(buf);

        if (this.usePacketSplitter())
        {
            PacketSplitter.send(handler, CHANNEL_ID, buf);
        }
        else
        {
            handler.sendPacket(new CustomPayloadC2SPacket(CHANNEL_ID, buf));
        }
    }

    private void onSendFailure()
    {
        if (this.failures > MAX_FAILURES)
        {
            Tweakeroo.debugLog("ServuxTweaksHandler#onSendFailure(): encountered [{}] send failures, cancelling any Servux join attempt(s)", MAX_FAILURES);
            this.servuxRegistered = false;
            this.unregisterPlayReceiver();
            EntityDataManager.getInstance().onPacketFailure();
        }
        else
        {
            this.failures++;
        }
    }

    public void encodeClientData(ServuxTweaksPacket packet)
    {
        this.sendPlayRequest(packet);
    }
}
