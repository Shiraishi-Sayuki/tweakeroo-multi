package com.sayuki.tweakeroo.fabric;

import net.fabricmc.api.ModInitializer;
import fi.dy.masa.tweakeroo.Tweakeroo;

// Fabricエントリポイント - fabric.mod.jsonのmainから呼ばれる、共通初期化に橋渡しするだけ
// MOD自体はクライアント限定(environment: client)なのでModInitializerで問題ない
public class TweakerooFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Tweakeroo.onInitialize();
    }
}
