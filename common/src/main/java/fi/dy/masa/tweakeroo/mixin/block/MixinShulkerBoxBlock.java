package fi.dy.masa.tweakeroo.mixin.block;

import java.util.List;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.BlockView;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ShulkerBoxBlock.class)
public abstract class MixinShulkerBoxBlock
{
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void removeVanillaTooltip(ItemStack stack,
                                      BlockView view,
                                      List<Text> tooltip,
                                      TooltipContext options,
                                      CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_SHULKER_BOX_TOOLTIP.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
