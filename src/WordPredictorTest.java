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
    private static final int N = 100_000;        // size of the "numbers" list
    private static final int TRIALS = 100_000;  // number of Monte Carlo samples
    private static final double TOL = 0.02;     // tolerance for frequency assertions

    private WordPredictor predictor;
    private Map<String, List<WordProbability>> testMap;

    @BeforeEach
    void setUp() {
        testMap = createTestMap();
        predictor = new WordPredictor(testMap, new Random(RANDOM_SEED));
    }

    /**
     * Builds a map with three keys:
     *  • "the"   → [cat(.1), dog(.5), lizard(1.0)]
     *  • "cat"   → [sat(.6), ate(1.0)]
     *  • "numbers" → [1(p1), 2(p2), …, N(pN)]
     *
     * where the pi are cumulative probabilities summing to 1.0.
     */
    private Map<String, List<WordProbability>> createTestMap() {
        Map<String, List<WordProbability>> map = new HashMap<>();
        // existing small examples
        map.put("the", List.of(
            new WordProbability("cat",    0.1),
            new WordProbability("dog",    0.5),
            new WordProbability("lizard", 1.0)
        ));
        map.put("cat", List.of(
            new WordProbability("sat", 0.6),
            new WordProbability("ate", 1.0)
        ));

        // large "numbers" example
        Random rnd = new Random(RANDOM_SEED);
        double[] weights = new double[N];
        double total = 0;
        for (int i = 0; i < N; i++) {
            weights[i] = rnd.nextDouble();
            total += weights[i];
        }
        List<WordProbability> numList = new ArrayList<>(N);
        double cum = 0;
        for (int i = 0; i < N; i++) {
            cum += weights[i] / total;
            numList.add(new WordProbability(String.valueOf(i + 1), cum));
        }
        map.put("numbers", numList);

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
            new WordProbability("b", 0.7)  // ends at 0.7
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
            counts.merge(predictor.predict("the"), 1, Integer::sum);
        }

        double freqCat    = counts.getOrDefault("cat",    0) / (double) trials;
        double freqDog    = counts.getOrDefault("dog",    0) / (double) trials;
        double freqLizard = counts.getOrDefault("lizard", 0) / (double) trials;

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
            counts.merge(predictor.predict("cat"), 1, Integer::sum);
        }

        double freqSat = counts.getOrDefault("sat", 0) / (double) trials;
        double freqAte = counts.getOrDefault("ate", 0) / (double) trials;

        double tol = 0.02;
        assertEquals(0.60, freqSat, tol, "sat frequency out of tolerance");
        assertEquals(0.40, freqAte, tol, "ate frequency out of tolerance");
    }

    @Test
    void monteCarloDistributionForNumbers() {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < TRIALS; i++) {
            counts.merge(predictor.predict("numbers"), 1, Integer::sum);
        }

        List<WordProbability> list = testMap.get("numbers");
        double prev = 0.0;
        for (WordProbability wp : list) {
            double expected = wp.cumulativeProbability() - prev;
            double freq = counts.getOrDefault(wp.word(), 0) / (double) TRIALS;
            assertEquals(expected, freq, TOL, wp.word() + " frequency out of tolerance");
            prev = wp.cumulativeProbability();
        }
    }
}
