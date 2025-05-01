/**
 * Represents a word and its associated cumulative probability threshold.
 *
 * Each instance holds a specific word alongside the cumulative probability that this word
 * or any preceding words in an ordered list should be selected. The probability value
 * must be greater than zero and at most one.
 *
 * @param word the word being represented
 * @param cumulativeProbability the cumulative probability threshold in the range (0., 1.]
 */
public record WordProbability(String word, double cumulativeProbability) {
}