package com.sayuki.tweakeroo.neoforge;

import com.sayuki.tweakeroo.neoforge.NeoForgePayloads;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.gui.GuiConfigs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// NeoForgeエントリポイント - クライアント限定で共通初期化とペイロード登録と設定画面登録をやる
@Mod(value = "tweakeroo", dist = Dist.CLIENT)
public class TweakerooNeoForge {
    public TweakerooNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // 共通初期化 - Fabric側と同じ流れでmasaのコードを呼ぶ
        Tweakeroo.onInitialize();

        // 設定画面登録 - Mod一覧のボタンから開けるようにする、Supplierは明示キャストしないとオーバーロードで曖昧になる
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo("tweakeroo", "Tweakeroo", GuiConfigs::new));
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (java.util.function.Supplier<IConfigScreenFactory>) () -> (IConfigScreenFactory) (minecraft, parent) -> {
                    GuiConfigs gui = new GuiConfigs();
                    gui.setParent(parent);
                    return gui;
                });

        // ペイロード登録 - ModバスのイベントでServuxチャンネルを受信できるようにする
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            NeoForgePayloads.register(event.registrar("servux"));
        });
    }
}
