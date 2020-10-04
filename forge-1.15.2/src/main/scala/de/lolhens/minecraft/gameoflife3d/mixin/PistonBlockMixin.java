package de.lolhens.minecraft.gameoflife3d.mixin;

import de.lolhens.minecraft.gameoflife3d.util.PistonMovableUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {
    @Inject(at = @At("HEAD"), method = "canPush", cancellable = true)
    private static void canPushHead(BlockState state,
                                    World world,
                                    BlockPos pos,
                                    Direction motionDir,
                                    boolean canBreak,
                                    Direction pistonDir,
                                    CallbackInfoReturnable<Boolean> info) {
        PistonMovableUtil.startIsMovable(state);
    }

    @Inject(at = @At("RETURN"), method = "canPush", cancellable = true)
    private static void canPushReturn(BlockState state,
                                      World world,
                                      BlockPos pos,
                                      Direction motionDir,
                                      boolean canBreak,
                                      Direction pistonDir,
                                      CallbackInfoReturnable<Boolean> info) {
        PistonMovableUtil.endIsMovable();
    }
}
