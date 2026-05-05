package de.lolhens.minecraft.gameoflife3d.mixin;

import de.lolhens.minecraft.gameoflife3d.util.PistonMovableUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBlockMixin {
    @Inject(at = @At("HEAD"), method = "isPushable", cancellable = true)
    private static void isMovableHead(BlockState state,
                                      Level level,
                                      BlockPos pos,
                                      Direction motionDir,
                                      boolean canBreak,
                                      Direction pistonDir,
                                      CallbackInfoReturnable<Boolean> info) {
        PistonMovableUtil.startIsMovable(state);
    }

    @Inject(at = @At("RETURN"), method = "isPushable", cancellable = true)
    private static void isMovableReturn(BlockState state,
                                        Level level,
                                        BlockPos pos,
                                        Direction motionDir,
                                        boolean canBreak,
                                        Direction pistonDir,
                                        CallbackInfoReturnable<Boolean> info) {
        PistonMovableUtil.endIsMovable();
    }
}
