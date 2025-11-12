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

        // pre-damage scaling
        pipeline.addHandler(new MomentumScalingHandler());
        pipeline.addHandler(new CloseRangeBonusHandler());
        pipeline.addHandler(new MgGainHandler());

        // damage negation
        pipeline.addHandler(new BarrierHandler());
        //pipeline.addHandler(new ArmorHandler());
        pipeline.addHandler(new DefenseHandler());

        // special behavior
        pipeline.addHandler(new CounterHandler());
        pipeline.addHandler(new InterruptHandler(bus));
        pipeline.addHandler(new ExecutionHandler());

        pipeline.addHandler(new ApplyDamageHandler()); // last stage

        // Hook up resolvers and engine.
        EffectResolver resolver = new EffectResolver(bus);
        BattleEngine engine = new BattleEngine(bus, pipeline, resolver);

        // Set up screen and show.
        BattleScreen screen = new BattleScreen(state, engine, bus);
        stage.setScene(new Scene(screen, 900, 600));
        stage.setTitle("Fraymark Combat Prototype");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}