package dev.egojpg.spellpowersimulator.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Owns the simulator's screen transition, separately from its widgets. */
public final class SimulatorNavigation {
    private SimulatorNavigation() {}

    // setScreen removes the underlying container and can clear its crafting output.
    public static void open(Minecraft client, Screen simulator) { client.pushGuiLayer(simulator); }
    public static void close(Minecraft client) { client.popGuiLayer(); }
}
