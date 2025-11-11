package fraymark.ui.javafx.screens;

import fraymark.combat.engine.*;
import fraymark.combat.events.*;
import fraymark.model.actions.*;
import fraymark.model.combatants.*;
import fraymark.ui.javafx.viewmodel.*;
import fraymark.ui.javafx.screens.components.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
        VBox box = new VBox(15);
        Text title = new Text("Battlefield");
        box.getChildren().add(title);

        // === Party ===
        Text partyLabel = new Text("=== YOUR PARTY ===");
        box.getChildren().add(partyLabel);

        for (Combatant c : state.getParty()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);
            MGBar mg = new MGBar(vm);

            Text partyInfo = new Text();
            partyInfo.textProperty().bind(
                    Bindings.format("%s (HP: %d/%d, TRP: %d)",
                            vm.nameProperty(),
                            vm.hpProperty().asObject(),
                            vm.maxHpProperty().asObject(),
                            vm.trpProperty().asObject()
                    )
            );

            VBox stats = new VBox(3, hp, mg);
            VBox partyRow = new VBox(5, partyInfo, stats);

            // style + register for selection
            partyRow.setStyle(NEUTRAL_STYLE);
            nodeToCombatant.put(partyRow, c);

            partyRow.setOnMouseClicked(e -> {
                if (!awaitingTarget) return;
                onTargetClicked(partyRow, c);
            });
            partyRow.setOnMouseEntered(e -> {
                if (!awaitingTarget) return;
                if (selectedTarget == null || nodeToCombatant.get(partyRow) != selectedTarget) {
                    partyRow.setStyle(HOVER_STYLE);
                }
            });
            partyRow.setOnMouseExited(e -> {
                if (!awaitingTarget) return;
                if (selectedTarget == null || nodeToCombatant.get(partyRow) != selectedTarget) {
                    partyRow.setStyle(NEUTRAL_STYLE);
                }
            });

            box.getChildren().add(partyRow);
        }
        box.getChildren().add(new Separator());

        // === Enemies ===
        Text enemyLabel = new Text("=== ENEMIES ===");
        box.getChildren().add(enemyLabel);

        for (Combatant c : state.getEnemies()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);

            Text enemyInfo = new Text();
            enemyInfo.textProperty().bind(
                    Bindings.format("Enemy: %s (HP: %d/%d, TRP: %d)",
                            vm.nameProperty(),
                            vm.hpProperty().asObject(),
                            vm.maxHpProperty().asObject(),
                            vm.trpProperty().asObject()
                    )
            );

            VBox enemyRow = new VBox(5, enemyInfo, hp);
            enemyRow.setStyle(NEUTRAL_STYLE);

            // Hover feedback (only if not selected and we are in selection mode)
            enemyRow.setOnMouseEntered(e -> {
                if (!awaitingTarget) return;
                if (selectedTarget == null || nodeToCombatant.get(enemyRow) != selectedTarget) {
                    enemyRow.setStyle(HOVER_STYLE);
                }
            });
            enemyRow.setOnMouseExited(e -> {
                if (!awaitingTarget) return;
                if (selectedTarget == null || nodeToCombatant.get(enemyRow) != selectedTarget) {
                    enemyRow.setStyle(NEUTRAL_STYLE);
                }
            });
            // track this row -> combatant for click selection
            nodeToCombatant.put(enemyRow, c);

            // click handler (only active when awaiting target)
            enemyRow.setOnMouseClicked(evt -> {
                if (!awaitingTarget) return;
                onTargetClicked(enemyRow, c);
            });

            box.getChildren().add(enemyRow);
        }

        return box;
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

    /** Called when an enemy row is clicked while in selection mode. */
    private void onTargetClicked(Node enemyRow, Combatant target) {
        selectedTarget = target;
        highlightOnly(enemyRow);

        // Show a confirm button
        Button confirm = new Button("Confirm: " + pendingAction.getName() + " → " + target.getName());
        confirm.setOnAction(e -> {
            // safety checks
            if (pendingActor == null || pendingAction == null || selectedTarget == null) return;

            engine.performAction(state, pendingActor, pendingAction, List.of(selectedTarget));

            // End selection mode and proceed
            awaitingTarget = false;
            pendingActor = null;
            pendingAction = null;
            selectedTarget = null;
            clearEnemyHighlights();

            processTurn();
        });

        // Replace or append the confirm button in the actionBox
        if (actionBox.getChildren().size() == 2) {
            actionBox.getChildren().add(confirm); // after prompt + cancel
        } else {
            // keep prompt and cancel at index 0,1; replace confirm at 2
            if (actionBox.getChildren().size() > 2) {
                actionBox.getChildren().set(2, confirm);
            } else {
                actionBox.getChildren().add(confirm);
            }
        }
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
            btn.setOnAction(e -> performPlayerAction(actor, a));

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

        engine.performAction(state, enemy, chosenAction, List.of(target));

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
}