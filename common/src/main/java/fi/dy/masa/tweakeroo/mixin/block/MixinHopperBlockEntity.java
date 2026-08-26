package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.MiscUtils;

/**
 * <a href="https://github.com/kikugie/stackable-shulkers-fix">...</a> by KikuGie
 * Priority 999 if installed with stackable-shulkers-fix
 */
@Mixin(value = HopperBlockEntity.class, priority = 999)
public class MixinHopperBlockEntity
{
    @Redirect(
            method = "isFull",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private int modifyShulkerMaxCount(ItemStack instance)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue() &&
            MiscUtils.isShulkerBox(instance))
        {
            return instance.getCount();
        }

        return instance.getMaxCount();
    }

    @Redirect(
            method = "isInventoryFull",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private static int modifyShulkerMaxCountStatic(ItemStack instance)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue() &&
            MiscUtils.isShulkerBox(instance))
        {
            return 1;
        }

        return instance.getMaxCount();
    }

    @Inject(
            method = "canMergeItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelItemMerging(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue())
        {
            if (MiscUtils.isShulkerBox(first) || MiscUtils.isShulkerBox(second)) cir.setReturnValue(false);
        }
    }
}