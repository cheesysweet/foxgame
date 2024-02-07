package se.miun.chrfin.foxgame;

import ch.rfin.ai.games.Player;
import ch.rfin.foxgame.Foxgame;
import ch.rfin.foxgame.Pos;
import ch.rfin.foxgame.Role;
import ch.rfin.foxgame.rules.Board;
import ch.rfin.foxgame.rules.State;
import se.miun.chrfin.foxgame.com.GameStatus;
import se.miun.chrfin.foxgame.setup.PlayerSetup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Anton Byström
 */
public class FoxGameEngine implements AiGameEngine {

  public record Pair(Double first, String second) { }
  private final PlayerSetup setup;
  private final Foxgame foxgame;
  private State state;

  public FoxGameEngine(PlayerSetup setup) {
    this.setup = setup;
    this.foxgame = new Foxgame();
    this.state = foxgame.getRoot();
  }

  /**
   * Return a move of the form "x1,y1 x2,y2".
   */
  @Override
  public String getMove(GameStatus status) {
    long maxTimeSlice = System.currentTimeMillis() + (status.timeSlice/2L);

    return miniMaxWithAlphaBetaPruning(state, maxTimeSlice, 10, foxgame.player(foxgame.getRoot()), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).second;
  }

  @Override
  public void updateState(String move) {
    //System.out.println("current state: " + state + " move: " + move);

    this.state = this.foxgame.transition(state, move);
  }

  @Override
  public String getPlayerName() {
    return setup.playerName;
  }

  private Pair miniMaxWithAlphaBetaPruning(State state, long maxTimeSlice, int depth, Player player, Double alpha, Double beta) {
    if (this.foxgame.terminal(state) || System.currentTimeMillis() >= maxTimeSlice || depth == 0) {
      /*if (state.getCurrentPlayer() == Role.FOX) {
        return new Pair(evalFox(state),null);
      } else {
        return new Pair(evalSheep(state),null);
      }

       */
      return new Pair(eval(state),null);
    }

    boolean maxPlayer = (player == Player.MAX);
    double value = (maxPlayer) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    String action = null;

    for (String child : this.foxgame.possibleActionsIn(state)) {
      State newState = foxgame.transition(state, child); // gets state of child
      if (state.getHistory().contains(newState.getBoard())) {
        continue;
      }
      Pair pair = miniMaxWithAlphaBetaPruning(newState, maxTimeSlice, depth-1, player.next(), alpha, beta);

        if (maxPlayer) {
          if (value < pair.first) {
            value = pair.first;
            action = child;
            alpha = Math.max(alpha, value);
          }
          if (beta <= value) {
            return new Pair(value, action);
          }
        } else {
          if (value > pair.first) {
            value = pair.first;
            action = child;
            beta = Math.min(beta, value);
          }
          if (value <= alpha) {
            return new Pair(value, action);
          }
        }
    }
    return new Pair(value, action);
  }

  private Double evaluate(State state) {
    double score = 0.0;

    if (this.foxgame.tryUtility(state).isPresent()) {
      score += this.foxgame.utilityOf(state) * 100;
    }

    int sheepSize = state.getSheep().size();
    int foxSize = state.getFoxes().size();
    int hs = state.getSheep().stream().map(Pos::getY).reduce(0, Integer::sum);

    if (state.getCurrentPlayer() != Role.FOX) {
      var a1 = state.getSheep().stream().filter(endPositions::contains).count();
      int e1 = foxSize - sheepSize;
      score += e1 - (a1 * 10);
    } else {
      var b1 = state.getSheep().stream().filter(endPositions::contains).count();
      var b2 = state.getSheep().stream().map(a -> 7-a.getY()).reduce(0, Integer::sum);
      int e2 = hs / sheepSize;
      score += e2 + (b1*20) -(b2*10);
    }
    score *= (state.getCurrentPlayer() == Role.FOX) ? 1 : -1;
    return score;
  }

  private final List<Pos> endPositions = Arrays.asList(
          new Pos.Impl(3,1), new Pos.Impl(4,1), new Pos.Impl(5,1),
          new Pos.Impl(3,2), new Pos.Impl(4,2), new Pos.Impl(5,2),
          new Pos.Impl(3,3), new Pos.Impl(4,3), new Pos.Impl(5,3)
  );

  private Double eval(State state) {
    double score = 0.0;
    Pos sheepPos = state.getSheep().stream().iterator().next();
    if (state.getCurrentPlayer() == Role.SHEEP) {
      if (this.foxgame.tryUtility(state).isPresent()) {
        score -= this.foxgame.utilityOf(state);
      }
      //score -= state.getSheep().stream().filter(endPositions::contains).count();
      score -= state.getFoxes().stream().map(Pos::getY).reduce(0, Integer::sum);
      score += state.getSheep().stream().map(Pos::getY).reduce(0,Integer::sum);
      score += (state.getFoxes().size() - state.getSheep().size());
      score += state.getSheep().stream().map(pos -> getNeighbors(state, pos)).reduce(0d,Double::sum);
      //score -= state.getBoard().getEmpty().stream().map(endPositions::contains).count();
    } else {
      if (this.foxgame.tryUtility(state).isPresent()) {
        score += this.foxgame.utilityOf(state);
      }
      score += state.getFoxes().stream().map(Pos::getY).reduce(0, Integer::sum);
      score -= state.getSheep().stream().map(Pos::getY).reduce(0,Integer::sum);
      //score += state.getBoard().getEmpty().stream().map(endPositions::contains).count();
      //score += state.getFoxes().size() - state.getSheep().size();
    }
    //score *= (state.getCurrentPlayer() == Role.SHEEP) ? 1 : -1;
    return score;
  }
  /* works for foxes!?
  private Double evalFox(State state) {
    double score = 0.0;

    score += state.getFoxes().stream().map(Pos::getY).reduce(0, Integer::sum);
    score += state.getFoxes().stream().map(pos -> getNeighbors(state, pos)).reduce(0d,Double::sum);

    if (this.foxgame.tryUtility(state).isPresent()) {
      score += this.foxgame.utilityOf(state);
    }
    return score;
  }

  private Double evalSheep(State state) {
    double score = 0.0;
    score -= state.getSheep().stream().map(Pos::getY).reduce(0,Integer::sum);
    score -= state.getSheep().stream().filter(endPositions::contains).map(Pos::getY).reduce(0,Integer::sum);
    score += state.getSheep().stream().map(pos -> getNeighbors(state, pos)).reduce(0d,Double::sum);

    if (this.foxgame.tryUtility(state).isPresent()) {
      score -= this.foxgame.utilityOf(state);
    }
    return score;
  }

 */

  //horizontal and vertical neighbors
  Collection<Pos> sheepNeighbors = Arrays.asList(
          new Pos.Impl(-1, -1),
          new Pos.Impl(-1, 0),
          new Pos.Impl(-1, +1),
          new Pos.Impl(0, -1),
          new Pos.Impl(0, +1),
          new Pos.Impl(+1, +1),
          new Pos.Impl(+1, -1),
          new Pos.Impl(+1, 0)
  );

  // diagonal and 2 distant horizontal and vertical neighbors
  Collection<Pos> sheepNeighbors2 = Arrays.asList(
          //new Pos.Impl(-1, -1),
          new Pos.Impl(-2, 0),
          // Pos.Impl(-1, +1),
          new Pos.Impl(0, -2),
          new Pos.Impl(0, +2),
          //new Pos.Impl(+1, +1),
          //new Pos.Impl(+1, -1),
          new Pos.Impl(+2, 0)
  );

  // all node neighbours
  Collection<Pos> neighbors = Arrays.asList(
          new Pos.Impl(-1, -1),
          new Pos.Impl(-1, 0),
          new Pos.Impl(-1, +1),
          new Pos.Impl(0, -1),
          new Pos.Impl(0, +1),
          new Pos.Impl(+1, +1),
          new Pos.Impl(+1, -1),
          new Pos.Impl(+1, 0)
  );

  private Double getNeighbors(State state, Pos pos) {
    double score = 0d;
    score += sheepNeighbors.stream()
            .map(pos1 -> state.hasSheep(new Pos.Impl(pos1.getX() + pos.getX(), pos1.getY() + pos.getY())))
            .filter(Boolean::booleanValue)
            .count();
    score -= sheepNeighbors2.stream()
            .map(pos1 -> state.hasSheep(new Pos.Impl(pos1.getX() + pos.getX(), pos1.getY() + pos.getY())))
            .filter(Boolean::booleanValue)
            .count();
    score -= neighbors.stream()
            .map(pos1 -> state.hasFox(new Pos.Impl(pos1.getX() + pos.getX(), pos1.getY() + pos.getY())))
            .filter(Boolean::booleanValue)
            .count();
    return score;
  }

  private Double evalFox(State state) {
    double score = 0.0;

    score += state.getFoxes().stream().map(Pos::getY).reduce(0, Integer::sum);
    score -= state.getSheep().stream().map(Pos::getY).reduce(0,Integer::sum);
    score += state.getSheep().stream().filter(endPositions::contains).map(Pos::getY).reduce(0,Integer::sum);
    score += state.getSheep().stream().map(pos -> getNeighbors(state, pos)).reduce(0d,Double::sum);
    score += (state.getFoxes().size() - state.getSheep().size());

    if (this.foxgame.tryUtility(state).isPresent()) {
      score += this.foxgame.utilityOf(state);
    }
    return score;
  }

  private Double evalSheep(State state) {
    double score = 0.0;

    score -= state.getFoxes().stream().map(Pos::getY).reduce(0, Integer::sum);
    score += state.getSheep().stream().map(Pos::getY).reduce(0,Integer::sum);
    score -= state.getSheep().stream().filter(endPositions::contains).map(Pos::getY).reduce(0,Integer::sum);
    //score += state.getSheep().stream().map(pos -> getNeighbors(state, pos)).reduce(0d,Double::sum);
    //score += (state.getFoxes().size() - state.getSheep().size());
    //score -= state.getBoard().getEmpty().stream().map(endPositions::contains).count();

    //score *= -1;
    if (this.foxgame.tryUtility(state).isPresent()) {
      score -= this.foxgame.utilityOf(state);
    }
    return score;
  }
}
