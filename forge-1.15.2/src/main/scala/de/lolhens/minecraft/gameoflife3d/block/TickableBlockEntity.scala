package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.tileentity.{ITickableTileEntity, TileEntity, TileEntityType}

class TickableBlockEntity(`type`: TileEntityType[TickableBlockEntity],
                          block: TickableBlock) extends TileEntity(`type`) with ITickableTileEntity {
  override def tick(): Unit = block.tick(world, pos)
}
