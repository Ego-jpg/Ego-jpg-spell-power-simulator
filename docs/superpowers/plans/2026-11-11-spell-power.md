# Spell Power Simulator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Inspect JEI scrolls and simulate audited spell damage without changing game state.
**Architecture:** JEI runtime adapter → immutable scroll selection → attribute snapshot → explicit formula registry → Minecraft Screen.
**Tech Stack:** Java 21, Gradle Wrapper, NeoForge 21.1.250, Minecraft 1.21.1, Iron's 3.16.3, JEI 19.21.0.247, JUnit 5.
**Spec:** ../specs/2026-11-11-spell-power-design.md

## Global Constraints

- Exact dependency JARs and source must be inspected before implementation.
- Client only; no inventory/attribute mutations, casts, server packets or possession requirement.
- Unsupported spells return a reason rather than estimated damage.
- Build and tests must run; in-game checks must be reported separately.
- Use existing isolated cloud checkout; no additional worktree.

## Review Focus

- JEI runtime disappears or cursor is outside ingredient: no stale selection.
- Empty/malformed/non-scroll components: no simulation or stack mutation.
- Zero baseline and invalid/nonfinite simulated input: no misleading percentage.
- Addon replaces a registered spell class: refuse unaudited formula.
- Resizing and editing GUI: keep valid input and return to prior screen.

### Task 1: Build and immutable scroll reading

**Files:** build.gradle, settings.gradle, gradle.properties, gradlew*, gradle/wrapper/*, src/main/java/dev/egojpg/spellpowersimulator/scroll/ScrollReader.java, src/test/java/dev/egojpg/spellpowersimulator/scroll/ScrollReaderTest.java.
**Interfaces:** ScrollReader.read(ItemStack) returns Optional<ScrollSelection> containing AbstractSpell and level. JEI adapter supplies the stack, never an inventory lookup.
- [ ] Inspect JAR APIs; establish official wrapper and exact dependencies.
- [ ] Write reader tests for actual scroll components, empty/non-scroll/invalid levels and unchanged stack.
- [ ] Run tests before reader exists; expected missing implementation. Implement read-only reader; rerun tests to PASS.
- [ ] Compile JEI adapter using runtime overlays and recipes API; commit working reader increment.

### Task 2: Audited calculator

**Files:** calculation/PowerInputs.java, calculation/DamageComparison.java, calculation/SpellCalculator.java; matching tests under src/test/java.
**Interfaces:** PowerInputs validates general/school values; DamageComparison.of(double,double) computes delta and optional percent; SpellCalculator accepts ScrollSelection, player and simulated values and returns supported comparison or reason.
- [ ] Write independent numeric fixtures for Firebolt level 1/10, school/general changes, config multiplier, zero baseline and nonfinite input; run RED.
- [ ] Implement pure comparison and audited adapter, preserving upstream float operations; reject unaudited IDs/classes. Read current attributes and synced config without mutation.
- [ ] Run all tests to PASS; commit calculator increment.

### Task 3: Client integration, GUI and delivery

**Files:** SpellPowerSimulator.java, client/ClientEvents.java, client/SimulatorScreen.java, jei/SimulatorJeiPlugin.java, resources/META-INF/neoforge.mods.toml, resources/assets/spell_power_simulator/lang/*.json, .github/workflows/build.yml, README.md.
**Interfaces:** plugin exposes Optional<ItemStack> at current cursor; registered configurable key opens SimulatorScreen with immutable selection and prior Screen.
- [ ] Add screen input validation tests before implementing parser; verify invalid, partial and finite numeric cases.
- [ ] Implement configurable key, JEI lifecycle handling and screen; show current/simulated powers, damage/deltas, pre-mitigation scope and unsupported reasons. Preserve edits through resize.
- [ ] Add Java 21 CI test/build/artifact workflow and audit/testing documentation.
- [ ] Execute full test/build; inspect artifact client metadata and fresh test reports.
- [ ] Fresh whole-branch review; address consequential findings and rerun affected tests.
- [ ] Save tested reusable environment instructions; commit, push feature branch and open PR for main.

## Execution record

Native execution follows the user's explicit “Aprovar e implementar”. The existing onboarding checkout is isolated. Track evidence and decisions in /workspace/spell-power-progress.md.
