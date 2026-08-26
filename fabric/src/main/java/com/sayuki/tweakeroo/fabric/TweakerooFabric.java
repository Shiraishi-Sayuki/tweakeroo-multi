package com.sayuki.tweakeroo.fabric;

import fi.dy.masa.malilib.network.ClientPacketChannelHandler;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.network.ServuxTweaksHandler;
import net.fabricmc.api.ModInitializer;

// Fabricエントリポイント - fabric.mod.jsonのmainから呼ばれる、共通初期化後にチャンネル登録する
// MOD自体はクライアント限定(environment: client)なのでModInitializerで問題ない
public class TweakerooFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // 共通初期化 - masaのコードを呼ぶ
        Tweakeroo.onInitialize();

        // 1.20.1 - malilibのチャンネルハンドラ経由で登録(旧PayloadAPIは削除済み)
        ClientPacketChannelHandler.getInstance().registerClientChannelHandler(
                ServuxTweaksHandler.getInstance()
        );
    }
}
