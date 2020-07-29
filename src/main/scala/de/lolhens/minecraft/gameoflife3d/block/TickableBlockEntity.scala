package de.lolhens.minecraft.gameoflife3d.block

import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.util.Tickable

class TickableBlockEntity(`type`: BlockEntityType[TickableBlockEntity],
                          block: TickableBlock) extends BlockEntity(`type`) with Tickable {
  override def tick(): Unit = block.tick(world, pos)
}
