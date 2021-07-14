package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle, GameRules}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{AutomaticItemPlacementContext, ItemStack}
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.{BlockPos, Direction, Vec3i}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.util.{ActionResult, Hand}
import net.minecraft.world.{BlockView, World}

import scala.jdk.CollectionConverters._

class CellBlock(val rules: GameRules)
  extends TransparentBlock(CellBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(state: CellState): BlockState =
    getStateManager.getDefaultState.`with`(CellBlock.STATE, state)

  setDefaultState(getState(state = CellState.Inactive))

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(CellBlock.STATE)

  private def neighborOffsets: Array[Vec3i] =
    if (rules.onlyHorizontal) CellBlock.horizontalNeighborOffsets else CellBlock.neighborOffsets

  override def tick(world: World, pos: BlockPos): Unit = {
    def isEmpty(pos: BlockPos): Boolean = {
      val state = world.getBlockState(pos)
      state.canReplace(new AutomaticItemPlacementContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) ||
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
          val neighbors = neighborOffsets.map[BlockPos](pos.add)
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

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    if (!CellState.fromBlockState(state, Some(this)).get.isActive) {
      world.setBlockState(pos, getState(state = CellState.Alive))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResult.SUCCESS
    } else
      ActionResult.PASS
  }


  override def getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    if (rules.onlyHorizontal) CellBlock.flatOutlineShape else VoxelShapes.fullCube

  @Environment(EnvType.CLIENT)
  override def getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos) = 1.0F

  override def isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true
}

object CellBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.STONE)
      .nonOpaque()
      .hardness(0.3F)

  private val flatOutlineShape: VoxelShape =
    Block.createCuboidShape(0, 4, 0, 16, 12, 16)

  val STATE: EnumProperty[CellState] = EnumProperty.of("state", classOf[CellState], CellState.values.asJavaCollection)

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
