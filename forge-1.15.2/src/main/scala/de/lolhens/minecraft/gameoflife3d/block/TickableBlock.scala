package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.block.{Block, BlockState}
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockReader, World}

trait TickableBlock {
  def tick(world: World, pos: BlockPos): Unit
}

object TickableBlock {

  trait BlockEntityProvider extends TickableBlock with net.minecraft.block.ITileEntityProvider with MovableBlockEntityProvider {
    self: Block =>

    val blockEntityType: TileEntityType[TickableBlockEntity] =
      TileEntityType.Builder.create(() => new TickableBlockEntity(blockEntityType, this), this)
        .build(null)

    override def createNewTileEntity(world: IBlockReader): TileEntity = blockEntityType.create()

    override def isMovable(blockState: BlockState): Boolean = true
  }

}
