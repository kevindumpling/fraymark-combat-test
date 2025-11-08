package fraymark.app;

import fraymark.combat.engine.*;
import fraymark.combat.events.EventBus;
import fraymark.combat.damage.pipeline.*;
import fraymark.model.combatants.*;
import fraymark.model.stats.*;
import fraymark.model.actions.physical.BasicPhysical;
import fraymark.ui.javafx.screens.BattleScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

/***
 * The driver class for the Fraymark Application.
 */
public class FraymarkApp extends Application {
    @Override
    public void start(Stage stage) {
        // Initialize test data
        Combatant sam = buildPlayer();
        Combatant goon = buildEnemy();

        BattleState state = new BattleState(List.of(sam), List.of(goon));

        // Setup systems
        EventBus bus = new EventBus();
        DamagePipeline pipeline = new DamagePipeline();
        pipeline.addHandler(new BarrierHandler());
        pipeline.addHandler(new ArmorHandler());
        pipeline.addHandler(new ExecutionHandler());
        pipeline.addHandler(new CounterHandler());
        pipeline.addHandler(new InterruptHandler(bus));

        EffectResolver resolver = new EffectResolver();
        BattleEngine engine = new BattleEngine(bus, pipeline, resolver);

        // Build and show UI
        BattleScreen battleScreen = new BattleScreen(state, engine, bus);
        stage.setScene(new Scene(battleScreen, 900, 600));
        stage.setTitle("Fraymark Combat Prototype");
        stage.show();
    }

    private Combatant buildPlayer() {
        Stats stats = new Stats(200, 50, 40, 55, 35, 20);
        Resources res = new Resources(200, 0, 0, 0, 0);
        PlayerCharacter sam = new PlayerCharacter("sam", "Sam", stats, res, false);
        sam.addAction(new BasicPhysical("Punch", 40));
        return sam;
    }

    private Combatant buildEnemy() {
        Stats stats = new Stats(150, 45, 35, 20, 20, 20);
        Resources res = new Resources(150, 0, 0, 0, 0);
        return new Enemy("goon", "Unarmored Goon", stats, res, false, 1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}