package de.lolhens.gameoflife3d

import de.lolhens.gameoflife3d.block.{CellBlock, CellBlockEntity}
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

  val CELL_BLOCK_ID = new Identifier(metadata.getId, "cell")
  val CELL_BLOCK: CellBlock = new CellBlock()
  val CELL_BLOCK_ENTITY: BlockEntityType[CellBlockEntity] = BlockEntityType.Builder.create(() => new CellBlockEntity(), CELL_BLOCK).build(null)

  def rules: GameOfLifeRules = GameOfLifeRules.ConwaysRules

  def interval: Int = 10

  override def onInitialize(): Unit = {
    Registry.register(Registry.BLOCK, CELL_BLOCK_ID, CELL_BLOCK)
    Registry.register(Registry.ITEM, CELL_BLOCK_ID, new BlockItem(CELL_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)))
    Registry.register(Registry.BLOCK_ENTITY_TYPE, CELL_BLOCK_ID, CELL_BLOCK_ENTITY)

    ServerTickEvents.START_WORLD_TICK.register { world =>
      WorldTickPhase.startWorldTick(world)
    }
  }
}
