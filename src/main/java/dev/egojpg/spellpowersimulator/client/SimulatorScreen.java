package dev.egojpg.spellpowersimulator.client;

import dev.egojpg.spellpowersimulator.calculation.*;
import dev.egojpg.spellpowersimulator.scroll.ScrollSelection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class SimulatorScreen extends Screen {
    private final Screen previous;
    private final ScrollSelection selection;
    private SpellCalculator calculator;
    private String generalText;
    private String schoolText;
    private EditBox general;
    private EditBox school;
    private int left, top, panelWidth, panelHeight;

    public SimulatorScreen(Screen previous, ScrollSelection selection) {
        super(text("title"));
        this.previous = previous;
        this.selection = selection;
    }

    @Override protected void init() {
        refresh();
        panelWidth = Math.min(440, width - 12);
        panelHeight = Math.min(248, height - 12);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        if (generalText == null) {
            generalText = calculator == null ? "1.0" : Double.toString(calculator.currentPowers().general());
            schoolText = calculator == null ? "1.0" : Double.toString(calculator.currentPowers().school());
        }
        int fieldWidth = (panelWidth - 36) / 2;
        general = new EditBox(font, left + 12, top + 84, fieldWidth, 18, text("general"));
        school = new EditBox(font, left + 24 + fieldWidth, top + 84, fieldWidth, 18, text("school"));
        general.setMaxLength(32);
        school.setMaxLength(32);
        general.setValue(generalText);
        school.setValue(schoolText);
        general.setResponder(value -> generalText = value);
        school.setResponder(value -> schoolText = value);
        addRenderableWidget(general);
        addRenderableWidget(school);
        addRenderableWidget(Button.builder(text("reset"), button -> {
            if (calculator != null) {
                general.setValue(Double.toString(calculator.currentPowers().general()));
                school.setValue(Double.toString(calculator.currentPowers().school()));
            }
        }).bounds(left + 12, top + panelHeight - 26, 120, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + panelWidth - 92, top + panelHeight - 26, 80, 20).build());
    }

    @Override public void tick() { refresh(); }

    private void refresh() {
        calculator = null;
        if (!SpellCalculator.supports(selection) || minecraft == null || minecraft.player == null) return;
        try {
            calculator = SpellCalculator.capture(selection, minecraft.player);
        } catch (IllegalArgumentException exception) {
            // No fallback estimate when the captured state fails the audit checks.
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, 0xFFAAAAAA);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFF202020);
        graphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);
        line(graphics, text("spell", selection.spell().getSpellId(), selection.level()), 25, 0xFFFFFF);
        line(graphics, selection.spell().getSchoolType().getDisplayName(), 38, 0xAAAAFF);
        if (calculator != null) {
            var current = calculator.currentPowers();
            line(graphics, text("current_powers", number(current.general()), number(current.school())), 54, 0xCCCCCC);
        }
        line(graphics, text("general"), 72, 0xFFFFFF);
        graphics.drawString(font, text("school"), left + 24 + (panelWidth - 36) / 2, top + 72, 0xFFFFFF);
        if (!SpellCalculator.supports(selection)) {
            graphics.drawWordWrap(font, text("unsupported"), left + 12, top + 113, panelWidth - 24, 0xFFAA55);
        } else if (calculator == null) {
            graphics.drawWordWrap(font, text("unavailable"), left + 12, top + 113, panelWidth - 24, 0xFFAA55);
        } else {
            renderComparison(graphics);
        }
        graphics.drawWordWrap(font, text("scope"), left + 12, top + 157, panelWidth - 24, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderComparison(GuiGraphics graphics) {
        var generalValue = PowerInputParser.parse(generalText);
        var schoolValue = PowerInputParser.parse(schoolText);
        if (generalValue.isEmpty() || schoolValue.isEmpty()) {
            line(graphics, text("invalid"), 113, 0xFF7777);
            return;
        }
        try {
            var result = calculator.compare(new PowerInputs(generalValue.getAsDouble(), schoolValue.getAsDouble()));
            line(graphics, text("damage", number(result.current()), number(result.simulated())), 113, 0xFFFFFF);
            String percentage = result.percent().isPresent() ? number(result.percent().getAsDouble()) + "%" : "—";
            line(graphics, text("delta", number(result.delta()), percentage), 128, 0x99FF99);
            line(graphics, text("config", number(calculator.configMultiplier())), 143, 0xAAAAAA);
        } catch (IllegalArgumentException exception) {
            line(graphics, text("invalid"), 113, 0xFF7777);
        }
    }

    private void line(GuiGraphics graphics, Component value, int y, int color) {
        graphics.drawString(font, font.substrByWidth(value, panelWidth - 24).getString(), left + 12, top + y, color);
    }
    private static String number(double value) { return String.format(Locale.ROOT, "%.3f", value); }
    private static Component text(String key, Object... args) {
        return Component.translatable("screen.spell_power_simulator." + key, args);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.setScreen(minecraft.player == null ? null : previous); }
}
