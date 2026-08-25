package com.sayuki.tweakeroo.neoforge;

import com.sayuki.tweakeroo.neoforge.NeoForgePayloads;
import fi.dy.masa.tweakeroo.Tweakeroo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// NeoForgeエントリポイント - クライアント限定で共通初期化とペイロード登録をやる
@Mod(value = "tweakeroo", dist = Dist.CLIENT)
public class TweakerooNeoForge {
    public TweakerooNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // 共通初期化 - Fabric側と同じ流れでmasaのコードを呼ぶ
        Tweakeroo.onInitialize();

        // ペイロード登録 - ModバスのイベントでServuxチャンネルを受信できるようにする
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            NeoForgePayloads.register(event.registrar("servux"));
        });
    }
}
