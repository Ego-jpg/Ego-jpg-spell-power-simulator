package dev.egojpg.spellpowersimulator.calculation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PowerInputParserTest {
    @Test void acceptsFiniteNonnegativeMultipliers() {
        assertEquals(1.25, PowerInputParser.parse("1.25").orElseThrow());
        assertEquals(0, PowerInputParser.parse("0").orElseThrow());
        assertEquals(2, PowerInputParser.parse(" 2 ").orElseThrow());
    }
    @Test void incompleteOrInvalidEditsProduceNoNumber() {
        for (var text : new String[]{"", ".", "-", "-1", "NaN", "Infinity", "1e999", "abc"}) {
            assertTrue(PowerInputParser.parse(text).isEmpty(), text);
        }
    }
}
