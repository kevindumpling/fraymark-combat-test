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

/***
 * The main view of the battle shown to the player, including the UI's elements.
 */
public class BattleScreen extends BorderPane implements EventListener {
    private final BattleState state;
    private final BattleEngine engine;
    private final EventBus bus;
    private final TextArea log = new TextArea();

    public BattleScreen(BattleState state, BattleEngine engine, EventBus bus) {
        this.state = state;
        this.engine = engine;
        this.bus = bus;
        bus.subscribe(this);

        setPadding(new Insets(10));
        setCenter(buildCombatUI());
        setBottom(buildActionUI());
    }

    private VBox buildCombatUI() {
        VBox box = new VBox(15);

        Text title = new Text("Battlefield");
        box.getChildren().add(title);

        // Party view
        for (Combatant c : state.getParty()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);
            MGBar mg = new MGBar(vm);

            VBox stats = new VBox(3, hp, mg);
            VBox row = new VBox(5, new Text(vm.getName()), stats);
            box.getChildren().add(row);
        }

        box.getChildren().add(new Separator());

        // Enemy list
        for (Combatant c : state.getEnemies()) {
            CombatantViewModel vm = new CombatantViewModel(c);
            HPBar hp = new HPBar(vm);
            box.getChildren().add(new VBox(5, new Text("Enemy: " + vm.getName()), hp));
        }

        return box;
    }

    private VBox buildActionUI() {
        VBox container = new VBox(8);
        log.setEditable(false);
        log.setPrefHeight(150);
        container.getChildren().add(new Label("Combat Log:"));
        container.getChildren().add(log);

        Button attackBtn = new Button("Punch");
        attackBtn.setOnAction(e -> doAttack());

        container.getChildren().add(attackBtn);
        return container;
    }

    private void doAttack() {
        Combatant actor = state.getParty().get(0);
        Combatant target = state.getEnemies().get(0);
        Action action = actor.getActions().get(0);

        engine.performAction(state, actor, action, List.of(target));
    }

    @Override
    public void onEvent(CombatEvent event) {
        log.appendText(event.getMessage() + "\n");
    }
}