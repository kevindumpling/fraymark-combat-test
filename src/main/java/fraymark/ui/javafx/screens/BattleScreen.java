package fraymark.ui.javafx.screens;

import fraymark.combat.engine.*;
import fraymark.combat.events.*;
import fraymark.model.actions.*;
import fraymark.model.combatants.*;
import fraymark.ui.javafx.viewmodel.*;
import fraymark.ui.javafx.screens.components.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class BattleScreen extends BorderPane implements EventListener {
    private final BattleState state;
    private final BattleEngine engine;
    private final EventBus bus;

    private final VBox actionBox = new VBox(10);
    private final TextArea log = new TextArea();

    public BattleScreen(BattleState state, BattleEngine engine, EventBus bus) {
        this.state = state;
        this.engine = engine;
        this.bus = bus;
        bus.subscribe(this);

        setPadding(new Insets(10));

        // Build UI regions
        setCenter(buildCombatUI());
        setRight(buildLogPanel());
        setBottom(actionBox);

        // Populate action buttons once combatants are ready
        refreshActionUI();
    }

    /** Build the middle battlefield view (party + enemies). */
    private VBox buildCombatUI() {
        VBox box = new VBox(15);
        Text title = new Text("Battlefield");
        box.getChildren().add(title);

        // === Party ===
        for (Combatant c : state.getParty()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);
            MGBar mg = new MGBar(vm);
            VBox stats = new VBox(3, hp, mg);
            VBox row = new VBox(5, new Text(vm.getName()), stats);
            box.getChildren().add(row);
        }

        box.getChildren().add(new Separator());

        // === Enemies ===
        for (Combatant c : state.getEnemies()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);
            box.getChildren().add(new VBox(5, new Text("Enemy: " + vm.getName()), hp));
        }

        return box;
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

    /** Bottom: builds action buttons for the current actor. */
    private void refreshActionUI() {
        actionBox.getChildren().clear();

        Combatant actor = state.getCurrentActor();
        if (actor == null) {
            actionBox.getChildren().add(new Label("No active combatant."));
            return;
        }

        List<Action> actions = actor.getActions();
        if (actions.isEmpty()) {
            actionBox.getChildren().add(new Label(actor.getName() + " has no actions."));
            return;
        }

        for (Action a : actions) {
            Button btn = new Button(a.getName());
            btn.setOnAction(e -> doAttack(a));
            actionBox.getChildren().add(btn);
        }
    }

    /** Executes the chosen action (simple default targeting). */
    private void doAttack(Action action) {
        if (state.getParty().isEmpty() || state.getEnemies().isEmpty()) {
            log.appendText("⚠ No valid targets.\n");
            return;
        }

        Combatant actor = state.getParty().get(0);  // TODO: for now its just the first person
        Combatant target = state.getEnemies().get(0);

        engine.performAction(state, actor, action, List.of(target));

        refreshActionUI(); // optional: refresh after each action
    }

    @Override
    public void onEvent(CombatEvent event) {
        log.appendText(event.getMessage() + "\n");

    }
}
