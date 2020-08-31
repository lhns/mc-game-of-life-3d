package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle}
import net.minecraft.block._
import net.minecraft.block.material.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.{BooleanProperty, StateContainer}
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.math.shapes.{ISelectionContext, VoxelShape, VoxelShapes}
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.util.{ActionResult, ActionResultType, Hand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

class CellSupportBlock()
  extends Block(CellSupportBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(active: Boolean): BlockState =
    getStateContainer.getBaseState.`with`(CellSupportBlock.ACTIVE, java.lang.Boolean.valueOf(active))

  setDefaultState(getState(active = false))

  override protected def fillStateContainer(stateManager: StateContainer.Builder[Block, BlockState]): Unit =
    stateManager.add(CellSupportBlock.ACTIVE)

  private def neighborOffsets: Array[Vector3i] = CellBlock.neighborOffsets

  override def tick(world: World, pos: BlockPos): Unit = {
    def isActive(pos: BlockPos): Boolean =
      CellState.fromBlockState(world.getBlockState(pos), None).exists(_.isActive)

    GameCycle.ofWorld(world).foreach { cycle =>
      val state = world.getBlockState(pos)
      val cellState = CellState.fromBlockState(state, None).get

      if (cycle.sweep) {
        val newState: Option[BlockState] = cellState match {
          case inactive if !inactive.isActive =>
            val activeNeighbor = neighborOffsets.exists(offset => isActive(pos.add(offset)))

            if (activeNeighbor) {
              cycle.blocksActivated()
              Some(getState(active = true))
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

  override def onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType = {
    if (!CellState.fromBlockState(state, None).get.isActive) {
      world.setBlockState(pos, getState(active = true))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResultType.SUCCESS
    } else
      ActionResultType.PASS
  }


  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape =
    CellSupportBlock.outlineShape

  @OnlyIn(Dist.CLIENT)
  override def getAmbientOcclusionLightValue(state: BlockState, world: IBlockReader, pos: BlockPos) = 1.0F

  override def propagatesSkylightDown(state: BlockState, world: IBlockReader, pos: BlockPos) = true
}

object CellSupportBlock {
  private val settings =
    AbstractBlock.Properties
      .create(Material.ROCK)
      .notSolid()
      .hardnessAndResistance(0.3F)

  private val outlineShape: VoxelShape =
    VoxelShapes.or(
      Block.makeCuboidShape(6, 0, 6, 10, 16, 10),
      Block.makeCuboidShape(6, 6, 0, 10, 10, 16),
      Block.makeCuboidShape(0, 6, 6, 16, 10, 10)
    )

  val ACTIVE: BooleanProperty = BooleanProperty.create("active")
}

