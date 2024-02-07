import static net.finkn.foxgame.util.Role.FOX;
import static net.finkn.foxgame.util.Role.SHEEP;

import net.finkn.foxgame.PerformanceTest;
import net.finkn.foxgame.adapters.EngineFactory;
import se.miun.chrfin.foxgame.AiGameEngine;
import se.miun.chrfin.foxgame.FoxGameEngine;
import se.miun.chrfin.foxgame.setup.PlayerSetup;
import se.miun.chrfin.foxgame.setup.PlayerSetupParser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class ExampleTest {
  public static void main(String[] args) {
    EngineFactory factory = new MyFactory();
    boolean result;

    // Some example tests...
    List<Boolean> results = new LinkedList<Boolean>();
    List<Integer> hard = Arrays.asList(
            151,90,773,9,345,937,14,15,17,21,23,new Random().nextInt(1000), new Random().nextInt(1000)
    );
    List<Integer> times = Arrays.asList(
            200,300,400,500
    );

    List<Integer> time = Arrays.asList(
            200
    );

    // Use the default test parameters.
    //result = PerformanceTest.test(factory).run();
    //System.out.println("Result: " + result);

    // Tweaking each parameter.
    /*
    result = PerformanceTest.test(factory)
      .seeds(345,937,773,14,new Random().nextInt(1000), new Random().nextInt(1000)) // Use random() for a random seed (default is 1)
      .matches(10)       // Run 5 matches per level (default is 10)
      .levels(1)     // Runs levels 0 and 2 (default is 0, 1, and 2) (max 3)
      .timeouts(200)    // Use 200 ms timeout (default is 500 and 300)
      .roles(SHEEP)       // Only play as fox (default is FOX and SHEEP)
      //.agents(new MyFactory()) // Want to play against another agent? Put it
                        // here. Not used by default.
      .run(.5);         // Win threshold of 50% (default is 80%)
    System.out.println("Result: " + result);

     */


    for (Integer integer : hard) {
      for (Integer t: time) {

        // Tweaking each parameter.
        results.add(PerformanceTest.test(factory)
                .seeds(integer) // Use random() for a random seed (default is 1)
                .matches(10)       // Run 5 matches per level (default is 10)
                .levels(1)     // Runs levels 0 and 2 (default is 0, 1, and 2) (max 3)
                .timeouts(t)    // Use 200 ms timeout (default is 500 and 300)
                .roles(SHEEP)
                // Only play as fox (default is FOX and SHEEP)
                //.agents(new MyFactory()) // Want to play against another agent? Put it
                // here. Not used by default.
                .run(.85));         // Win threshold of 50% (default is 80%)
        //System.out.println("Result: " + result);
      }
    }

    int passed = 0;
    int failed = 0;
    for (boolean res: results) {
      if (res) {
        passed ++;
      } else {
        failed ++;
      }
    }
    System.out.println("Passed: " + passed);
    System.out.println("Failed: " + failed);
  }
}

class MyFactory implements EngineFactory {
  @Override
  public AiGameEngine getEngine(String role) {
    var setup = new PlayerSetup(
            "localhost", 6000, role, "fox", ""
    );

    return new FoxGameEngine(setup); // Your code here.
    // Something like
    // return new AwesomeFoxgameEngine();
  }

  @Override
  public String toString() {
    return "me"; // Or "AwesomeFoxgameEngine" if you want to. :)
  }
}
