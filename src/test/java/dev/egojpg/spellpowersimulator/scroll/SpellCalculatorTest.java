package dev.egojpg.spellpowersimulator.scroll;

import dev.egojpg.spellpowersimulator.calculation.PowerInputs;
import dev.egojpg.spellpowersimulator.calculation.SpellCalculator;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.spells.fire.FireboltSpell;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.SyncJsonConfigPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(EphemeralTestServerProvider.class)
class SpellCalculatorTest {
    @Test void refusesUnauditedSpellAndReplacementClass(MinecraftServer server) {
        assertFalse(SpellCalculator.supports(new ScrollSelection(SpellRegistry.MAGIC_ARROW_SPELL.get(), 1)));
        assertFalse(SpellCalculator.supports(new ScrollSelection(new FireboltSpell() {}, 1)));
        assertFalse(SpellCalculator.supports(new ScrollSelection(SpellRegistry.FIREBOLT_SPELL.get(), 11)));
    }
    @Test void capturesRealAttributesAndAgreesWithUpstreamWithoutMutation(MinecraftServer server) {
        // The framework server loads registries/datapacks, not a playable world.
        // This isolated living entity uses the actual AttributeMap and Iron's attributes.
        SpellConfigManager.INSTANCE.handleClientSync(new SyncJsonConfigPacket(Map.of()));
        // Curios requires a level to select server/client slots during entity construction.
        // Mock only that world boundary; retain real entity attributes and spell methods.
        var player = new ArmorStand(EntityType.ARMOR_STAND, mock(Level.class));
        var spell = SpellRegistry.FIREBOLT_SPELL.get();
        var before = player.getAttributes().save();
        var calculator = SpellCalculator.capture(new ScrollSelection(spell, 7), player);
        var result = calculator.compare(new PowerInputs(2, 1.5));
        assertEquals(spell.getSpellPower(7, player) * .5f, result.current());
        assertEquals(calculator.currentDamage(), calculator.compare(calculator.currentPowers()).simulated());
        assertTrue(result.simulated() > result.current());
        assertEquals(before, player.getAttributes().save());
    }

    @Test void usesSyncedConfigAndEffectiveEquipmentStyleModifiersAtEveryAuditedLevel(MinecraftServer server) {
        var spell = SpellRegistry.FIREBOLT_SPELL.get();
        var config = "{\"power_multiplier\":0.6}".getBytes(StandardCharsets.UTF_8);
        SpellConfigManager.INSTANCE.handleClientSync(new SyncJsonConfigPacket(Map.of(spell.getSpellResource(), config)));
        try {
            var entity = new ArmorStand(EntityType.ARMOR_STAND, mock(Level.class));
            var general = entity.getAttribute(AttributeRegistry.SPELL_POWER);
            general.setBaseValue(1.2);
            general.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("spell_power_simulator", "test"),
                    .25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            entity.getAttribute(AttributeRegistry.FIRE_SPELL_POWER).setBaseValue(1.75);
            var before = entity.getAttributes().save();
            for (int level = 1; level <= 10; level++) {
                var calculator = SpellCalculator.capture(new ScrollSelection(spell, level), entity);
                assertEquals(1.5, calculator.currentPowers().general(), 1e-12);
                assertEquals(1.75, calculator.currentPowers().school());
                assertEquals(.6f, calculator.configMultiplier());
                assertEquals(spell.getSpellPower(level, entity) * .5f, calculator.currentDamage());
                assertEquals(calculator.currentDamage(), calculator.compare(calculator.currentPowers()).simulated());
            }
            assertEquals(before, entity.getAttributes().save());
        } finally {
            SpellConfigManager.INSTANCE.handleClientSync(new SyncJsonConfigPacket(Map.of()));
        }
    }
}
