package de.lolhens.minecraft.gameoflife3d.game

import de.lolhens.minecraft.gameoflife3d.GameOfLife3dMod
import de.lolhens.minecraft.gameoflife3d.block.{CellBlock, CellSupportBlock}
import net.minecraft.block.BlockState
import net.minecraft.util.StringIdentifiable

sealed abstract case class CellState(_name: String,
                                     _ordinal: Int) extends Enum[CellState](_name, _ordinal) with StringIdentifiable {
  override def asString(): String = name

  def isActive: Boolean

  def isAlive: Boolean
}

object CellState {

  object Inactive extends CellState("inactive", 0) {
    override def isActive: Boolean = false

    override def isAlive: Boolean = true
  }

  object Alive extends CellState("alive", 1) {
    override def isActive: Boolean = true

    override def isAlive: Boolean = true
  }

  object ScheduledDead extends CellState("scheduled_dead", 2) {
    override def isActive: Boolean = true

    override def isAlive: Boolean = true
  }

  object ScheduledAlive extends CellState("scheduled_alive", 3) {
    override def isActive: Boolean = true

    override def isAlive: Boolean = false
  }

  object DummyDeadInactive extends CellState("dummy", 4) {
    override def isActive: Boolean = false

    override def isAlive: Boolean = false
  }

  val values: List[CellState] = List(Inactive, Alive, ScheduledDead, ScheduledAlive)

  def fromBlockState(state: BlockState, block: Option[CellBlock]): Option[CellState] = {
    if (block.exists(state.isOf) || (block.isEmpty && state.getBlock.isInstanceOf[CellBlock]))
      Some(state.get(CellBlock.STATE))
    else if (state.isOf(GameOfLife3dMod.cellSupportBlock))
      Some(if (state.get(CellSupportBlock.ACTIVE)) ScheduledAlive else DummyDeadInactive)
    else
      None
  }
}