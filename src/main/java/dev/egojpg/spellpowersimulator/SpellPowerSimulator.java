package dev.egojpg.spellpowersimulator;

import dev.egojpg.spellpowersimulator.client.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SpellPowerSimulator.MOD_ID, dist = Dist.CLIENT)
public final class SpellPowerSimulator {
    public static final String MOD_ID = "spell_power_simulator";

    public SpellPowerSimulator(IEventBus modBus) {
        modBus.addListener(ClientEvents::registerKeys);
        NeoForge.EVENT_BUS.addListener(ClientEvents::keyPressed);
        NeoForge.EVENT_BUS.addListener(ClientEvents::mousePressed);
    }
}
