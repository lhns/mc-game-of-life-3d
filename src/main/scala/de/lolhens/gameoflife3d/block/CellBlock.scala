package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.game.{CellState, GameCycle, GameRules}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResult, Hand}
import net.minecraft.world.{BlockView, World}

import scala.jdk.CollectionConverters._

class CellBlock(createBlockEntity: BlockView => BlockEntity,
                val rules: GameRules)
  extends Block(CellBlock.settings) with BlockEntityProvider with MovableBlockEntityProvider {

  def getState(state: CellState): BlockState =
    getStateManager.getDefaultState.`with`(CellBlock.STATE, state)

  setDefaultState(getState(state = CellState.Inactive))

  override def isMovable(blockState: BlockState): Boolean = true

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(CellBlock.STATE)

  override def createBlockEntity(world: BlockView): BlockEntity = createBlockEntity.apply(world)

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    if (!CellState.fromBlockState(state, Some(this)).get.isActive) {
      world.setBlockState(pos, getState(state = CellState.Alive))
      GameCycle.ofWorld(world).foreach(_.blocksActivated())
      ActionResult.SUCCESS
    } else
      ActionResult.PASS
  }
}

object CellBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.STONE)
      .hardness(2.0F)
      .resistance(6.0F)

  val STATE: EnumProperty[CellState] = EnumProperty.of("state", classOf[CellState], CellState.values.asJavaCollection)
}
