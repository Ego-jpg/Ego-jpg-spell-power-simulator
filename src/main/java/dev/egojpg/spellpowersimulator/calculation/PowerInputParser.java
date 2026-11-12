package dev.egojpg.spellpowersimulator.calculation;

import java.util.OptionalDouble;

public final class PowerInputParser {
    private PowerInputParser() {}

    public static OptionalDouble parse(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return Double.isFinite(value) && value >= 0 ? OptionalDouble.of(value) : OptionalDouble.empty();
        } catch (NumberFormatException exception) {
            return OptionalDouble.empty();
        }
    }
}
