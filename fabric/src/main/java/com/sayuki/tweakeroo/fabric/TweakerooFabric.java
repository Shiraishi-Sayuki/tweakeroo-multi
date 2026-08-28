package com.sayuki.tweakeroo.fabric;

import net.fabricmc.api.ModInitializer;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class TweakerooFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Tweakeroo.onInitialize();
    }
}
