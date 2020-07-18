package de.lolhens.gameoflife3d.block;

import net.minecraft.block.BlockState;

public interface MovableBlockEntityProvider {
    boolean isMovable(BlockState blockState);
}
