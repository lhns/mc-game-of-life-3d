package de.lolhens.minecraft.gameoflife3d.game

import de.lolhens.minecraft.gameoflife3d.GameOfLife3dMod
import de.lolhens.minecraft.gameoflife3d.block.{CellBlock, CellSupportBlock}
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.block.state.BlockState

// Scala 3 `enum` extends java.lang.Enum at bytecode level — required by Mojang's
// EnumProperty.create(name, Class, Collection<T>) which constrains T <: Enum<T>.
enum CellState(val serializedName: String,
               val isActive: Boolean,
               val isAlive: Boolean)
  extends Enum[CellState] with StringRepresentable {

  override def getSerializedName: String = serializedName

  case Inactive extends CellState("inactive", false, true)
  case Alive extends CellState("alive", true, true)
  case ScheduledDead extends CellState("scheduled_dead", true, true)
  case ScheduledAlive extends CellState("scheduled_alive", true, false)
  // Logical placeholder when a CellSupportBlock is queried as a CellState — never put into
  // a block-state property (omitted from blockstateValues below).
  case DummyDeadInactive extends CellState("dummy", false, false)
}

object CellState {
  // Cases registered as the EnumProperty values. DummyDeadInactive is intentionally excluded.
  val blockstateValues: List[CellState] = List(Inactive, Alive, ScheduledDead, ScheduledAlive)

  def fromBlockState(state: BlockState, block: Option[CellBlock]): Option[CellState] = {
    if (block.exists(state.is) || (block.isEmpty && state.getBlock.isInstanceOf[CellBlock]))
      Some(state.getValue[CellState](CellBlock.STATE))
    else if (state.is(GameOfLife3dMod.cellSupportBlock))
      Some(if (state.getValue[java.lang.Boolean](CellSupportBlock.ACTIVE).booleanValue) ScheduledAlive else DummyDeadInactive)
    else
      None
  }
}
