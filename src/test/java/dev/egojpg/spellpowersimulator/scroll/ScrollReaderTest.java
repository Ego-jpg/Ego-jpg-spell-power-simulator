package dev.egojpg.spellpowersimulator.scroll;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class ScrollReaderTest {
    @Test void readsRealComponentWithoutChangingTheStack(MinecraftServer server) {
        var stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(SpellRegistry.FIREBOLT_SPELL.get(), 7, stack);
        var before = stack.copy();
        var result = ScrollReader.read(stack).orElseThrow();
        assertEquals("irons_spellbooks:firebolt", result.spell().getSpellId());
        assertEquals(7, result.level());
        assertTrue(ItemStack.matches(before, stack));
        assertSame(ISpellContainer.get(before), ISpellContainer.get(stack));
    }
    @Test void rejectsNonScrollEvenWithSpellComponent(MinecraftServer server) {
        var stack = new ItemStack(Items.STICK);
        ISpellContainer.createScrollContainer(SpellRegistry.FIREBOLT_SPELL.get(), 1, stack);
        assertTrue(ScrollReader.read(stack).isEmpty());
    }
    @Test void rejectsAbsentAndEmptyComponents(MinecraftServer server) {
        assertTrue(ScrollReader.read(null).isEmpty());
        assertTrue(ScrollReader.read(ItemStack.EMPTY).isEmpty());
        assertTrue(ScrollReader.read(new ItemStack(ItemRegistry.SCROLL.get())).isEmpty());
    }
    @Test void rejectsInvalidLevel(MinecraftServer server) {
        var stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(SpellRegistry.FIREBOLT_SPELL.get(), 0, stack);
        assertTrue(ScrollReader.read(stack).isEmpty());
    }
}
