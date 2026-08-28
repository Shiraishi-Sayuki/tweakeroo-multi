package fi.dy.masa.tweakeroo.network;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import fi.dy.masa.malilib.network.ClientPlayHandler;
import fi.dy.masa.malilib.network.IClientPayloadData;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.EntityDataManager;

public class ServuxTweaksHandler implements IPluginClientPlayHandler<ServuxTweaksPacket.Payload>
{
	private static final ServuxTweaksHandler INSTANCE = new ServuxTweaksHandler();

	public static ServuxTweaksHandler getInstance() {return INSTANCE;}

	public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath("servux", "tweaks");

	private boolean servuxRegistered;
	private boolean payloadRegistered = false;
	private int failures = 0;

	@Override
	public Identifier getPayloadChannel() {return CHANNEL_ID;}

	@Override
	public boolean isPlayRegistered(Identifier channel)
	{
		if (channel.equals(CHANNEL_ID))
		{
			return payloadRegistered;
		}

		return false;
	}

	@Override
	public void setPlayRegistered(Identifier channel)
	{
		if (channel.equals(CHANNEL_ID))
		{
			this.payloadRegistered = true;
		}
	}

	@Override
	public <P extends IClientPayloadData> void decodeClientData(Identifier channel, P data)
	{
		if (!channel.equals(CHANNEL_ID))
		{
			return;
		}
		if (!EntityDataManager.getInstance().isEnabled() || !this.checkFailures())
		{
			return;
		}

		if (data instanceof ServuxTweaksPacket packet)
		{
			switch (packet.getType())
			{
				case PACKET_S2C_METADATA ->
				{
					if (EntityDataManager.getInstance().receiveServuxMetadata(packet.getCompound()))
					{
						this.servuxRegistered = true;
					}
				}
				case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE ->
				{
					if (this.servuxRegistered)
					{
						EntityDataManager.getInstance().handleBlockEntityData(packet.getPos(), packet.getCompound());
					}
				}
				case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE ->
				{
					if (this.servuxRegistered)
					{
						EntityDataManager.getInstance().handleEntityData(packet.getEntityId(), packet.getCompound());
					}
				}
				default ->
						Tweakeroo.LOGGER.warn("ServuxTweaksHandler#decodeClientData(): received unhandled packetType {} of size {} bytes.", packet.getPacketType(), packet.getTotalSize());
			}
		}
	}

	@Override
	public void reset(Identifier channel)
	{
		if (channel.equals(CHANNEL_ID) && this.servuxRegistered)
		{
			this.servuxRegistered = false;
			this.failures = 0;
		}
	}

	public void resetFailures(Identifier channel)
	{
		if (channel.equals(CHANNEL_ID) && this.failures > 0)
		{
			this.failures = 0;
		}
	}

	@Override
	public void receivePlayPayload(ServuxTweaksPacket.Payload payload, ClientPacketListener handler)
	{
		if (payload.type().id().equals(CHANNEL_ID))
		{
			ServuxTweaksHandler.INSTANCE.decodeClientData(CHANNEL_ID, payload.data());
		}
	}

	// Compatibility overload for Fabric's ClientPlayNetworking.Context (not used in common)
	public void receive(ServuxTweaksPacket.Payload payload, Object context)
	{
		receivePlayPayload(payload, null);
	}

	@Override
	public void encodeWithSplitter(FriendlyByteBuf buffer, ClientPacketListener handler)
	{
		// Send each PacketSplitter buffer slice
		ServuxTweaksHandler.INSTANCE.sendPlayPayload(new ServuxTweaksPacket.Payload(ServuxTweaksPacket.ResponseS2CData(buffer)));
	}

	@Override
	public <P extends IClientPayloadData> void encodeClientData(P data)
	{
		if (!EntityDataManager.getInstance().isEnabled() || !this.checkFailures())
		{
			return;
		}

		if (data instanceof ServuxTweaksPacket packet)
		{
			if (!ServuxTweaksHandler.INSTANCE.sendPlayPayload(new ServuxTweaksPacket.Payload(packet)))
			{
				this.tickFailures();
			}
		}
	}

	@Override
	public boolean checkFailures()
	{
		return !(this.failures > this.maxFailures());
	}

	@Override
	public void tickFailures()
	{
		if (this.failures > this.maxFailures())
		{
			Tweakeroo.debugLog("ServuxTweaksHandler#tickFailures(): encountered [{}] sendPayload failures, cancelling any Servux join attempt(s)", this.maxFailures());
			this.servuxRegistered = false;
			ServuxTweaksHandler.INSTANCE.unregisterPlayReceiver();
			EntityDataManager.getInstance().onPacketFailure();
		}
		else
		{
			this.failures++;
		}
	}

	// Multiloader helper: register payload receiver (Fabric & NeoForge both use this)
	public void registerPlayReceiver(CustomPacketPayload.Type<ServuxTweaksPacket.Payload> id, java.util.function.BiConsumer<ServuxTweaksPacket.Payload, ClientPacketListener> consumer)
	{
		ClientPlayHandler.getInstance().registerClientPlayHandler(this);
		this.registerPlayPayload(id, ServuxTweaksPacket.Payload.CODEC, IPluginClientPlayHandler.BOTH_CLIENT);
	}

	@Override
	public void unregisterPlayReceiver()
	{
		ClientPlayHandler.getInstance().unregisterClientPlayHandler(this);
	}
}
