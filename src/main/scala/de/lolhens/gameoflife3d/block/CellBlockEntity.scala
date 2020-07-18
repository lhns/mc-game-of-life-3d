package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.block.CellBlock.State
import de.lolhens.gameoflife3d.{GameOfLife3dMod, GameOfLifeRules, WorldTickPhase}
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.item.{AutomaticItemPlacementContext, ItemStack}
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, Direction, Vec3i}
import net.minecraft.world.WorldView

class CellBlockEntity extends BlockEntity(GameOfLife3dMod.CELL_BLOCK_ENTITY) with Tickable {
  private def isEmpty(world: WorldView, pos: BlockPos): Boolean = {
    val state = world.getBlockState(pos)
    state.canReplace(new AutomaticItemPlacementContext(this.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) ||
      state.isOf(GameOfLife3dMod.CELL_BLOCK) && !state.get(CellBlock.STATE).isAlive
  }

  private def isAlive(world: WorldView, pos: BlockPos): Boolean = {
    val state = world.getBlockState(pos)
    state.isOf(GameOfLife3dMod.CELL_BLOCK) && state.get(CellBlock.STATE).isAlive
  }

  private def rules: GameOfLifeRules = GameOfLife3dMod.rules

  private def neighborOffsets: Array[Vec3i] =
    if (rules.onlyHorizontal) CellBlockEntity.horizontalNeighborOffsets else CellBlockEntity.neighborOffsets

  override def tick(): Unit = {
    val world = getWorld
    WorldTickPhase(world).foreach { phase =>
      val pos = getPos
      val state = world.getBlockState(pos).get(CellBlock.STATE)

      if (phase.sweep) {
        val newState: Option[BlockState] = state match {
          case State.ScheduledDead =>
            Some(Blocks.AIR.getDefaultState)

          case State.ScheduledAlive =>
            Some(GameOfLife3dMod.CELL_BLOCK.getState(state = State.Alive))

          case State.Inactive =>
            val activeNeighbor = neighborOffsets.exists { offset =>
              val state = world.getBlockState(pos.add(offset))
              state.isOf(GameOfLife3dMod.CELL_BLOCK) && state.get(CellBlock.STATE).isActive
            }

            if (activeNeighbor) {
              phase.yieldOnce()
              Some(GameOfLife3dMod.CELL_BLOCK.getState(state = State.Alive))
            } else
              None
          case _ => None
        }

        newState.foreach { state =>
          world.setBlockState(pos, state)
        }
      } else if (phase.mark) {
        if (state.isAlive && state.isActive) {
          val neighbors = neighborOffsets.map(pos.add)
          val neighborCount = neighbors.count(isAlive(world, _))
          if (!rules.remainAlive(neighborCount)) {
            world.setBlockState(pos, GameOfLife3dMod.CELL_BLOCK.getState(state = State.ScheduledDead))
          } else {
            neighbors.foreach { neighbor =>
              if (isEmpty(world, neighbor)) {
                val neighborNeighborCount = neighborOffsets.count(e => isAlive(world, neighbor.add(e)))
                if (rules.becomeAlive(neighborNeighborCount)) {
                  world.setBlockState(neighbor, GameOfLife3dMod.CELL_BLOCK.getState(state = State.ScheduledAlive))
                }
              }
            }
          }
        }
      }
    }
  }
}

object CellBlockEntity {
  private val neighborOffsets: Array[Vec3i] =
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
