package dev.egojpg.spellpowersimulator.calculation;

import java.util.OptionalDouble;

public record DamageComparison(double current, double simulated, double delta, OptionalDouble percent) {
    public static DamageComparison of(double current, double simulated) {
        if (!Double.isFinite(current) || !Double.isFinite(simulated) || current < 0 || simulated < 0) {
            throw new IllegalArgumentException("Damage must be finite and nonnegative");
        }
        double delta = simulated - current;
        double percent = delta / current * 100;
        return new DamageComparison(current, simulated, delta,
                current == 0 || !Double.isFinite(percent) ? OptionalDouble.empty() : OptionalDouble.of(percent));
    }
}
