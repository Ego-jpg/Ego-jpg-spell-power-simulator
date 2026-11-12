package dev.egojpg.spellpowersimulator.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.egojpg.spellpowersimulator.jei.SimulatorJeiPlugin;
import dev.egojpg.spellpowersimulator.scroll.ScrollReader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final KeyMapping OPEN = new KeyMapping("key.spell_power_simulator.open",
            KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "key.categories.spell_power_simulator");
    private ClientEvents() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN); }

    public static void keyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (OPEN.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))
                && open(event.getScreen())) event.setCanceled(true);
    }

    public static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (OPEN.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))
                && open(event.getScreen())) event.setCanceled(true);
    }

    private static boolean open(Screen previous) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || previous instanceof SimulatorScreen
                || previous.getFocused() instanceof EditBox || SimulatorJeiPlugin.hasSearchFocus()) return false;
        var selection = SimulatorJeiPlugin.hoveredStack().flatMap(ScrollReader::read);
        if (selection.isEmpty()) return false;
        SimulatorNavigation.open(minecraft, new SimulatorScreen(selection.get()));
        return true;
    }
}
