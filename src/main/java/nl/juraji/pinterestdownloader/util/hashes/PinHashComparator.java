package nl.juraji.pinterestdownloader.util.hashes;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinImageHash;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Juraji on 29-4-2018.
 * Pinterest Downloader
 */
public final class PinHashComparator {
    // The minimum percentage of similarity for two hashes to be considered equal.
    private static final int MINIMUM_SIMILARITY = 85;

    /**
     * Test if the 2 PinImageHash objects equal by contents
     * <p>
     * Starts off by comparing calculated image contrast,
     * if contrasts equal the levenshtein distance is used to
     * determine exact match.
     *
     * @param a Object a to compare
     * @param b Object b to compare
     * @return True when the hashes are similar
     */
    public boolean compare(Pin a, Pin b) {
        // If either or both results are null the pins don't match
        if (notNullCheck(a) || notNullCheck(b)) {
            PinImageHash hashA = a.getImageHash();
            PinImageHash hashB = b.getImageHash();
            // Calculate hamming similarity of pin hash if contrasts are equal
            if (hashA.getContrast().equals(hashB.getContrast())) {
                int percentage = computeHammingSimilarityPercentage(hashA.getHash(), hashB.getHash());
                return percentage >= MINIMUM_SIMILARITY;
            }
        }

        return false;
    }

    private boolean notNullCheck(Pin pin) {
        return !(pin == null
                || pin.getImageHash() == null
                || pin.getImageHash().getContrast() == null
                || pin.getImageHash().getHash() == null);
    }

    private int computeHammingSimilarityPercentage(String a, String b) {
        int maxDistance = a.length();
        int invDistance = maxDistance - computeHammingDistance(a, b, maxDistance);

        if (invDistance == 0) {
            return 0;
        } else {
            BigDecimal percentage = new BigDecimal(invDistance / maxDistance * 100d)
                    .setScale(0, RoundingMode.HALF_EVEN);
            return percentage.intValueExact();
        }
    }

    private int computeHammingDistance(String a, String b, double maxDistance) {
        int distance = 0;

        for (int i = 0; i < maxDistance; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                distance++;
            }
        }

        return distance;
    }
}
