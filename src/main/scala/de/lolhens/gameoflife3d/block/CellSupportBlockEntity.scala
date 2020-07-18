package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.GameOfLife3dMod
import de.lolhens.gameoflife3d.game.{CellState, GameCycle}
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, Vec3i}

class CellSupportBlockEntity() extends BlockEntity(GameOfLife3dMod.cellSupportBlockEntity) with Tickable {
  private val block = GameOfLife3dMod.cellSupportBlock

  private def isActive(pos: BlockPos): Boolean =
    CellState.fromBlockState(world.getBlockState(pos), None).exists(_.isActive)

  private def neighborOffsets: Array[Vec3i] = CellBlockEntity.neighborOffsets

  override def tick(): Unit = {
    val world = getWorld
    GameCycle.ofWorld(world).foreach { cycle =>
      val pos = getPos
      val state = world.getBlockState(pos)
      val cellState = CellState.fromBlockState(state, None).get

      if (cycle.sweep) {
        val newState: Option[BlockState] = cellState match {
          case inactive if !inactive.isActive =>
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.add(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(block.getState(active = true))
            } else
              None

          case _ =>
            None
        }

        newState.foreach { state =>
          world.setBlockState(pos, state)
        }
      } else if (cycle.mark) {
        if (cellState.isActive) {
          world.setBlockState(pos, Blocks.AIR.getDefaultState)
        }
      }
    }
  }
}


