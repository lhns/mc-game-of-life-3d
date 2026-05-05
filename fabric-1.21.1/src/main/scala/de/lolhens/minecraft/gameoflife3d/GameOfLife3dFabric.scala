package de.lolhens.minecraft.gameoflife3d

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.level.ItemLike

object GameOfLife3dFabric extends ModInitializer {
  override def onInitialize(): Unit = {
    Platform.register(new Platform {
      override def onServerLevelStartTick(cb: ServerLevel => Unit): Unit =
        ServerTickEvents.START_WORLD_TICK.register(world => cb(world))

      override def onServerLevelEndTick(cb: ServerLevel => Unit): Unit =
        ServerTickEvents.END_WORLD_TICK.register(world => cb(world))

      override def addToBuildingBlocksTab(items: Seq[ItemLike]): Unit =
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register { entries =>
          items.foreach(entries.accept(_))
        }
    })

    GameOfLife3dMod.registerBlocks()
    GameOfLife3dMod.registerBlockEntities()
    GameOfLife3dMod.registerItems()
    GameOfLife3dMod.init()
  }
}
