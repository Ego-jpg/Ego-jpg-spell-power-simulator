package dev.egojpg.spellpowersimulator.scroll;

import dev.egojpg.spellpowersimulator.jei.SimulatorJeiPlugin;
import mezz.jei.api.runtime.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class JeiSelectionTest {
    @Test void readsJeiStackWithoutInventoryAndDropsUnavailableRuntime(MinecraftServer server) {
        var stack = new ItemStack(Items.PAPER);
        var plugin = new SimulatorJeiPlugin();
        plugin.onRuntimeAvailable(runtime(stack, null, Optional.empty()));
        assertSame(stack, SimulatorJeiPlugin.hoveredStack().orElseThrow());
        plugin.onRuntimeUnavailable();
        assertTrue(SimulatorJeiPlugin.hoveredStack().isEmpty());
    }
    @Test void handlesBookmarksRecipesAndEmptyCursor(MinecraftServer server) {
        var stack = new ItemStack(Items.PAPER);
        var plugin = new SimulatorJeiPlugin();
        try {
            plugin.onRuntimeAvailable(runtime(null, stack, Optional.empty()));
            assertSame(stack, SimulatorJeiPlugin.hoveredStack().orElseThrow());
            plugin.onRuntimeAvailable(runtime(null, null, Optional.of(stack)));
            assertSame(stack, SimulatorJeiPlugin.hoveredStack().orElseThrow());
            plugin.onRuntimeAvailable(runtime(null, null, Optional.empty()));
            assertTrue(SimulatorJeiPlugin.hoveredStack().isEmpty());
        } finally { plugin.onRuntimeUnavailable(); }
    }
    private static IJeiRuntime runtime(ItemStack list, ItemStack bookmark, Optional<ItemStack> recipe) {
        return proxy(IJeiRuntime.class, Map.of(
                "getIngredientListOverlay", proxy(IIngredientListOverlay.class, Map.of("getIngredientUnderMouse", Optional.ofNullable(list))),
                "getBookmarkOverlay", proxy(IBookmarkOverlay.class, Map.of("getIngredientUnderMouse", Optional.ofNullable(bookmark))),
                "getRecipesGui", proxy(IRecipesGui.class, Map.of("getIngredientUnderMouse", recipe))));
    }
    // Test doubles only for the external, read-only JEI boundary. Overload by
    // ingredient type returns nullable T, while recipes returns Optional<T>.
    private static <T> T proxy(Class<T> type, Map<String, ?> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, method, args) -> {
            Object value = values.get(method.getName());
            if (value instanceof Optional<?> optional && !method.getReturnType().equals(Optional.class)) return optional.orElse(null);
            return value;
        }));
    }
}
