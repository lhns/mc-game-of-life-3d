package de.lolhens.minecraft.gameoflife3d.util

import de.lolhens.minecraft.gameoflife3d.block.MovableBlockEntityProvider
import net.minecraft.world.level.block.state.BlockState

object PistonMovableUtil {
  private val localBlockState: ThreadLocal[BlockState] = new ThreadLocal()

  def startIsMovable(blockState: BlockState): Unit = localBlockState.set(blockState)

  def endIsMovable(): Unit = localBlockState.remove()

  // Combines threadlocal lookup, instanceof check, and isMovable call in one step so the
  // mixin only needs a single primitive-returning call — keeping the mod-private
  // MovableBlockEntityProvider type out of the mixin bytecode (which lives in a
  // platform-loaded MC class after injection and can't see mod-private types directly).
  def shouldHideMovableBlockEntity(): Boolean = {
    val s = localBlockState.get()
    s != null && (s.getBlock match {
      case mp: MovableBlockEntityProvider => mp.isMovable(s)
      case _ => false
    })
  }
}
