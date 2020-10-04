package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.block.{BlockEntityProvider, BlockState}

trait MovableBlockEntityProvider {
  self: BlockEntityProvider =>

  def isMovable(blockState: BlockState): Boolean
}
