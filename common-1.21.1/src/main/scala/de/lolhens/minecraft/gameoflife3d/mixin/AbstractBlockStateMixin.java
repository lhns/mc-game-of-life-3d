package de.lolhens.minecraft.gameoflife3d.mixin;

import de.lolhens.minecraft.gameoflife3d.util.PistonMovableUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {
    @Inject(at = @At("HEAD"), method = "hasBlockEntity", cancellable = true)
    public void hasBlockEntity(CallbackInfoReturnable<Boolean> info) {
        if (PistonMovableUtil.shouldHideMovableBlockEntity()) {
            info.setReturnValue(false);
        }
    }
}
