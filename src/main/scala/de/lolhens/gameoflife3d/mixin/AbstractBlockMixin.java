package de.lolhens.gameoflife3d.mixin;

import de.lolhens.gameoflife3d.block.MovableBlockEntityProvider;
import de.lolhens.gameoflife3d.util.PistonMovableUtil;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.block.AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Inject(at = @At("HEAD"), method = "hasBlockEntity", cancellable = true)
    public void hasBlockEntity(CallbackInfoReturnable<Boolean> info) {
        BlockState state = PistonMovableUtil.isMovableBlockState();
        if (state != null &&
                state.getBlock() instanceof MovableBlockEntityProvider &&
                ((MovableBlockEntityProvider) state.getBlock()).isMovable(state)) {
            info.setReturnValue(false);
        }
    }
}
