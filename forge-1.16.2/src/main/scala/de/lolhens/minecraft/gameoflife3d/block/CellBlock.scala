package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle, GameRules}
import net.minecraft.block._
import net.minecraft.block.material.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{DirectionalPlaceContext, ItemStack}
import net.minecraft.state.{EnumProperty, StateContainer}
import net.minecraft.util.math.shapes.{ISelectionContext, VoxelShape, VoxelShapes}
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{ActionResultType, Direction, Hand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

import scala.jdk.CollectionConverters._

class CellBlock(val rules: GameRules)
  extends BreakableBlock(CellBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(state: CellState): BlockState =
    getStateContainer.getBaseState.`with`(CellBlock.STATE, state)

  setDefaultState(getState(state = CellState.Inactive))

  override protected def fillStateContainer(stateManager: StateContainer.Builder[Block, BlockState]): Unit =
    stateManager.add(CellBlock.STATE)

  private def neighborOffsets: Array[Vector3i] =
    if (rules.onlyHorizontal) CellBlock.horizontalNeighborOffsets else CellBlock.neighborOffsets

  override def tick(world: World, pos: BlockPos): Unit = {
    def isEmpty(pos: BlockPos): Boolean = {
      val state = world.getBlockState(pos)
      state.isReplaceable(new DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) ||
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
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.add(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(getState(state = CellState.Alive))
            } else
              None

          case CellState.ScheduledDead =>
            Some(Blocks.AIR.getDefaultState)

          case CellState.ScheduledAlive =>
            Some(getState(state = CellState.Alive))

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
            world.setBlockState(pos, getState(state = CellState.ScheduledDead))
          }

          neighbors.foreach { neighbor =>
            if (isEmpty(neighbor)) {
              val neighborNeighborCount = neighborOffsets.count(e => isAlive(neighbor.add(e)))
              if (rules.becomeAlive(neighborNeighborCount)) {
                world.setBlockState(neighbor, getState(state = CellState.ScheduledAlive))
              }
            }
          }
        }
      }
    }
  }

  override def onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType = {
    if (!CellState.fromBlockState(state, Some(this)).get.isActive) {
      world.setBlockState(pos, getState(state = CellState.Alive))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResultType.SUCCESS
    } else
      ActionResultType.PASS
  }


  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape =
    if (rules.onlyHorizontal) CellBlock.flatOutlineShape else VoxelShapes.fullCube

  @OnlyIn(Dist.CLIENT)
  override def getAmbientOcclusionLightValue(state: BlockState, world: IBlockReader, pos: BlockPos) = 1.0F

  override def propagatesSkylightDown(state: BlockState, world: IBlockReader, pos: BlockPos) = true
}

object CellBlock {
  private val settings =
    AbstractBlock.Properties
      .create(Material.ROCK)
      .notSolid()
      .hardnessAndResistance(0.3F)

  private val flatOutlineShape: VoxelShape =
    Block.makeCuboidShape(0, 4, 0, 16, 12, 16)

  val STATE: EnumProperty[CellState] = EnumProperty.create("state", classOf[CellState], CellState.values.asJavaCollection)

  private[gameoflife3d] val neighborOffsets: Array[Vector3i] =
    (for {
      z <- -1 to 1
      y <- -1 to 1
      x <- -1 to 1
      if !(x == 0 && y == 0 && z == 0)
    } yield
      new Vector3i(x, y, z))
      .toArray

  private val horizontalNeighborOffsets: Array[Vector3i] =
    (for {
      z <- -1 to 1
      x <- -1 to 1
      if !(x == 0 && z == 0)
    } yield
      new Vector3i(x, 0, z))
      .toArray
}
