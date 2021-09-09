package de.lolhens.minecraft.gameoflife3d.mixin;

import de.lolhens.minecraft.gameoflife3d.block.MovableBlockEntityProvider;
import de.lolhens.minecraft.gameoflife3d.util.PistonMovableUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
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
