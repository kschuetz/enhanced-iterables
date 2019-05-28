package dev.marksman.enhancediterables;

final class Validation {

    private Validation() {
    }

    static void requirePositive(String paramName, int value) {
        if (value < 1) {
            throw new IllegalArgumentException(paramName + " must be >= 1");
        }
    }

    static void requireNonNegative(String paramName, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(paramName + " must be >= 0");
        }
    }

    static void validateTake(int count) {
        requireNonNegative("count", count);
    }

    static void validateDrop(int count) {
        requireNonNegative("count", count);
    }

    static void validateSlide(int k) {
        requirePositive("k", k);
    }

}
