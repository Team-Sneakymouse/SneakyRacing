package com.sneakyracing

import com.sneakyracing.commands.ReloadCommand
import com.sneakyracing.listener.PlaceListener
import com.sneakyracing.listener.RingEntityListener
import com.sneakyracing.listener.VisibilityListener
import com.sneakyracing.service.CrossingService
import com.sneakyracing.service.RingConfig
import com.sneakyracing.service.RingTracker
import com.sneakyracing.service.VisibilityService
import com.sneakyracing.util.Items
import com.sneakyracing.util.Keys
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

open class SneakyRacing : JavaPlugin() {

    companion object {
        const val IDENTIFIER = "sneakyracing"
        lateinit var instance: SneakyRacing

        fun log(message: String) {
            instance.logger.info(message)
        }
    }

    lateinit var keys: Keys
        private set
    lateinit var items: Items
        private set
    lateinit var ringTracker: RingTracker
        private set
    lateinit var visibilityService: VisibilityService
        private set
    lateinit var crossingService: CrossingService
        private set

    @Volatile
    var ringConfig: RingConfig =
        RingConfig(com.sneakyracing.service.Corrections(1.0, 0.75, 0.0), emptyMap())
        private set

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        keys = Keys(this)
        items = Items(keys)
        ringTracker = RingTracker(this, keys, items) { ringConfig }
        visibilityService = VisibilityService(this, items, ringTracker)
        crossingService = CrossingService(this, ringTracker, visibilityService) { ringConfig }

        saveDefaultConfig()
        reloadPlugin()

        server.pluginManager.registerEvents(
            PlaceListener(items, ringTracker, visibilityService) { ringConfig },
            this,
        )
        server.pluginManager.registerEvents(
            VisibilityListener(this, visibilityService, crossingService),
            this,
        )
        server.pluginManager.registerEvents(
            RingEntityListener(ringTracker, visibilityService),
            this,
        )

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                ReloadCommand(this).create(),
                "SneakyRacing commands (reload, give)",
            )
        }

        crossingService.start()
        logger.info("SneakyRacing enabled.")
    }

    /** Shared configuration entry point for startup and reload. */
    fun reloadPlugin() {
        reloadConfig()
        ringConfig = RingConfig.from(config)
        ringTracker.scanAllWorlds()
        val removed = ringTracker.removeTransient(visibilityService)
        if (removed > 0) {
            logger.info("Removed $removed temporary racing ring(s).")
        }
        ringTracker.refreshVisuals()
        visibilityService.syncAllOnline()
    }

    override fun onDisable() {
        crossingService.stop()
        val removed = ringTracker.removeTransient(visibilityService)
        if (removed > 0) {
            logger.info("Removed $removed temporary racing ring(s) on disable.")
        }
        visibilityService.shutdown()
        ringTracker.clear()
        logger.info("SneakyRacing disabled.")
    }
}
