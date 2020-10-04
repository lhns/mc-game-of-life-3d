package de.lolhens.minecraft.gameoflife3d.block

import de.lolhens.minecraft.gameoflife3d.game.{CellState, GameCycle}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.{BlockPos, Vec3i}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.util.{ActionResult, Hand}
import net.minecraft.world.{BlockView, World}

class CellSupportBlock()
  extends Block(CellSupportBlock.settings) with TickableBlock.BlockEntityProvider {

  def getState(active: Boolean): BlockState =
    getStateManager.getDefaultState.`with`(CellSupportBlock.ACTIVE, java.lang.Boolean.valueOf(active))

  setDefaultState(getState(active = false))

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(CellSupportBlock.ACTIVE)

  private def neighborOffsets: Array[Vec3i] = CellBlock.neighborOffsets

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

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    if (!CellState.fromBlockState(state, None).get.isActive) {
      world.setBlockState(pos, getState(active = true))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResult.SUCCESS
    } else
      ActionResult.PASS
  }


  override def getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    CellSupportBlock.outlineShape

  @Environment(EnvType.CLIENT)
  override def getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos) = 1.0F

  override def isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true
}

object CellSupportBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.STONE)
      .nonOpaque()
      .hardness(0.3F)

  private val outlineShape: VoxelShape =
    VoxelShapes.union(
      Block.createCuboidShape(6, 0, 6, 10, 16, 10),
      Block.createCuboidShape(6, 6, 0, 10, 10, 16),
      Block.createCuboidShape(0, 6, 6, 16, 10, 10)
    )

  val ACTIVE: BooleanProperty = BooleanProperty.of("active")
}
