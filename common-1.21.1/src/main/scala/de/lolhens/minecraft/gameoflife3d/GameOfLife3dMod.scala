package de.lolhens.minecraft.gameoflife3d

import de.lolhens.minecraft.gameoflife3d.block.{CellBlock, CellSupportBlock}
import de.lolhens.minecraft.gameoflife3d.game.{GameCycle, GameRules}
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.resources.{ResourceKey, ResourceLocation}
import net.minecraft.world.item.{BlockItem, Item}
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType

object GameOfLife3dMod {
  val id: String = "gameoflife3d"

  def interval: Int = 10

  private def rl(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(id, path)

  // Build cell variants. Block instances are constructed eagerly; registration is deferred
  // (Fabric: at init; NeoForge: inside RegisterEvent).
  private final case class CellVariant(
    name: String,
    block: CellBlock,
    blockKey: ResourceKey[Block],
    itemKey: ResourceKey[Item]
  )

  private def makeCellVariant(name: String, rules: GameRules): CellVariant = {
    val block = new CellBlock(rules)
    CellVariant(
      name = name,
      block = block,
      blockKey = ResourceKey.create(Registries.BLOCK, rl(name)),
      itemKey = ResourceKey.create(Registries.ITEM, rl(name))
    )
  }

  private val conway: CellVariant = makeCellVariant("cell_conway", GameRules.ConwaysRules)
  private val bays: CellVariant = makeCellVariant("cell_bays", GameRules.BaysRules5766)

  val cellConwayBlock: CellBlock = conway.block
  val cellBaysBlock: CellBlock = bays.block

  val cellSupportBlock: CellSupportBlock = new CellSupportBlock()
  private val cellSupportBlockKey: ResourceKey[Block] = ResourceKey.create(Registries.BLOCK, rl("cell_support"))
  private val cellSupportItemKey: ResourceKey[Item] = ResourceKey.create(Registries.ITEM, rl("cell_support"))

  /** Register all blocks. Platform-specific timing:
    *  - Fabric: call during onInitialize.
    *  - NeoForge: call inside RegisterEvent for Registries.BLOCK.
    */
  def registerBlocks(): Unit = {
    Registry.register(BuiltInRegistries.BLOCK, conway.blockKey, conway.block)
    Registry.register(BuiltInRegistries.BLOCK, bays.blockKey, bays.block)
    Registry.register(BuiltInRegistries.BLOCK, cellSupportBlockKey, cellSupportBlock)
  }

  /** Register all block-entity types. Platform-specific timing:
    *  - Fabric: call during onInitialize.
    *  - NeoForge: call inside RegisterEvent for Registries.BLOCK_ENTITY_TYPE.
    */
  def registerBlockEntities(): Unit = {
    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl(conway.name), conway.block.blockEntityType)
    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl(bays.name), bays.block.blockEntityType)
    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, rl("cell_support"), cellSupportBlock.blockEntityType)
  }

  /** Register all BlockItems. Platform-specific timing:
    *  - Fabric: call during onInitialize.
    *  - NeoForge: call inside RegisterEvent for Registries.ITEM.
    */
  def registerItems(): Unit = {
    Registry.register(BuiltInRegistries.ITEM, conway.itemKey, new BlockItem(conway.block, new Item.Properties()))
    Registry.register(BuiltInRegistries.ITEM, bays.itemKey, new BlockItem(bays.block, new Item.Properties()))
    Registry.register(BuiltInRegistries.ITEM, cellSupportItemKey, new BlockItem(cellSupportBlock, new Item.Properties()))
  }

  /** Items to be placed into the Building Blocks creative tab. */
  def creativeTabItems: Seq[ItemLike] = Seq(conway.block, bays.block, cellSupportBlock)

  /** Wire shared event listeners. Call after Platform.register + all register* calls. */
  def init(): Unit = {
    Platform.instance.onServerLevelStartTick(GameCycle.startWorldTick)
    Platform.instance.onServerLevelEndTick(GameCycle.endWorldTick)
    Platform.instance.addToBuildingBlocksTab(creativeTabItems)
  }
}
