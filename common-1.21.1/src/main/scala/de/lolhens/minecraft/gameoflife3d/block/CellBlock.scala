package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle, GameRules}
import net.minecraft.core.{BlockPos, Direction, Vec3i}
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.level.block.{Block, Blocks, TransparentBlock}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}

import scala.jdk.CollectionConverters._

class CellBlock(val rules: GameRules)
  extends TransparentBlock(CellBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(state: CellState): BlockState =
    getStateDefinition.any.setValue(CellBlock.STATE, state)

  registerDefaultState(getState(state = CellState.Inactive))

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(CellBlock.STATE)

  private def neighborOffsets: Array[Vec3i] =
    if (rules.onlyHorizontal) CellBlock.horizontalNeighborOffsets else CellBlock.neighborOffsets

  override def tick(world: Level, pos: BlockPos): Unit = {
    def isEmpty(pos: BlockPos): Boolean = {
      val state = world.getBlockState(pos)
      state.canBeReplaced ||
        CellState.fromBlockState(state, Some(this)).exists(!_.isAlive)
    }

    def isActive(pos: BlockPos): Boolean =
      CellState.fromBlockState(world.getBlockState(pos), Some(this)).exists(_.isActive)

    def isAlive(pos: BlockPos): Boolean =
      CellState.fromBlockState(world.getBlockState(pos), Some(this)).exists(_.isAlive)

    GameCycle.ofWorld(world).foreach { cycle =>
      val state = world.getBlockState(pos)
      val cellState = CellState.fromBlockState(state, Some(this)).get

      if (cycle.sweep) {
        val newState: Option[BlockState] = cellState match {
          case inactive if !inactive.isActive =>
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.offset(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(getState(state = CellState.Alive))
            } else
              None

          case CellState.ScheduledDead =>
            Some(Blocks.AIR.defaultBlockState)

          case CellState.ScheduledAlive =>
            Some(getState(state = CellState.Alive))

          case _ =>
            None
        }

        newState.foreach { state =>
          world.setBlockAndUpdate(pos, state)
        }
      } else if (cycle.mark) {
        if (cellState.isAlive && cellState.isActive) {
          val neighbors = neighborOffsets.map[BlockPos](pos.offset)
          val neighborCount = neighbors.count(isAlive)
          if (!rules.remainAlive(neighborCount)) {
            world.setBlockAndUpdate(pos, getState(state = CellState.ScheduledDead))
          }

          neighbors.foreach { neighbor =>
            if (isEmpty(neighbor)) {
              val neighborNeighborCount = neighborOffsets.count(e => isAlive(neighbor.offset(e)))
              if (rules.becomeAlive(neighborNeighborCount)) {
                world.setBlockAndUpdate(neighbor, getState(state = CellState.ScheduledAlive))
              }
            }
          }
        }
      }
    }
  }

  override def useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult = {
    if (!CellState.fromBlockState(state, Some(this)).get.isActive) {
      level.setBlockAndUpdate(pos, getState(state = CellState.Alive))
      GameCycle.ofWorld(level).foreach(_.blocksActivated())
      InteractionResult.SUCCESS
    } else
      InteractionResult.PASS
  }

  override def getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
    if (rules.onlyHorizontal) CellBlock.flatOutlineShape else Shapes.block

  override def getShadeBrightness(state: BlockState, level: BlockGetter, pos: BlockPos): Float = 1.0F

  override def propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = true
}

object CellBlock {
  private val settings: BlockBehaviour.Properties =
    BlockBehaviour.Properties.of()
      .noOcclusion
      .destroyTime(0.3F)

  private val flatOutlineShape: VoxelShape =
    Block.box(0, 4, 0, 16, 12, 16)

  val STATE: EnumProperty[CellState] = EnumProperty.create("state", classOf[CellState], CellState.blockstateValues.asJavaCollection)

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
