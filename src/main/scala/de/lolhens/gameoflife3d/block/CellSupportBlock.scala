package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.GameOfLife3dMod
import de.lolhens.gameoflife3d.game.{CellState, GameCycle}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.util.{ActionResult, Hand}
import net.minecraft.world.{BlockView, World}

class CellSupportBlock()
  extends Block(CellSupportBlock.settings) with BlockEntityProvider with MovableBlockEntityProvider {

  def getState(active: Boolean): BlockState =
    getStateManager.getDefaultState.`with`(CellSupportBlock.ACTIVE, java.lang.Boolean.valueOf(active))

  setDefaultState(getState(active = false))

  override def isMovable(blockState: BlockState): Boolean = true

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(CellSupportBlock.ACTIVE)

  override def createBlockEntity(world: BlockView): BlockEntity =
    GameOfLife3dMod.cellSupportBlockEntity.instantiate()

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    if (!CellState.fromBlockState(state, None).get.isActive) {
      world.setBlockState(pos, getState(active = true))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResult.SUCCESS
    } else
      ActionResult.PASS
  }

  override def getVisualShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    VoxelShapes.empty

  override def getCullingShape(state: BlockState, world: BlockView, pos: BlockPos): VoxelShape =
    VoxelShapes.empty

  @Environment(EnvType.CLIENT)
  override def getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos) = 1.0F

  override def isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true

  @Environment(EnvType.CLIENT)
  override def isSideInvisible(state: BlockState, stateFrom: BlockState, direction: Direction): Boolean =
    if (stateFrom.isOf(this)) true else super.isSideInvisible(state, stateFrom, direction)
}

object CellSupportBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.STONE)
      .hardness(2.0F)
      .resistance(6.0F)

  val ACTIVE: BooleanProperty = BooleanProperty.of("active")
}

