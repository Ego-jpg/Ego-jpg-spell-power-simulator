package dev.egojpg.spellpowersimulator.calculation;

/** Effective attribute values (1.0 means the normal 100%), not additive bonuses. */
public record PowerInputs(double general, double school) {
    public PowerInputs {
        if (!Double.isFinite(general) || !Double.isFinite(school) || general < 0 || school < 0) {
            throw new IllegalArgumentException("Powers must be finite and nonnegative");
        }
    }
}
