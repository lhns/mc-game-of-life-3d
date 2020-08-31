package de.lolhens.minecraft.gameoflife3d

import de.lolhens.minecraft.gameoflife3d.block.{CellBlock, CellSupportBlock}
import de.lolhens.minecraft.gameoflife3d.game.{GameCycle, GameRules}
import net.minecraft.block.Block
import net.minecraft.item.{BlockItem, Item, ItemGroup}
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent.Phase
import net.minecraftforge.event.{RegistryEvent, TickEvent}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.{ModContainer, ModLoadingContext}
import org.apache.logging.log4j.LogManager

@Mod("gameoflife3d")
object GameOfLife3dMod {
  val container: ModContainer = ModLoadingContext.get().getActiveContainer
  private val logger = LogManager.getLogger

  case class RegistryHandler(registerBlocks: RegistryEvent.Register[Block] => Unit = _ => (),
                             registerTileEntities: RegistryEvent.Register[TileEntityType[_]] => Unit = _ => (),
                             registerItems: RegistryEvent.Register[Item] => Unit = _ => ())

  private def makeCellBlock(id: String, rules: GameRules): (ResourceLocation, CellBlock, TileEntityType[_], RegistryHandler) = {
    val blockId = new ResourceLocation(container.getModId, id)
    lazy val block: CellBlock = new CellBlock(rules)
    lazy val bockEntity = block.blockEntityType

    (blockId, block, bockEntity, RegistryHandler(
      registerBlocks = { event =>
        event.getRegistry.register(block.setRegistryName(blockId))
      },
      registerTileEntities = { event =>
        event.getRegistry.register(bockEntity.setRegistryName(blockId))
      },
      registerItems = { event =>
        event.getRegistry.register(new BlockItem(block, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName(blockId))
      }
    ))
  }

  val (
    cellConwayBlockId,
    cellConwayBlock,
    cellConwayBlockEntity,
    cellConwayBlockRegistryHandler
    ) = makeCellBlock("cell_conway", GameRules.ConwaysRules)

  val (
    cellBaysBlockId,
    cellBaysBlock,
    cellBaysBlockEntity,
    cellBaysBlockRegistryHandler
    ) = makeCellBlock("cell_bays", GameRules.BaysRules5766)

  val cellSupportBlockId = new ResourceLocation(container.getModId, "cell_support")
  val cellSupportBlock: CellSupportBlock = new CellSupportBlock()
  val cellSupportBlockEntity: TileEntityType[_] = cellSupportBlock.blockEntityType

  def interval: Int = 10

  FMLJavaModLoadingContext.get.getModEventBus.addListener { _: FMLCommonSetupEvent =>
    // setup
  }

  FMLJavaModLoadingContext.get.getModEventBus.addGenericListener(classOf[Block], { event: RegistryEvent.Register[Block] =>
    cellConwayBlockRegistryHandler.registerBlocks(event)
    cellBaysBlockRegistryHandler.registerBlocks(event)
    event.getRegistry.register(cellSupportBlock.setRegistryName(cellSupportBlockId))
  })

  FMLJavaModLoadingContext.get.getModEventBus.addGenericListener(classOf[TileEntityType[_]], { event: RegistryEvent.Register[TileEntityType[_]] =>
    cellConwayBlockRegistryHandler.registerTileEntities(event)
    cellBaysBlockRegistryHandler.registerTileEntities(event)
    event.getRegistry.register(cellSupportBlockEntity.setRegistryName(cellSupportBlockId))
  })

  FMLJavaModLoadingContext.get.getModEventBus.addGenericListener(classOf[Item], { event: RegistryEvent.Register[Item] =>
    cellConwayBlockRegistryHandler.registerItems(event)
    cellBaysBlockRegistryHandler.registerItems(event)
    event.getRegistry.register(new BlockItem(cellSupportBlock, new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)).setRegistryName(cellSupportBlockId))
  })

  MinecraftForge.EVENT_BUS.addListener { event: TickEvent.WorldTickEvent =>
    (event.phase, event.world) match {
      case (Phase.START, serverWorld: ServerWorld) =>
        GameCycle.startWorldTick(serverWorld)

      case (Phase.END, serverWorld: ServerWorld) =>
        GameCycle.endWorldTick(serverWorld)

      case _ =>
    }
  }
}
