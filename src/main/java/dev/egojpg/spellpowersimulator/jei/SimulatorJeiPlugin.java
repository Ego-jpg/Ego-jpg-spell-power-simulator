package dev.egojpg.spellpowersimulator.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

@JeiPlugin
public final class SimulatorJeiPlugin implements IModPlugin {
    private static IJeiRuntime runtime;

    @Override public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("spell_power_simulator", "jei");
    }
    @Override public void onRuntimeAvailable(IJeiRuntime available) { runtime = available; }
    @Override public void onRuntimeUnavailable() { runtime = null; }

    public static boolean hasSearchFocus() {
        return runtime != null && runtime.getIngredientListOverlay().hasKeyboardFocus();
    }

    public static Optional<ItemStack> hoveredStack() {
        var current = runtime;
        if (current == null) return Optional.empty();
        ItemStack stack = current.getIngredientListOverlay().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        if (stack == null) stack = current.getBookmarkOverlay().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
        if (stack != null) return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
        return current.getRecipesGui().getIngredientUnderMouse(VanillaTypes.ITEM_STACK).filter(s -> !s.isEmpty());
    }
}
