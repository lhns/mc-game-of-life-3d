package de.lolhens.minecraft.gameoflife3d.util

import net.minecraft.world.level.block.state.BlockState

object PistonMovableUtil {
  private val localBlockState: ThreadLocal[BlockState] = new ThreadLocal()

  def startIsMovable(blockState: BlockState): Unit = localBlockState.set(blockState)

  def endIsMovable(): Unit = localBlockState.remove()

  def isMovableBlockState: BlockState = localBlockState.get()
}
