package de.lolhens.gameoflife3d.block

import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{BlockView, World}

trait TickableBlock {
  def tick(world: World, pos: BlockPos): Unit
}

object TickableBlock {

  trait BlockEntityProvider extends TickableBlock with net.minecraft.block.BlockEntityProvider with MovableBlockEntityProvider {
    self: Block =>

    val blockEntityType: BlockEntityType[TickableBlockEntity] =
      BlockEntityType.Builder.create(() => new TickableBlockEntity(blockEntityType, this), this)
        .build(null)

    override def createBlockEntity(world: BlockView): BlockEntity = blockEntityType.instantiate()

    override def isMovable(blockState: BlockState): Boolean = true
  }

}
