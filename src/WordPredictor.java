import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordPredictor {
    private Random rng;
    // Each key is a word and the value is a list of words that could possibly follow it
    // Each word is stored along with its cumulative probability
    // The cumulative probability is the probability in the range (0, 1.] that this word
    // or any of the preceding words in the list of possibilities should be chosen.
    // The probabilities must be in strictly ascending order and the final probability
    // must be 1.0. There can be no empty lists.
    // Example: 
    // { 
    //   the: [[cat, .1], [dog, .5], [lizard, 1.0]],
    //   cat: [[sat, .6], [ate, 1.0]]
    // }
    // In this example:
    // there is a 10% chance that "cat" follows "the" (.1)
    // there is a 40% chance that "dog" follows "the" (.5-.1=.4)
    // there is a 50% chance that "lizard" follows "the" (1.-.5=.5)
    //
    // there is a 60% chance "sat" follows "cat" (.6)
    // there is a 40% chance "ate" follows ""cat" (1.-.6=.4)
    private Map<String, List<WordProbability>> probs;
    
    // Sets the variables and throws an IllegalArgumentException if the probabilities are malformed
    public WordPredictor(Map<String, List<WordProbability>> probs, Random rng) {
        validateMap();
        this.probs = probs;
        this.rng = rng;
    }

    // Sets the variables and throws an IllegalArgumentException if the probabilities are malformed
    // Creates a new RNG
    public WordPredictor(Map<String, List<WordProbability>> probs) {
        this(probs, new Random());
    }


    // throws an illegal argument exception if probabilities are malformed or empty
    private void validateMap() {
        // TODO
    }

    /**
     * Predicts the next word in a sentence given the previous word.
     * 
     * Predictions are made given weighted probabilities.
     * 
     * @param word the previous word in the sentence
     * @return a prediction of the next word
     */
    public String predict(String word) {
        // Implement this so it runs in O(log(n)) time where n is probs.get(word).size()
        // Having a hard time getting started? Implement it in O(n) time first, then optimize.
        return null;
    }
}