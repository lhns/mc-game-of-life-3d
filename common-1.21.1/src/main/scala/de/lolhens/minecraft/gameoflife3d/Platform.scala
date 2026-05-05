package de.lolhens.minecraft.gameoflife3d

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ItemLike

import scala.compiletime.uninitialized

/** Platform abstraction so the common module can register events and add items to creative
  * tabs without depending on Fabric API or the NeoForge event bus. The platform modules call
  * `Platform.register(...)` from their entry point before `GameOfLife3dMod.init()` runs.
  */
trait Platform {
  def onServerLevelStartTick(cb: ServerLevel => Unit): Unit
  def onServerLevelEndTick(cb: ServerLevel => Unit): Unit

  /** Add the given items to the BUILDING_BLOCKS creative tab. Each platform wires the
    * appropriate event (Fabric: ItemGroupEvents; NeoForge: BuildCreativeModeTabContentsEvent).
    */
  def addToBuildingBlocksTab(items: Seq[ItemLike]): Unit
}

object Platform {
  @volatile private var _instance: Platform = uninitialized

  def instance: Platform = {
    val p = _instance
    if (p == null) throw new IllegalStateException("Platform not registered yet — call Platform.register from the platform entry point before GameOfLife3dMod.init()")
    p
  }

  def register(p: Platform): Unit = _instance = p
}
