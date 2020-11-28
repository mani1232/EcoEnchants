package com.willfp.ecoenchants.display.sorting;

import com.willfp.ecoenchants.display.EnchantmentCache;
import com.willfp.ecoenchants.enchantments.EcoEnchant;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TypeLengthSorter implements EnchantmentSorter {
    @Override
    public void sortEnchantments(final List<Enchantment> toSort) {
        List<Enchantment> sorted = new ArrayList<>();
        EcoEnchant.EnchantmentType.getValues().forEach(enchantmentType -> {
            List<Enchantment> typeEnchants = toSort.stream()
                    .filter(enchantment -> EnchantmentCache.getEntry(enchantment).getType().equals(enchantmentType))
                    .sorted(Comparator.comparingInt(enchantment -> EnchantmentCache.getEntry(enchantment).getRawName().length()))
                    .collect(Collectors.toList());
            sorted.addAll(typeEnchants);
        });

        toSort.clear();
        toSort.addAll(sorted);
    }
}
