package de.lolhens.gameoflife3d

import de.lolhens.gameoflife3d.block.{CellBlock, CellBlockEntity, CellSupportBlock, CellSupportBlockEntity}
import de.lolhens.gameoflife3d.game.{GameCycle, GameRules}
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.{BlockItem, Item, ItemGroup}
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import scala.jdk.CollectionConverters._

object GameOfLife3dMod extends ModInitializer {
  val metadata: ModMetadata = {
    FabricLoader.getInstance().getEntrypointContainers("main", classOf[ModInitializer])
      .iterator().asScala.find(_.getEntrypoint == this).get.getProvider.getMetadata
  }

  private def makeCellBlock(id: String, rules: GameRules): (Identifier, CellBlock, BlockEntityType[CellBlockEntity], () => Unit) = {
    val blockId = new Identifier(metadata.getId, id)
    lazy val block: CellBlock = new CellBlock(_ => bockEntity.instantiate(), rules)
    lazy val bockEntity: BlockEntityType[CellBlockEntity] = BlockEntityType.Builder.create(() => new CellBlockEntity(bockEntity, block), block).build(null)

    def register(): Unit = {
      Registry.register(Registry.BLOCK, blockId, block)
      Registry.register(Registry.ITEM, blockId, new BlockItem(block, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)))
      Registry.register(Registry.BLOCK_ENTITY_TYPE, blockId, bockEntity)
    }

    (blockId, block, bockEntity, register)
  }

  val (
    cellConwayBlockId,
    cellConwayBlock,
    cellConwayBlockEntity,
    cellConwayBlockRegister
    ) = makeCellBlock("cell_conway", GameRules.ConwaysRules)

  val (
    cellBaysBlockId,
    cellBaysBlock,
    cellBaysBlockEntity,
    cellBaysBlockRegister
    ) = makeCellBlock("cell_bays", GameRules.BaysRules5766)

  val cellSupportBlockId = new Identifier(metadata.getId, "cell_support")
  val cellSupportBlock: CellSupportBlock = new CellSupportBlock()
  val cellSupportBlockEntity: BlockEntityType[CellSupportBlockEntity] = BlockEntityType.Builder.create(() => new CellSupportBlockEntity(), cellSupportBlock).build(null)

  def interval: Int = 10

  override def onInitialize(): Unit = {
    cellConwayBlockRegister()
    cellBaysBlockRegister()

    Registry.register(Registry.BLOCK, cellSupportBlockId, cellSupportBlock)
    Registry.register(Registry.ITEM, cellSupportBlockId, new BlockItem(cellSupportBlock, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)))
    Registry.register(Registry.BLOCK_ENTITY_TYPE, cellSupportBlockId, cellSupportBlockEntity)

    ServerTickEvents.START_WORLD_TICK.register(GameCycle.startWorldTick)
    ServerTickEvents.END_WORLD_TICK.register(GameCycle.endWorldTick)
  }
}
