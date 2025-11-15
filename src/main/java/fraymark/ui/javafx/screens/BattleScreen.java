package fraymark.ui.javafx.screens;

import fraymark.combat.engine.*;
import fraymark.combat.events.*;
import fraymark.model.actions.*;
import fraymark.model.actions.weaves.TrpScalingProfile;
import fraymark.model.actions.weaves.Weave;
import fraymark.model.combatants.*;
import fraymark.model.position.DistanceTier;
import fraymark.ui.javafx.viewmodel.*;
import fraymark.ui.javafx.screens.components.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Random;// imports
import javafx.scene.Node;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.Bindings;

// fields


public class BattleScreen extends BorderPane implements EventListener {
    private static final String NEUTRAL_STYLE =
            "-fx-padding: 6; -fx-background-color: transparent; -fx-background-radius: 6;";
    private static final String HOVER_STYLE =
            "-fx-padding: 6; -fx-background-color: #eee; -fx-background-radius: 6;";
    private static final String SELECTED_STYLE =
            "-fx-padding: 6; -fx-background-color: #ccc; -fx-border-color: #88f; -fx-border-width: 2; -fx-background-radius: 6; -fx-border-radius: 6;";
    private static final String STYLE_AOE =
            "-fx-padding: 6; -fx-background-color: #fff; -fx-border-color: #99d; -fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;";
    private final Map<Combatant, VBox> rowByCombatant = new HashMap<>();
    private final Map<Combatant, Circle> dotByCombatant = new HashMap<>();

    private final BattleState state;
    private final BattleEngine engine;
    private final EventBus bus;
    private final Random random = new Random();
    private boolean awaitingTarget = false;
    private Combatant pendingActor = null;
    private Action pendingAction = null;
    private Combatant selectedTarget = null;

    // map from UI node -> Combatant
    private final Map<Node, Combatant> nodeToCombatant = new HashMap<>();
    private final VBox actionBox = new VBox(10);
    private final TextArea log = new TextArea();
    private final Label turnIndicator = new Label();

    public BattleScreen(BattleState state, BattleEngine engine, EventBus bus) {
        this.state = state;
        this.engine = engine;
        this.bus = bus;
        bus.subscribe(this);

        setPadding(new Insets(10));

        // Build UI regions
        setTop(turnIndicator);
        setCenter(buildCombatUI());
        setRight(buildLogPanel());
        setBottom(actionBox);

        // Start the first turn
        processTurn();
    }

    /** Build the middle battlefield view (party + enemies). */
    private VBox buildCombatUI() {
        VBox root = new VBox(15);
        root.getChildren().add(new Text("Battlefield"));

        // Party sections
        root.getChildren().add(new Text("=== YOUR PARTY ==="));
        root.getChildren().add(sectionForSide(state.getParty(), true));

        root.getChildren().add(new Separator());

        // Enemy sections
        root.getChildren().add(new Text("=== ENEMIES ==="));
        root.getChildren().add(sectionForSide(state.getEnemies(), false));

        return root;
    }

    private VBox sectionForSide(List<Combatant> side, boolean isParty) {
        VBox wrapper = new VBox(10);

        // Close Range
        wrapper.getChildren().add(new Text("— Close Range —"));
        VBox closeBox = new VBox(8);
        wrapper.getChildren().add(closeBox);

        // Behind
        wrapper.getChildren().add(new Text("— Behind —"));
        VBox behindBox = new VBox(8);
        wrapper.getChildren().add(behindBox);

        for (Combatant c : side) {
            DistanceTier tier = (isParty ? state.getPartyLineup() : state.getEnemyLineup()).tierOf(c);
            VBox row = buildCombatantRow(c, isParty);
            if (tier == DistanceTier.CLOSE) closeBox.getChildren().add(row);
            else behindBox.getChildren().add(row);
        }
        return wrapper;
    }

    private VBox buildCombatantRow(Combatant c, boolean isParty) {
        CombatantViewModel vm = new CombatantViewModel(c);
        HPBar hp = new HPBar(vm);
        MGBar mg = new MGBar(vm);

        Text info = new Text();
        info.textProperty().bind(
                Bindings.format("%s (HP: %d/%d, TRP: %d, MG: %d%%, BARRIER: %.2f)",
                        vm.nameProperty(),
                        vm.hpProperty().asObject(),
                        vm.maxHpProperty().asObject(),
                        vm.trpProperty().asObject(),
                        vm.mgProperty().asObject(),
                        vm.barrierProperty().asObject()
                )
        );

        Circle dot = new Circle(8);
        dot.setFill(isParty ? Color.STEELBLUE : Color.CRIMSON);
        dot.setStroke(Color.BLACK);
        dot.setStrokeWidth(1.0);

        Tooltip.install(dot, new Tooltip(
                (isParty ? "Ally" : "Enemy") + " • " +
                        (isParty ? state.getPartyLineup().tierOf(c) : state.getEnemyLineup().tierOf(c))
        ));

        HBox header = new HBox(8, dot, info);
        VBox stats = new VBox(3, hp, mg);
        VBox row = new VBox(5, header, stats);
        row.setStyle(NEUTRAL_STYLE);

        // store for later highlight/clear
        rowByCombatant.put(c, row);
        dotByCombatant.put(c, dot);

        // click/hover handlers only active in target-select mode
        row.setOnMouseEntered(e -> {
            if (!awaitingTarget) return;
            previewAoETier(c);
        });
        row.setOnMouseExited(e -> {
            if (!awaitingTarget) return;
            if (selectedTarget != null) {
                applyLockedSelection();  // keep selection visible
            } else {
                clearAoEPreview();       // no selection yet → clear preview
            }
        });
        row.setOnMouseClicked(e -> {
            if (!awaitingTarget) return;
            // emulate your previous onTargetClicked, but with combatant directly
            onTargetChosen(c);
        });

        return row;
    }

    private void previewAoETier(Combatant hovered) {
        clearAoEPreview();
        if (pendingAction == null) return;

        List<Combatant> willHit = TargetingResolver.resolveTargets(state, pendingAction, hovered);
        for (Combatant t : willHit) {
            VBox row = rowByCombatant.get(t);
            if (row != null) row.setStyle(STYLE_AOE);
            Circle d = dotByCombatant.get(t);
            if (d != null) { d.setStroke(Color.DODGERBLUE); d.setStrokeWidth(2.0); }
        }
        // primary hovered target emphasized
        VBox main = rowByCombatant.get(hovered);
        if (main != null) main.setStyle(SELECTED_STYLE);
    }

    private void clearAoEPreview() {
        for (var row : rowByCombatant.values()) row.setStyle(NEUTRAL_STYLE);
        for (var dot : dotByCombatant.values()) { dot.setStroke(Color.BLACK); dot.setStrokeWidth(1.0); }
    }




    /** Enter target-selection mode for the chosen action. */
    private void enterTargetSelection(Combatant actor, Action action) {
        awaitingTarget = true;
        pendingActor = actor;
        pendingAction = action;
        selectedTarget = null;

        actionBox.getChildren().clear();
        Label prompt = new Label("Select the target of this action:");
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> exitTargetSelection());

        actionBox.getChildren().addAll(prompt, cancel);
    }

    /** Exit target-selection mode and restore normal action buttons. */
    private void exitTargetSelection() {
        awaitingTarget = false;
        pendingActor = null;
        pendingAction = null;
        selectedTarget = null;
        clearEnemyHighlights();
        refreshActionUI();
    }

    private void applyLockedSelection() {
        clearAoEPreview(); // reset everything first
        if (selectedTarget == null || pendingAction == null) return;

        // Re-highlight the selected target + its AoE group
        List<Combatant> willHit = TargetingResolver.resolveTargets(state, pendingAction, selectedTarget);
        for (Combatant t : willHit) {
            VBox row = rowByCombatant.get(t);
            if (row != null) row.setStyle(STYLE_AOE);
            Circle d = dotByCombatant.get(t);
            if (d != null) { d.setStroke(Color.DODGERBLUE); d.setStrokeWidth(2.0); }
        }
        VBox main = rowByCombatant.get(selectedTarget);
        if (main != null) main.setStyle(SELECTED_STYLE);
    }


    /** Called when an enemy row is clicked while in selection mode. */
    private void onTargetChosen(Combatant target) {
        selectedTarget = target;
        applyLockedSelection();
        clearAoEPreview();

        previewAoETier(target); // keep the visual selection while confirming

        Button confirm = new Button("Confirm: " + pendingAction.getName() + " → " + target.getName());
        confirm.setOnAction(e -> {
            if (pendingActor == null || pendingAction == null || selectedTarget == null) return;

            List<Combatant> finalTargets = TargetingResolver.resolveTargets(state, pendingAction, selectedTarget);
            engine.performAction(state, pendingActor, pendingAction, finalTargets);

            awaitingTarget = false;
            pendingActor = null;
            pendingAction = null;
            selectedTarget = null;
            clearAoEPreview();

            processTurn();
        });

        // show confirm button (keep your existing actionBox placement)
        if (actionBox.getChildren().size() == 2) actionBox.getChildren().add(confirm);
        else if (actionBox.getChildren().size() > 2) actionBox.getChildren().set(2, confirm);
        else actionBox.getChildren().add(confirm);
    }


    private void clearEnemyHighlights() {
        for (Node n : nodeToCombatant.keySet()) {
            n.setStyle(NEUTRAL_STYLE);
        }
    }

    private void highlightOnly(Node selected) {
        for (Node n : nodeToCombatant.keySet()) {
            if (n == selected) {
                n.setStyle(SELECTED_STYLE);
            } else {
                n.setStyle(NEUTRAL_STYLE);
            }
        }
    }

    /** Right side: combat log panel. */
    private VBox buildLogPanel() {
        log.setEditable(false);
        log.setPrefWidth(250);
        log.setWrapText(true);
        VBox panel = new VBox(new Label("Combat Log"), log);
        panel.setPadding(new Insets(5));
        return panel;
    }

    /** Process the current turn (player or enemy). */
    private void processTurn() {
        if (state.isBattleOver()) {
            showBattleResult();
            return;
        }

        Combatant actor = state.getCurrentActor();
        if (actor == null) {
            log.appendText("⚠ No active combatant.\n");
            return;
        }

        // Update turn indicator
        updateTurnIndicator(actor);

        if (actor.isPlayerControlled()) {
            // Player turn - show action buttons
            refreshActionUI();
        } else {
            // Enemy turn - use AI
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500); // Brief delay for readability
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                performEnemyAction(actor);
            });
        }
    }

    /** Update the turn indicator label. */
    private void updateTurnIndicator(Combatant actor) {
        String phaseText = actor.isPlayerControlled() ? "PLAYER TURN" : "ENEMY TURN";
        turnIndicator.setText(String.format("Round %d | %s | Current: %s (HP: %d/%d)",
                state.getRoundNumber(),
                phaseText,
                actor.getName(),
                actor.getResources().getHp(),
                actor.getStats().getMaxHP()
        ));
    }

    /** Show action buttons for the current player character. */
    private void refreshActionUI() {
        actionBox.getChildren().clear();

        Combatant actor = state.getCurrentActor();
        if (actor == null || !actor.isPlayerControlled()) {
            return;
        }

        List<Action> actions = actor.getActions();
        if (actions.isEmpty()) {
            actionBox.getChildren().add(new Label(actor.getName() + " has no actions."));
            return;
        }

        actionBox.getChildren().add(new Label(actor.getName() + "'s turn:"));


        for (Action a : actions) {
            Button btn = new Button(String.format("%s (TRP: %d)", a.getName(), a.getTrpCost()));
            btn.setOnAction(e -> {
                // If this is a VARIABLE TRP weave, ask how much to spend
                if (a instanceof Weave weave &&
                        weave.getTrpSpendMode() == fraymark.model.actions.weaves.TrpSpendMode.VARIABLE) {

                    int chosenExtra = askExtraTrp(actor, weave);
                    if (chosenExtra < 0) {
                        // User cancelled: don't do anything
                        return;
                    }

                    int base = Math.max(0, weave.getTrpBaseCost());
                    int totalSpend = base + chosenExtra;

                    // Tell the engine what to spend for THIS cast
                    engine.setTrpOverride(actor, weave, totalSpend);
                }
                performPlayerAction(actor, a);
            });

            // Disable if can't afford
            if (!a.canUse(actor)) {
                btn.setDisable(true);
            }

            actionBox.getChildren().add(btn);
        }
    }

    /** Execute a player's chosen action (now via target selection). */
    private void performPlayerAction(Combatant actor, Action action) {
        // Only player turn can initiate selection
        if (state.getPhase() != TurnPhase.PLAYER_TURN) {
            return;
        }

        // Filter enemies that are still alive
        List<Combatant> aliveEnemies = state.getEnemies().stream()
                .filter(e -> e.getResources().getHp() > 0)
                .toList();

        if (aliveEnemies.isEmpty()) {
            log.appendText("No valid targets.\n");
            processTurn();
            return;
        }

        // Enter selection mode; the actual performAction call happens after click-confirm
        enterTargetSelection(actor, action);
    }

    /** Execute an enemy's action using simple AI. */
    private void performEnemyAction(Combatant enemy) {
        List<Combatant> aliveParty = state.getParty().stream()
                .filter(p -> p.getResources().getHp() > 0)
                .toList();

        if (aliveParty.isEmpty()) {
            return;
        }

        // Simple AI: pick a random action and random target
        List<Action> actions = enemy.getActions();
        if (actions.isEmpty()) {
            log.appendText(enemy.getName() + " has no actions!\n");
            state.nextTurn();
            processTurn();
            return;
        }

        Action chosenAction = actions.get(random.nextInt(actions.size()));
        Combatant target = aliveParty.get(random.nextInt(aliveParty.size()));

        List<Combatant> finalTargets =
                TargetingResolver.resolveTargets(state, chosenAction, target);

        engine.performAction(state, enemy, chosenAction, finalTargets);


        // Process next turn
        processTurn();
    }

    /** Show the battle result. */
    private void showBattleResult() {
        actionBox.getChildren().clear();

        BattleState.BattleResult result = state.getResult();
        String resultText = switch (result) {
            case VICTORY -> "VICTORY! \nAll enemies have been defeated!";
            case DEFEAT -> " DEFEAT \nYour party was defeated!";
            case ONGOING -> "Battle continues...";
        };

        Label resultLabel = new Label(resultText);
        resultLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        actionBox.getChildren().add(resultLabel);
    }

    @Override
    public void onEvent(CombatEvent event) {
        log.appendText(event.getMessage() + "\n");
    }

    private int askExtraTrp(Combatant actor, fraymark.model.actions.weaves.Weave weave) {
        int available = actor.getResources().getTrp();
        int base = Math.max(0, weave.getTrpBaseCost());
        int maxByTrp = Math.max(0, available - base); // physical limit

        TrpScalingProfile prof = weave.getTrpScalingProfile();

        int profileMin = 0;
        int profileMax = maxByTrp;

        if (prof != null) {
            profileMin = Math.max(0, prof.minExtra());
            if (prof.maxExtra() > 0 && prof.maxExtra() != Integer.MAX_VALUE) {
                profileMax = Math.min(maxByTrp, prof.maxExtra());
            } else {
                profileMax = maxByTrp;
            }
        }

        int effMax = Math.min(maxByTrp, profileMax);
        if (effMax <= 0) {
            // Can't afford any extra; just use base TRP
            return 0;
        }

        int effMin = Math.min(profileMin, effMax);

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("TRP Spend");
        dialog.setHeaderText(weave.getName() + " — variable TRP spend");

        ButtonType okType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        Slider slider = new Slider(effMin, effMax, effMin);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        Label info = new Label();

        slider.valueProperty().addListener((obs, oldV, newV) -> {
            int extra = newV.intValue();
            StringBuilder sb = new StringBuilder(
                    "Extra TRP: " + extra +
                            " (total " + (base + extra) + "/" + available + ")"
            );

            if (prof != null) {
                int extraForMul = extra;
                double mul = 1.0 + prof.perPointMul() * extraForMul;
                if (prof.capMul() > 0.0) mul = Math.min(mul, prof.capMul());
                sb.append(String.format(" • Damage × %.2f", mul));
                sb.append(String.format(" • Allowed extra [%d..%d]", effMin, effMax));
            }

            info.setText(sb.toString());
        });

        // Trigger initial text
        slider.setValue(effMin);

        VBox content = new VBox(10,
                new Label("Choose how much extra TRP to invest:"),
                slider,
                info
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == okType) {
                return (int) Math.round(slider.getValue());
            }
            return null;
        });

        Integer result = dialog.showAndWait().orElse(null);
        if (result == null) {
            return -1; // cancel
        }
        return result;
    }

}