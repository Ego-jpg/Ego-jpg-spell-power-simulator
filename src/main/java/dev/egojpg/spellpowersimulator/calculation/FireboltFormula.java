package dev.egojpg.spellpowersimulator.calculation;

/** Audited against Iron's 1.21.1-3.16.3 source and bytecode; damage per direct impact. */
public final class FireboltFormula {
    private FireboltFormula() {}

    public static float damage(int level, PowerInputs powers, float configMultiplier) {
        if (level < 1 || level > 10 || !Float.isFinite(configMultiplier) || configMultiplier < 0) {
            throw new IllegalArgumentException("Invalid Firebolt level or configuration");
        }
        // AbstractSpell casts general power to float, promotes it to double, then
        // multiplies school and the float config. Firebolt halves the rounded result.
        float spellPower = (float) ((12 + (level - 1)) * (double) (float) powers.general()
                * powers.school() * configMultiplier);
        float result = spellPower * .5f;
        if (!Float.isFinite(result)) throw new IllegalArgumentException("Damage overflow");
        return result;
    }
}
