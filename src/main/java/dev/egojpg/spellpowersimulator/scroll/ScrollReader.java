package dev.egojpg.spellpowersimulator.scroll;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

public final class ScrollReader {
    private ScrollReader() {}

    public static Optional<ScrollSelection> read(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(ItemRegistry.SCROLL.get())) return Optional.empty();
        var container = ISpellContainer.get(stack);
        if (container == null || container.isEmpty() || container.getMaxSpellCount() != 1
                || container.getActiveSpellCount() != 1) return Optional.empty();
        var data = container.getSpellAtIndex(0);
        if (data == null || data.getSpell() == null || data.getLevel() < 1) return Optional.empty();
        return Optional.of(new ScrollSelection(data.getSpell(), data.getLevel()));
    }
}
