package dev.egojpg.spellpowersimulator.scroll;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

/** Detached selection: no inventory slot or mutable ItemStack is retained. */
public record ScrollSelection(AbstractSpell spell, int level) {}
