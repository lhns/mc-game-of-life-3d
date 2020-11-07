package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.block.{BlockState, ITileEntityProvider}

trait MovableBlockEntityProvider {
  self: ITileEntityProvider =>

  def isMovable(blockState: BlockState): Boolean
}
