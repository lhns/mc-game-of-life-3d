package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState

trait MovableBlockEntityProvider {
  self: EntityBlock =>

  def isMovable(blockState: BlockState): Boolean
}
