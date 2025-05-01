import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * Unit tests for WordPredictor, including validation checks and
 * Monte Carlo–style distribution tests.
 */
public class WordPredictorTest {

    // Change this seed or the createTestMap() method to customize behavior
    private static final long RANDOM_SEED = 12345L;

    private WordPredictor predictor;
    private Map<String, List<WordProbability>> testMap;

    @BeforeEach
    void setUp() {
        testMap = createTestMap();
        predictor = new WordPredictor(testMap, new Random(RANDOM_SEED));
    }

    /**
     * Modify this method if you want to use your own words and probabilities.
     * Each list must have strictly ascending cumulativeProbability values ending at 1.0.
     */
    private Map<String, List<WordProbability>> createTestMap() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        map.put("the", List.of(
            new WordProbability("cat",    0.1),
            new WordProbability("dog",    0.5),
            new WordProbability("lizard", 1.0)
        ));
        map.put("cat", List.of(
            new WordProbability("sat", 0.6),
            new WordProbability("ate", 1.0)
        ));
        map.put("alphabet", List.of(
            new WordProbability("a", 0.034859),
            new WordProbability("b", 0.098120),
            new WordProbability("c", 0.153596),
            new WordProbability("d", 0.213720),
            new WordProbability("e", 0.225172),
            new WordProbability("f", 0.293764),
            new WordProbability("g", 0.354170),
            new WordProbability("h", 0.392903),
            new WordProbability("i", 0.423932),
            new WordProbability("j", 0.474427),
            new WordProbability("k", 0.512483),
            new WordProbability("l", 0.580126),
            new WordProbability("m", 0.586292),
            new WordProbability("n", 0.603294),
            new WordProbability("o", 0.613061),
            new WordProbability("p", 0.613443),
            new WordProbability("q", 0.647686),
            new WordProbability("r", 0.680489),
            new WordProbability("s", 0.740058),
            new WordProbability("t", 0.770907),
            new WordProbability("u", 0.826005),
            new WordProbability("v", 0.879152),
            new WordProbability("w", 0.917383),
            new WordProbability("x", 0.943437),
            new WordProbability("y", 0.971542),
            new WordProbability("z", 1.000000)
        ));
        return map;
    }

    // -- Validation tests --

    @Test
    void constructorThrowsOnNullMap() {
        assertThrows(NullPointerException.class,
            () -> new WordPredictor(null, new Random()));
    }

    @Test
    void constructorThrowsOnNullRNG() {
        assertThrows(NullPointerException.class,
            () -> new WordPredictor(testMap, null));
    }

    @Test
    void validateThrowsOnEmptyMap() {
        Map<String, List<WordProbability>> empty = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(empty, new Random()));
    }

    @Test
    void validateThrowsOnEmptyList() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        map.put("foo", Collections.emptyList());
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(map, new Random()));
    }

    @Test
    void validateThrowsOnNonAscendingProbabilities() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        map.put("foo", List.of(
            new WordProbability("a", 0.5),
            new WordProbability("b", 0.4),
            new WordProbability("c", 1.0)
        ));
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(map, new Random()));
    }

    @Test
    void validateThrowsIfFinalProbabilityNotOne() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        map.put("foo", List.of(
            new WordProbability("a", 0.3),
            new WordProbability("b", 0.7)  // ends at 0.7 instead of 1.0
        ));
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(map, new Random()));
    }

    @Test
    void validateThrowsOnProbabilityOutOfRange() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        map.put("foo", List.of(
            new WordProbability("a", 0.0),
            new WordProbability("b", 1.0)
        ));
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(map, new Random()));

        map.clear();
        map.put("foo", List.of(
            new WordProbability("a", 0.5),
            new WordProbability("b", 1.2)
        ));
        assertThrows(IllegalArgumentException.class,
            () -> new WordPredictor(map, new Random()));
    }

    // -- Monte Carlo distribution tests --

    @Test
    void monteCarloDistributionForThe() {
        int trials = 100_000;
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < trials; i++) {
            String next = predictor.predict("the");
            counts.merge(next, 1, Integer::sum);
        }

        double freqCat    = counts.getOrDefault("cat",    0) / (double) trials;
        double freqDog    = counts.getOrDefault("dog",    0) / (double) trials;
        double freqLizard = counts.getOrDefault("lizard", 0) / (double) trials;

        // Expected probabilities: cat=0.1, dog=0.4, lizard=0.5
        double tol = 0.02;
        assertEquals(0.10, freqCat,    tol, "cat frequency out of tolerance");
        assertEquals(0.40, freqDog,    tol, "dog frequency out of tolerance");
        assertEquals(0.50, freqLizard, tol, "lizard frequency out of tolerance");
    }

    @Test
    void monteCarloDistributionForCat() {
        int trials = 100_000;
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < trials; i++) {
            String next = predictor.predict("cat");
            counts.merge(next, 1, Integer::sum);
        }

        double freqSat = counts.getOrDefault("sat", 0) / (double) trials;
        double freqAte = counts.getOrDefault("ate", 0) / (double) trials;

        // Expected probabilities: sat=0.6, ate=0.4
        double tol = 0.02;
        assertEquals(0.60, freqSat, tol, "sat frequency out of tolerance");
        assertEquals(0.40, freqAte, tol, "ate frequency out of tolerance");
    }

    @Test
    void monteCarloDistributionForAlphabet() {
        int trials = 300_000;
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < trials; i++) {
            String next = predictor.predict("alphabet");
            counts.merge(next, 1, Integer::sum);
        }

        double tol = 0.01;
        // Monte Carlo assertions
        double freqA = counts.getOrDefault("a", 0) / (double) trials;
        assertEquals(0.034859, freqA, tol, "a frequency out of tolerance");
        double freqB = counts.getOrDefault("b", 0) / (double) trials;
        assertEquals(0.063261, freqB, tol, "b frequency out of tolerance");
        double freqC = counts.getOrDefault("c", 0) / (double) trials;
        assertEquals(0.055476, freqC, tol, "c frequency out of tolerance");
        double freqD = counts.getOrDefault("d", 0) / (double) trials;
        assertEquals(0.060123, freqD, tol, "d frequency out of tolerance");
        double freqE = counts.getOrDefault("e", 0) / (double) trials;
        assertEquals(0.011452, freqE, tol, "e frequency out of tolerance");
        double freqF = counts.getOrDefault("f", 0) / (double) trials;
        assertEquals(0.068591, freqF, tol, "f frequency out of tolerance");
        double freqG = counts.getOrDefault("g", 0) / (double) trials;
        assertEquals(0.060407, freqG, tol, "g frequency out of tolerance");
        double freqH = counts.getOrDefault("h", 0) / (double) trials;
        assertEquals(0.038732, freqH, tol, "h frequency out of tolerance");
        double freqI = counts.getOrDefault("i", 0) / (double) trials;
        assertEquals(0.031029, freqI, tol, "i frequency out of tolerance");
        double freqJ = counts.getOrDefault("j", 0) / (double) trials;
        assertEquals(0.050496, freqJ, tol, "j frequency out of tolerance");
        double freqK = counts.getOrDefault("k", 0) / (double) trials;
        assertEquals(0.038056, freqK, tol, "k frequency out of tolerance");
        double freqL = counts.getOrDefault("l", 0) / (double) trials;
        assertEquals(0.067643, freqL, tol, "l frequency out of tolerance");
        double freqM = counts.getOrDefault("m", 0) / (double) trials;
        assertEquals(0.006165, freqM, tol, "m frequency out of tolerance");
        double freqN = counts.getOrDefault("n", 0) / (double) trials;
        assertEquals(0.017003, freqN, tol, "n frequency out of tolerance");
        double freqO = counts.getOrDefault("o", 0) / (double) trials;
        assertEquals(0.009766, freqO, tol, "o frequency out of tolerance");
        double freqP = counts.getOrDefault("p", 0) / (double) trials;
        assertEquals(0.000382, freqP, tol, "p frequency out of tolerance");
        double freqQ = counts.getOrDefault("q", 0) / (double) trials;
        assertEquals(0.034244, freqQ, tol, "q frequency out of tolerance");
        double freqR = counts.getOrDefault("r", 0) / (double) trials;
        assertEquals(0.032802, freqR, tol, "r frequency out of tolerance");
        double freqS = counts.getOrDefault("s", 0) / (double) trials;
        assertEquals(0.059570, freqS, tol, "s frequency out of tolerance");
        double freqT = counts.getOrDefault("t", 0) / (double) trials;
        assertEquals(0.030849, freqT, tol, "t frequency out of tolerance");
        double freqU = counts.getOrDefault("u", 0) / (double) trials;
        assertEquals(0.055098, freqU, tol, "u frequency out of tolerance");
        double freqV = counts.getOrDefault("v", 0) / (double) trials;
        assertEquals(0.053147, freqV, tol, "v frequency out of tolerance");
        double freqW = counts.getOrDefault("w", 0) / (double) trials;
        assertEquals(0.038232, freqW, tol, "w frequency out of tolerance");
        double freqX = counts.getOrDefault("x", 0) / (double) trials;
        assertEquals(0.026054, freqX, tol, "x frequency out of tolerance");
        double freqY = counts.getOrDefault("y", 0) / (double) trials;
        assertEquals(0.028104, freqY, tol, "y frequency out of tolerance");
        double freqZ = counts.getOrDefault("z", 0) / (double) trials;
        assertEquals(0.028458, freqZ, tol, "z frequency out of tolerance");
    }
}
