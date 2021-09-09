package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait TickableBlock {
  def tick(world: World, pos: BlockPos): Unit
}

object TickableBlock {

  trait BlockEntityProvider extends TickableBlock with net.minecraft.block.BlockEntityProvider with MovableBlockEntityProvider {
    self: Block =>

    val blockEntityType: BlockEntityType[BlockEntity] =
      BlockEntityType.Builder.create((pos, state) => new BlockEntity(blockEntityType, pos, state) {}, this)
        .build(null)

    override def createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = blockEntityType.instantiate(pos, state)

    override def getTicker[T <: BlockEntity](
                                              world: World,
                                              state: BlockState,
                                              `type`: BlockEntityType[T]
                                            ): BlockEntityTicker[T] = { (world, pos, _, _) =>
      self.tick(world, pos)
    }

    override def isMovable(blockState: BlockState): Boolean = true
  }

}
