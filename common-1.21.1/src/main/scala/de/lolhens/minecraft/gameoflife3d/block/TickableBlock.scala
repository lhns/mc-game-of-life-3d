package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{Block, EntityBlock}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

trait TickableBlock {
  def tick(world: Level, pos: BlockPos): Unit
}

object TickableBlock {

  trait BlockEntityProvider extends TickableBlock with EntityBlock with MovableBlockEntityProvider {
    self: Block =>

    val blockEntityType: BlockEntityType[BlockEntity] =
      BlockEntityType.Builder.of((pos, state) => new BlockEntity(blockEntityType, pos, state) {}, this)
        .build(null)

    override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = blockEntityType.create(pos, state)

    override def getTicker[T <: BlockEntity](
                                              world: Level,
                                              state: BlockState,
                                              `type`: BlockEntityType[T]
                                            ): BlockEntityTicker[T] = { (world, pos, _, _) =>
      self.tick(world, pos)
    }

    override def isMovable(blockState: BlockState): Boolean = true
  }

}
