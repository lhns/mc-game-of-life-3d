package de.lolhens.gameoflife3d.block

import de.lolhens.gameoflife3d.block.CellBlock.State
import de.lolhens.gameoflife3d.{GameOfLife3dMod, WorldTickPhase}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.util.{ActionResult, Hand, StringIdentifiable}
import net.minecraft.world.{BlockView, World}

import scala.jdk.CollectionConverters._

class CellBlock() extends Block(CellBlock.settings) with BlockEntityProvider {
  def getState(state: State): BlockState =
    getStateManager.getDefaultState.`with`(CellBlock.STATE, state)

  setDefaultState(getState(state = State.Inactive))

  override protected def appendProperties(stateManager: StateManager.Builder[Block, BlockState]): Unit =
    stateManager.add(CellBlock.STATE)

  override def createBlockEntity(world: BlockView): BlockEntity =
    GameOfLife3dMod.CELL_BLOCK_ENTITY.instantiate()

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    if (state.get(CellBlock.STATE) == State.Inactive) {
      world.setBlockState(pos, getState(state = State.Alive))
      WorldTickPhase(world).foreach(_.yieldOnce())
      ActionResult.SUCCESS
    } else
      ActionResult.PASS
  }

  override def getVisualShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape =
    VoxelShapes.empty

  @Environment(EnvType.CLIENT)
  override def getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos) = 1.0F

  override def isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true
}

object CellBlock {
  private val settings =
    FabricBlockSettings
      .of(Material.STONE)
      .hardness(2.0F)
      .resistance(6.0F)

  sealed abstract case class State(_name: String,
                                   _ordinal: Int) extends Enum[State](_name, _ordinal) with StringIdentifiable {
    override def asString(): String = name

    def isActive: Boolean

    def isAlive: Boolean
  }

  object State {

    object Inactive extends State("inactive", 0) {
      override def isActive: Boolean = false

      override def isAlive: Boolean = true
    }

    object Alive extends State("alive", 1) {
      override def isActive: Boolean = true

      override def isAlive: Boolean = true
    }

    object ScheduledDead extends State("scheduled_dead", 2) {
      override def isActive: Boolean = true

      override def isAlive: Boolean = true
    }

    object ScheduledAlive extends State("scheduled_alive", 3) {
      override def isActive: Boolean = true

      override def isAlive: Boolean = false
    }

    val values: List[State] = List(Inactive, Alive, ScheduledDead, ScheduledAlive)
  }

  val STATE: EnumProperty[State] = EnumProperty.of("state", classOf[State], State.values.asJavaCollection)
}
