package org.kvxd.combatLogArcain

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class CombatListener(private val plugin: CombatLogArcain) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        // Only handle player vs player combat
        val victim = event.entity as? Player ?: return
        val attacker = when (val damager = event.damager) {
            is Player -> damager
            is Projectile -> damager.shooter as? Player
            else -> null
        } ?: return

        // Tag both players
        plugin.tagPlayer(attacker)
        plugin.tagPlayer(victim)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (plugin.isInCombat(player)) {
            player.health = 0.0
            Bukkit.broadcast(
                Component.text("${player.name} logged out during combat!", NamedTextColor.RED)
            )
            plugin.removeTag(player)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        plugin.removeTag(event.entity)
    }
}