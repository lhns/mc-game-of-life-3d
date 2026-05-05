package de.lolhens.minecraft.gameoflife3d

import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.level.ItemLike
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.registries.RegisterEvent

// mcdp's NeoForge loader fills constructor parameters from a context bag of
// [IEventBus, ModContainer, Dist] with subset matching.
@Mod("gameoflife3d")
class GameOfLife3dNeoForge(modBus: IEventBus) {

  // Buffer the start/end-tick callbacks until we've subscribed to the event bus, since
  // GameOfLife3dMod.init() calls Platform.onServerLevel*Tick during FMLCommonSetupEvent
  // — well before the level tick fires.
  private val startTickCallbacks = scala.collection.mutable.Buffer.empty[ServerLevel => Unit]
  private val endTickCallbacks = scala.collection.mutable.Buffer.empty[ServerLevel => Unit]
  private val pendingCreativeTabItems = scala.collection.mutable.Buffer.empty[ItemLike]

  Platform.register(new Platform {
    override def onServerLevelStartTick(cb: ServerLevel => Unit): Unit =
      startTickCallbacks += cb

    override def onServerLevelEndTick(cb: ServerLevel => Unit): Unit =
      endTickCallbacks += cb

    override def addToBuildingBlocksTab(items: Seq[ItemLike]): Unit =
      pendingCreativeTabItems ++= items
  })

  NeoForge.EVENT_BUS.addListener { (ev: LevelTickEvent.Pre) =>
    ev.getLevel match {
      case sl: ServerLevel => startTickCallbacks.foreach(_(sl))
      case _ =>
    }
  }

  NeoForge.EVENT_BUS.addListener { (ev: LevelTickEvent.Post) =>
    ev.getLevel match {
      case sl: ServerLevel => endTickCallbacks.foreach(_(sl))
      case _ =>
    }
  }

  modBus.addListener { (ev: RegisterEvent) =>
    if (ev.getRegistryKey == Registries.BLOCK) {
      GameOfLife3dMod.registerBlocks()
    } else if (ev.getRegistryKey == Registries.BLOCK_ENTITY_TYPE) {
      GameOfLife3dMod.registerBlockEntities()
    } else if (ev.getRegistryKey == Registries.ITEM) {
      GameOfLife3dMod.registerItems()
    }
  }

  modBus.addListener { (ev: BuildCreativeModeTabContentsEvent) =>
    if (ev.getTabKey == CreativeModeTabs.BUILDING_BLOCKS) {
      pendingCreativeTabItems.foreach(ev.accept(_))
    }
  }

  modBus.addListener { (_: FMLCommonSetupEvent) =>
    GameOfLife3dMod.init()
  }
}
