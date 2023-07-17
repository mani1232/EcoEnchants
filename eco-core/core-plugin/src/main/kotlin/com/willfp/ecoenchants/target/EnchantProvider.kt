package com.willfp.ecoenchants.target

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.fast.fast
import com.willfp.eco.core.items.isEmpty
import com.willfp.eco.core.map.listMap
import com.willfp.ecoenchants.enchants.EcoEnchant
import com.willfp.ecoenchants.enchants.EcoEnchantLevel
import com.willfp.libreforge.ItemProvidedHolder
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.TimeUnit

/*

For libreforge-based enchantments.

 */

/**
 * The enchantment level holders active on a player.
 */
val Player.enchantmentHolders: List<ItemProvidedHolder>
    get() = this.pullEnchantmentLevels().toProvidedHolders()

/**
 * Clear the enchantment cache for this player.
 */
fun Player.clearEnchantmentCache() {
    inventoryContentsCache.invalidate(this.uniqueId)
}

// Convert a map of items to a list of enchantment level holders
private fun Map<ItemStack, List<EcoEnchantLevel>>.toProvidedHolders() =
    this.flatMap { (item, levels) ->
        levels.map {
            ItemProvidedHolder(it.enchant.getLevel(it.level), item)
        }
    }

// Cached inventory contents
private val inventoryContentsCache = Caffeine.newBuilder()
    .expireAfterWrite(500, TimeUnit.MILLISECONDS)
    .build<UUID, Map<Int, ItemStack>>()

@Suppress("UNCHECKED_CAST")
fun <K : Any, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> =
    this.filterValues { it != null } as Map<K, V>

private val Player.cachedInventoryContents: Map<Int, ItemStack>
    get() = inventoryContentsCache.get(this.uniqueId) {
        this.inventory.contents.mapIndexed { index, item ->
            index to item
        }.toMap().filterValuesNotNull()
    }

private fun Player.pullEnchantmentLevels(): Map<ItemStack, List<EcoEnchantLevel>> {
    val inSlots = pullEnchantmentLevelsAtSlots()

    val withItem = inSlots.map { (slot, levels) ->
        val item = this.cachedInventoryContents[slot]!!
        item to levels
    }.toMap()

    return withItem
}

private fun Player.pullEnchantmentLevelsAtSlots(): Map<Int, List<EcoEnchantLevel>> {
    val enchantLevels = listMap<Int, EcoEnchantLevel>()

    // Iterate through all items in inventory
    for ((slot, item) in this.cachedInventoryContents) {
        if (item.isEmpty) {
            continue
        }

        val enchantments = item.fast().enchants

        // Iterate through all enchantments on item
        for ((enchant, level) in enchantments) {
            if (enchant !is EcoEnchant) {
                continue
            }

            // Check if the enchantment should activate on this item

            // Only check for the slots, because enchantments should work
            // even on unsupported targets, for example how Sharpness still
            // works on a pickaxe.
            for (target in enchant.targets) {
                if (slot !in target.slot.getItemSlots(this)) {
                    continue
                }

                enchantLevels[slot] += enchant.getLevel(level)
            }
        }
    }

    return enchantLevels
}


/*

For hardcoded enchantments

 */

/**
 * If the player has the enchantment active.
 */
fun Player.hasEnchantActive(enchant: EcoEnchant): Boolean =
    this.enchantmentHolders.any {
        val level = it.holder as EcoEnchantLevel

        level.enchant == enchant && level.conditions.areMet(this, it)
    }

/**
 * Get the active enchantment level in a numeric slot.
 *
 * This respect conditions.
 *
 * Returns 0 if the enchantment is not active.
 */
fun Player.getActiveEnchantLevelInSlot(enchant: EcoEnchant, slot: Int): Int {
    return this.getEnchantLevelInSlot(enchant, slot)?.takeIf {
        val item = this.cachedInventoryContents[slot]!!
        val provider = ItemProvidedHolder(it, item)

        it.conditions.areMet(this, provider)
    }?.level ?: 0
}


/**
 * Get the active enchantment level in a numeric slot.
 */
fun Player.getEnchantLevelInSlot(enchant: EcoEnchant, slot: Int): EcoEnchantLevel? {
    return this.pullEnchantmentLevelsAtSlots()[slot]
        ?.firstOrNull { it.enchant == enchant }
}
