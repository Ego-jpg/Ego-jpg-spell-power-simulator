package dev.egojpg.spellpowersimulator.calculation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculationTest {
    // These fixtures catch swapped powers, lost configuration multipliers and wrong level scaling.
    @Test void fireboltUsesLevelBothAttributesAndConfig() {
        assertEquals(6f, FireboltFormula.damage(1, new PowerInputs(1, 1), 1));
        assertEquals(31.5f, FireboltFormula.damage(10, new PowerInputs(2, 1.5), 1));
        assertEquals(15.75f, FireboltFormula.damage(10, new PowerInputs(2, 1.5), .5f));
    }
    @Test void generalAndSchoolAreIndependentMultipliers() {
        assertEquals(12f, FireboltFormula.damage(1, new PowerInputs(2, 1), 1));
        assertEquals(18f, FireboltFormula.damage(1, new PowerInputs(1, 3), 1));
        assertEquals(0f, FireboltFormula.damage(1, new PowerInputs(0, 3), 1));
    }
    @Test void comparisonIncludesSignedDeltaAndPercentage() {
        var increase = DamageComparison.of(6, 9);
        assertEquals(3, increase.delta());
        assertEquals(50, increase.percent().orElseThrow());
        assertEquals(-50, DamageComparison.of(6, 3).percent().orElseThrow());
    }
    @Test void zeroBaselineHasNoPercentage() {
        assertTrue(DamageComparison.of(0, 9).percent().isEmpty());
        assertEquals(9, DamageComparison.of(0, 9).delta());
    }
    @Test void rejectsNonfiniteNegativeAndOverflowingValues() {
        assertThrows(IllegalArgumentException.class, () -> new PowerInputs(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> new PowerInputs(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new PowerInputs(Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> FireboltFormula.damage(0, new PowerInputs(1, 1), 1));
        assertThrows(IllegalArgumentException.class, () -> FireboltFormula.damage(1, new PowerInputs(Double.MAX_VALUE, 1), 1));
        assertThrows(IllegalArgumentException.class, () -> DamageComparison.of(1, Double.NaN));
    }
}
