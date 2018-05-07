package nl.juraji.pinterestdownloader.util.hashes;

import nl.juraji.pinterestdownloader.model.Pin;
import nl.juraji.pinterestdownloader.model.PinImageHash;

import java.util.BitSet;

/**
 * Created by Juraji on 29-4-2018.
 * Pinterest Downloader
 */
public final class PinHashComparator {
    // The minimum percentage of similarity for two hashes to be considered equal.
    private static final double HASH_LENGTH = 9900;
    private static final double MAXIMUM_DISTANCE = 1000;

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
        if (notNullCheck(a) && notNullCheck(b)) {
            PinImageHash hashA = a.getImageHash();
            PinImageHash hashB = b.getImageHash();
            // Calculate hamming similarity of pin hash if contrasts are equal
            if (hashA.getContrast().equals(hashB.getContrast())) {
                double distance = computeBitwiseHammingDistance(hashA.getHash(), hashB.getHash());
                return distance <= MAXIMUM_DISTANCE;
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

    private int computeBitwiseHammingDistance(BitSet a, BitSet b) {
        int distance = 0;

        for (int i = 0; i < HASH_LENGTH; i++) {
            if (a.get(i) == b.get(i)) {
                distance++;
            }
        }

        return distance;
    }
}
