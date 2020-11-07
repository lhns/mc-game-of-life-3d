package de.lolhens.minecraft.gameoflife3d.game

import de.lolhens.minecraft.gameoflife3d.GameOfLife3dMod
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

import scala.collection.mutable

class GameCycle() {
  private var ticks = 0
  private var shouldSweepAgain = false
  private var sweepAgain = false

  private def interval: Int = GameOfLife3dMod.interval

  def mark: Boolean = !sweepAgain && ticks == interval

  def sweep: Boolean = sweepAgain || ticks == 0

  def blocksActivated(): Unit = {
    shouldSweepAgain = true
  }

  def startTick(): Unit = {
    if (shouldSweepAgain) {
      shouldSweepAgain = false
      sweepAgain = true
    }
  }

  def endTick(): Unit = {
    if (!(ticks == interval && sweepAgain)) {
      ticks = if (ticks >= interval) 0 else ticks + 1
    }
    sweepAgain = false
  }
}

object GameCycle {
  private val worldCycles = mutable.WeakHashMap.empty[World, GameCycle]

  def ofWorld(world: World): Option[GameCycle] =
    Some(world).filterNot(_.isClient).map { world =>
      worldCycles.getOrElseUpdate(world, new GameCycle())
    }

  def startWorldTick(world: ServerWorld): Unit = {
    GameCycle.ofWorld(world).foreach(_.startTick())
  }

  def endWorldTick(world: ServerWorld): Unit = {
    GameCycle.ofWorld(world).foreach(_.endTick())
  }
}