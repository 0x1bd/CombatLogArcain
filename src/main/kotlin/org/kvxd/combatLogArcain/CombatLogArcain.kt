package org.kvxd.combatLogArcain

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CombatLogArcain : JavaPlugin() {

    private val combatTagMap = ConcurrentHashMap<UUID, Long>()
    private val miniMessage = MiniMessage.miniMessage()
    val combatDuration: Long
        get() = config.getLong("combat-duration", 15) * 1000

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()

        server.pluginManager.registerEvents(CombatListener(this), this)

        server.scheduler.runTaskTimer(this, Runnable {
            val currentTime = System.currentTimeMillis()
            combatTagMap.forEach { (uuid, expireTime) ->
                val player = Bukkit.getPlayer(uuid) ?: return@forEach
                val remaining = (expireTime - currentTime).coerceAtLeast(0) / 1000

                if (remaining > 0) {
                    showActionBar(player, remaining)
                } else {
                    player.sendActionBar(Component.text("No longer in combat", NamedTextColor.GREEN))
                    combatTagMap.remove(uuid)
                }
            }
        }, 0L, 5L) // Update every 5 ticks
    }

    fun tagPlayer(player: Player) {
        if (player.hasPermission("combatlog.bypass")) return
        combatTagMap[player.uniqueId] = System.currentTimeMillis() + combatDuration
        showActionBar(player, getRemainingCombatTime(player))
    }

    fun isInCombat(player: Player): Boolean {
        return getRemainingCombatTime(player) > 0
    }

    fun getRemainingCombatTime(player: Player): Long {
        val expireTime = combatTagMap[player.uniqueId] ?: return 0
        return (expireTime - System.currentTimeMillis()).coerceAtLeast(0) / 1000
    }

    fun removeTag(player: Player) {
        combatTagMap.remove(player.uniqueId)
        player.sendActionBar(Component.text("Combat ended", NamedTextColor.GREEN))
    }

    private fun showActionBar(player: Player, seconds: Long) {
        val message = config.getString("messages.action-bar")
            ?: "<red>Combat: <yellow><time>s</yellow>"
        val formatted = message.replace("<time>", seconds.toString())
        player.sendActionBar(miniMessage.deserialize(formatted))
    }
}