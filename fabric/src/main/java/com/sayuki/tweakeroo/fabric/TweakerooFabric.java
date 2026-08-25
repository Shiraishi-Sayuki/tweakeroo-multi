package com.sayuki.tweakeroo.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.network.ServuxTweaksHandler;
import fi.dy.masa.tweakeroo.network.ServuxTweaksPacket;

// Fabricエントリポイント - fabric.mod.jsonのmainから呼ばれる、共通初期化の後にServux受信を登録する
// MOD自体はクライアント限定(environment: client)なのでModInitializerで問題ない
public class TweakerooFabric implements ModInitializer {
    // 受信登録 - 共通初期化でペイロード種類が登録されてからレシーバーを足す
    private static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ServuxTweaksPacket.Payload.ID, (payload, context) -> {
            ServuxTweaksHandler.getInstance().receivePlayPayload(payload, null);
        });
    }

    @Override
    public void onInitialize() {
        Tweakeroo.onInitialize();
        try {
            registerReceivers();
        } catch (Throwable t) {
            Tweakeroo.LOGGER.warn("registerReceives failed - {}", t.getMessage());
        }
    }
}
