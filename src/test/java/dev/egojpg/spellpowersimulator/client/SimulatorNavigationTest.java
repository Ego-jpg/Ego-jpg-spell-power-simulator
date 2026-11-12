package dev.egojpg.spellpowersimulator.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.InputType;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import net.neoforged.neoforge.client.ClientHooks;
import dev.egojpg.spellpowersimulator.scroll.ScrollSelection;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(EphemeralTestServerProvider.class)
class SimulatorNavigationTest {
    @Test void preservesUnderlyingCraftingResultAndReturnsToSameScreen(MinecraftServer server) {
        var output = new ResultContainer();
        output.setItem(0, new ItemStack(Items.CRAFTING_TABLE));
        var previous = new Screen(Component.literal("inventory")) {
            // The relevant InventoryMenu.removed side effect uses this real container.
            @Override public void removed() { output.clearContent(); }
        };
        var overlay = new SimulatorScreen(new ScrollSelection(SpellRegistry.MAGIC_ARROW_SPELL.get(), 1));
        var client = mock(Minecraft.class);
        var window = mock(Window.class);
        when(window.getGuiScaledWidth()).thenReturn(640);
        when(window.getGuiScaledHeight()).thenReturn(480);
        when(client.getWindow()).thenReturn(window);
        when(client.getNarrator()).thenReturn(mock(GameNarrator.class));
        when(client.getLastInputType()).thenReturn(InputType.MOUSE);
        // Preserve the relevant setScreen boundary effect without starting a
        // native window. The layer operations below execute NeoForge's real code.
        doAnswer(invocation -> {
            if (client.screen != null) client.screen.removed();
            client.screen = invocation.getArgument(0);
            return null;
        }).when(client).setScreen(any());
        doCallRealMethod().when(client).pushGuiLayer(any());
        doCallRealMethod().when(client).popGuiLayer();
        client.screen = previous;
        previous.init(client, 640, 480);
        try {
            SimulatorNavigation.open(client, overlay);
            assertSame(overlay, client.screen);
            assertTrue(output.getItem(0).is(Items.CRAFTING_TABLE));
            var fields = overlay.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
            fields.get(0).setValue("1.75");
            fields.get(1).setValue("2.5");
            overlay.resize(client, 320, 240);
            ClientHooks.resizeGuiLayers(client, 320, 240);
            var resized = overlay.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
            assertEquals("1.75", resized.get(0).getValue());
            assertEquals("2.5", resized.get(1).getValue());
            assertTrue(output.getItem(0).is(Items.CRAFTING_TABLE));
            overlay.onClose();
            assertSame(previous, client.screen);
            assertTrue(output.getItem(0).is(Items.CRAFTING_TABLE));
        } finally {
            // Clean the actual global NeoForge layer stack if an assertion failed.
            if (client.screen == overlay) client.popGuiLayer();
        }
    }
}
