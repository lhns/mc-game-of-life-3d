package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.game.{CellState, GameCycle, GameRules}
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.item.{AutomaticItemPlacementContext, ItemStack}
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, Direction, Vec3i}

class CellBlockEntity(`type`: BlockEntityType[CellBlockEntity], block: CellBlock) extends BlockEntity(`type`) with Tickable {
  private def isEmpty(pos: BlockPos): Boolean = {
    val state = world.getBlockState(pos)
    state.canReplace(new AutomaticItemPlacementContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) ||
      CellState.fromBlockState(state, Some(block)).exists(!_.isAlive)
  }

  private def isActive(pos: BlockPos): Boolean =
    CellState.fromBlockState(world.getBlockState(pos), Some(block)).exists(_.isActive)

  private def isAlive(pos: BlockPos): Boolean =
    CellState.fromBlockState(world.getBlockState(pos), Some(block)).exists(_.isAlive)

  private def rules: GameRules = block.rules

  private def neighborOffsets: Array[Vec3i] =
    if (rules.onlyHorizontal) CellBlockEntity.horizontalNeighborOffsets else CellBlockEntity.neighborOffsets

  override def tick(): Unit = {
    val world = getWorld
    GameCycle.ofWorld(world).foreach { cycle =>
      val pos = getPos
      val state = world.getBlockState(pos)
      val cellState = CellState.fromBlockState(state, Some(block)).get

      if (cycle.sweep) {
        val newState: Option[BlockState] = cellState match {
          case inactive if !inactive.isActive =>
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.add(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(block.getState(state = CellState.Alive))
            } else
              None

          case CellState.ScheduledDead =>
            Some(Blocks.AIR.getDefaultState)

          case CellState.ScheduledAlive =>
            Some(block.getState(state = CellState.Alive))

          case _ =>
            None
        }

        newState.foreach { state =>
          world.setBlockState(pos, state)
        }
      } else if (cycle.mark) {
        if (cellState.isAlive && cellState.isActive) {
          val neighbors = neighborOffsets.map(pos.add)
          val neighborCount = neighbors.count(isAlive)
          if (!rules.remainAlive(neighborCount)) {
            world.setBlockState(pos, block.getState(state = CellState.ScheduledDead))
          }

          neighbors.foreach { neighbor =>
            if (isEmpty(neighbor)) {
              val neighborNeighborCount = neighborOffsets.count(e => isAlive(neighbor.add(e)))
              if (rules.becomeAlive(neighborNeighborCount)) {
                world.setBlockState(neighbor, block.getState(state = CellState.ScheduledAlive))
              }
            }
          }
        }
      }
    }
  }
}

object CellBlockEntity {
  private[gameoflife3d] val neighborOffsets: Array[Vec3i] =
    (for {
      z <- -1 to 1
      y <- -1 to 1
      x <- -1 to 1
      if !(x == 0 && y == 0 && z == 0)
    } yield
      new Vec3i(x, y, z))
      .toArray

  private val horizontalNeighborOffsets: Array[Vec3i] =
    (for {
      z <- -1 to 1
      x <- -1 to 1
      if !(x == 0 && z == 0)
    } yield
      new Vec3i(x, 0, z))
      .toArray
}
