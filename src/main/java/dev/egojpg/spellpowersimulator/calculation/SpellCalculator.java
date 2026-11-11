package dev.egojpg.spellpowersimulator.calculation;

import dev.egojpg.spellpowersimulator.scroll.ScrollSelection;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.spells.fire.FireboltSpell;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

/** Captured effective powers and configuration; never modifies a living entity. */
public record SpellCalculator(int level, PowerInputs currentPowers, float configMultiplier, float currentDamage) {
    public static boolean supports(ScrollSelection selection) {
        return selection != null && selection.spell().getClass() == FireboltSpell.class
                && selection.spell().getSpellId().equals("irons_spellbooks:firebolt")
                && selection.level() >= 1 && selection.level() <= 10;
    }

    public static SpellCalculator capture(ScrollSelection selection, LivingEntity player) {
        if (!supports(selection)) throw new IllegalArgumentException("Unaudited spell, class or level");
        var version = ModList.get().getModContainerById("irons_spellbooks").orElseThrow()
                .getModInfo().getVersion().toString();
        if (!version.equals("1.21.1-3.16.3")) throw new IllegalArgumentException("Unaudited Iron's version");
        if (player == null || !player.getAttributes().hasAttribute(AttributeRegistry.SPELL_POWER)
                || SpellConfigManager.ALL_TYPES.isEmpty()) {
            throw new IllegalArgumentException("Player attributes or spell configuration unavailable");
        }
        var spell = selection.spell();
        var current = new PowerInputs(player.getAttributeValue(AttributeRegistry.SPELL_POWER),
                spell.getSchoolType().getPowerFor(player));
        float config = SpellConfigManager.getSpellConfigValue(spell, SpellConfigParameter.POWER_MULTIPLIER).floatValue();
        float damage = spell.getSpellPower(selection.level(), player) * .5f;
        // Detect changed upstream base scaling (e.g. a runtime patch) before
        // presenting a simulation made from the audited constants.
        if (Float.compare(damage, FireboltFormula.damage(selection.level(), current, config)) != 0) {
            throw new IllegalArgumentException("Live spell scaling differs from audited formula");
        }
        return new SpellCalculator(selection.level(), current, config, damage);
    }

    public DamageComparison compare(PowerInputs simulated) {
        return DamageComparison.of(currentDamage, FireboltFormula.damage(level, simulated, configMultiplier));
    }
}
