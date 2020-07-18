package de.lolhens.gameoflife3d

import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

import scala.collection.mutable

class WorldTickPhase() {
  private var ticks = 0

  private def interval: Int = GameOfLife3dMod.interval

  def mark: Boolean = ticks == 0

  def sweep: Boolean = !mark

  def yieldOnce(): Unit =
    if (ticks == interval) ticks -= 1

  def tick(): Unit = {
    ticks = if (ticks >= interval) 0 else ticks + 1
  }
}

object WorldTickPhase {
  private val worldLocal = mutable.WeakHashMap.empty[World, WorldTickPhase]

  def apply(world: World): Option[WorldTickPhase] =
    Some(world).filterNot(_.isClient).map { world =>
      worldLocal.getOrElseUpdate(world, new WorldTickPhase())
    }

  def startWorldTick(world: ServerWorld): Unit = {
    WorldTickPhase(world).foreach(_.tick())
  }
}