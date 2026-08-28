package com.sayuki.tweakeroo.neoforge;

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
import com.sayuki.malilib.neoforge.NeoForgeNetworkHelper;

@Mod(value = "tweakeroo", dist = Dist.CLIENT)
public class TweakerooNeoForge {
    public TweakerooNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Tweakeroo.onInitialize();

        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo("tweakeroo", "Tweakeroo", GuiConfigs::new));
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (java.util.function.Supplier<IConfigScreenFactory>) () -> (IConfigScreenFactory) (minecraft, parent) -> {
                    GuiConfigs gui = new GuiConfigs();
                    gui.setParent(parent);
                    return gui;
                });

        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            NeoForgeNetworkHelper.setRegistrar(event.registrar("tweakeroo"));
        });
    }
}
