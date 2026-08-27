package fi.dy.masa.tweakeroo.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import fi.dy.masa.tweakeroo.util.IDecorationEntity;

@Mixin(AbstractDecorationEntity.class)
public abstract class MixinAbstractDecorationEntity extends Entity implements IDecorationEntity
{
    @Shadow protected BlockPos attachmentPos;

    public MixinAbstractDecorationEntity(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Override
    public BlockPos tweakeroo$getAttached()
    {
        return this.attachmentPos;
    }
}
