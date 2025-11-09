package fraymark.app;

import fraymark.combat.engine.*;
import fraymark.combat.events.EventBus;
import fraymark.combat.damage.pipeline.*;
import fraymark.model.combatants.*;
import fraymark.model.stats.*;
import fraymark.model.actions.physical.BasicPhysicalAction;
import fraymark.ui.javafx.screens.BattleScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import fraymark.data.*;

/***
 * The driver class for the Fraymark Application.
 */
public class FraymarkApp extends Application {
    @Override
    public void start(Stage stage) {

        // Load data.
        DataAssembler assembler = new DataAssembler();
        BattleState state = assembler.buildFullBattle();
        // DEBUG
        System.out.println("Party: " + state.getParty().size());
        System.out.println("Enemies: " + state.getEnemies().size());
        // Hook up event pipeline.
        EventBus bus = new EventBus();
        DamagePipeline pipeline = new DamagePipeline();
        pipeline.addHandler(new BarrierHandler());
        pipeline.addHandler(new ArmorHandler());
        pipeline.addHandler(new ExecutionHandler());
        pipeline.addHandler(new CounterHandler());
        pipeline.addHandler(new InterruptHandler(bus));
        pipeline.addHandler(new ApplyDamageHandler()); // last stage

        // Hook up resolvers and engine.
        EffectResolver resolver = new EffectResolver();
        BattleEngine engine = new BattleEngine(bus, pipeline, resolver);

        // Set up screen and show.
        BattleScreen screen = new BattleScreen(state, engine, bus);
        stage.setScene(new Scene(screen, 900, 600));
        stage.setTitle("Fraymark Combat Prototype");
        stage.show();
    }

    private Combatant buildPlayer() {
        Stats stats = new Stats(200, 50, 40, 55, 35, 20);
        Resources res = new Resources(200, 0, 0, 0, 0);
        PlayerCharacter sam = new PlayerCharacter("sam", "Sam", stats, res, false);
        sam.addAction(new BasicPhysicalAction("Punch", 40));
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