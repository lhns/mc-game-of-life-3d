package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle}
import net.minecraft.core.{BlockPos, Vec3i}
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}

class CellSupportBlock()
  extends Block(CellSupportBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(active: Boolean): BlockState =
    getStateDefinition.any.setValue(CellSupportBlock.ACTIVE, java.lang.Boolean.valueOf(active))

  registerDefaultState(getState(active = false))

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(CellSupportBlock.ACTIVE)

  private def neighborOffsets: Array[Vec3i] = CellBlock.neighborOffsets

  override def tick(world: Level, pos: BlockPos): Unit = {
    def isActive(pos: BlockPos): Boolean =
      CellState.fromBlockState(world.getBlockState(pos), None).exists(_.isActive)

    GameCycle.ofWorld(world).foreach { cycle =>
      val state = world.getBlockState(pos)
      val cellState = CellState.fromBlockState(state, None).get

      if (cycle.sweep) {
        val newState: Option[BlockState] = cellState match {
          case inactive if !inactive.isActive =>
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.offset(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(getState(active = true))
            } else
              None

          case _ =>
            None
        }

        newState.foreach { state =>
          world.setBlockAndUpdate(pos, state)
        }
      } else if (cycle.mark) {
        if (cellState.isActive) {
          world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState)
        }
      }
    }
  }

  override def useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult = {
    if (!CellState.fromBlockState(state, None).get.isActive) {
      level.setBlockAndUpdate(pos, getState(active = true))
      GameCycle.ofWorld(level).foreach(_.blocksActivated())
      InteractionResult.SUCCESS
    } else
      InteractionResult.PASS
  }

  override def getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
    CellSupportBlock.outlineShape

  override def getShadeBrightness(state: BlockState, level: BlockGetter, pos: BlockPos): Float = 1.0F

  override def propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = true
}

object CellSupportBlock {
  private val settings: BlockBehaviour.Properties =
    BlockBehaviour.Properties.of()
      .noOcclusion
      .destroyTime(0.3F)

  private val outlineShape: VoxelShape =
    Shapes.or(
      Block.box(6, 0, 6, 10, 16, 10),
      Block.box(6, 6, 0, 10, 10, 16),
      Block.box(0, 6, 6, 16, 10, 10)
    )

  val ACTIVE: BooleanProperty = BooleanProperty.create("active")
}
