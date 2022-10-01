package io.github.rajat19.filters;

import org.apache.lucene.analysis.TokenStream;

import java.util.BitSet;
import java.util.IdentityHashMap;

import static java.lang.Character.UnicodeBlock.BENGALI;
import static java.lang.Character.UnicodeBlock.DEVANAGARI;
import static java.lang.Character.UnicodeBlock.GUJARATI;
import static java.lang.Character.UnicodeBlock.GURMUKHI;
import static java.lang.Character.UnicodeBlock.KANNADA;
import static java.lang.Character.UnicodeBlock.MALAYALAM;
import static java.lang.Character.UnicodeBlock.ORIYA;
import static java.lang.Character.UnicodeBlock.TAMIL;
import static java.lang.Character.UnicodeBlock.TELUGU;
import static java.lang.Character.UnicodeBlock.of;

/**
 * This is copy of
 * <a href="https://github.com/apache/lucene/blob/main/lucene/analysis/common/src/java/org/apache/lucene/analysis/in/IndicNormalizer.java">
 *  IndicNormalizer
 * </a> from lucene library
 * Normalizes the Unicode representation of text in Indian languages.
 * <p>
 * Follows guidelines from Unicode 5.2, chapter 6, South Asian Scripts I
 * and graphical decompositions from
 * <a href="http://ldc.upenn.edu/myl/IndianScriptsUnicode.html">
 *     IndianScriptsUnicode
 * </a>
 * </p>
 */
public class IndicNormalizer extends IndicFilter {

    private static final IdentityHashMap<Character.UnicodeBlock, ScriptData> SCRIPTS =
            new IdentityHashMap<>(9);

    static {
        SCRIPTS.put(DEVANAGARI, new ScriptData(1, 0x0900));
        SCRIPTS.put(BENGALI, new ScriptData(2, 0x0980));
        SCRIPTS.put(GURMUKHI, new ScriptData(4, 0x0A00));
        SCRIPTS.put(GUJARATI, new ScriptData(8, 0x0A80));
        SCRIPTS.put(ORIYA, new ScriptData(16, 0x0B00));
        SCRIPTS.put(TAMIL, new ScriptData(32, 0x0B80));
        SCRIPTS.put(TELUGU, new ScriptData(64, 0x0C00));
        SCRIPTS.put(KANNADA, new ScriptData(128, 0x0C80));
        SCRIPTS.put(MALAYALAM, new ScriptData(256, 0x0D00));
    }

    /**
     * Decompositions according to Unicode 5.2,
     * and <a href="http://ldc.upenn.edu/myl/IndianScriptsUnicode.html">IndianScriptsUnicode</a>
     * <p>
     * Most of these are not handled by unicode normalization anyway.
     * <p>
     * The numbers here represent offsets into the respective codepages,
     * with -1 representing null and 0xFF representing zero-width joiner.
     * <p>
     * the columns are: ch1, ch2, ch3, res, flags
     * ch1, ch2, and ch3 are the decomposition
     * res is the composition, and flags are the scripts to which it applies.
     */
    private static final int[][] DECOMPOSITIONS = {
            /* devanagari, gujarati vowel candra O */
            {0x05, 0x3E, 0x45, 0x11, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari short O */
            {0x05, 0x3E, 0x46, 0x12, flag(DEVANAGARI)},
            /* devanagari, gujarati letter O */
            {0x05, 0x3E, 0x47, 0x13, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari letter AI, gujarati letter AU */
            {0x05, 0x3E, 0x48, 0x14, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari, bengali, gurmukhi, gujarati, oriya AA */
            {0x05, 0x3E, -1, 0x06, flag(DEVANAGARI) | flag(BENGALI) | flag(GURMUKHI) | flag(GUJARATI) | flag(ORIYA)},
            /* devanagari letter candra A */
            {0x05, 0x45, -1, 0x72, flag(DEVANAGARI)},
            /* gujarati vowel candra E */
            {0x05, 0x45, -1, 0x0D, flag(GUJARATI)},
            /* devanagari letter short A */
            {0x05, 0x46, -1, 0x04, flag(DEVANAGARI)},
            /* gujarati letter E */
            {0x05, 0x47, -1, 0x0F, flag(GUJARATI)},
            /* gurmukhi, gujarati letter AI */
            {0x05, 0x48, -1, 0x10, flag(GURMUKHI) | flag(GUJARATI)},
            /* devanagari, gujarati vowel candra O */
            {0x05, 0x49, -1, 0x11, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari short O */
            {0x05, 0x4A, -1, 0x12, flag(DEVANAGARI)},
            /* devanagari, gujarati letter O */
            {0x05, 0x4B, -1, 0x13, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari letter AI, gurmukhi letter AU, gujarati letter AU */
            {0x05, 0x4C, -1, 0x14, flag(DEVANAGARI) | flag(GURMUKHI) | flag(GUJARATI)},
            /* devanagari, gujarati vowel candra O */
            {0x06, 0x45, -1, 0x11, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari short O */
            {0x06, 0x46, -1, 0x12, flag(DEVANAGARI)},
            /* devanagari, gujarati letter O */
            {0x06, 0x47, -1, 0x13, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari letter AI, gujarati letter AU */
            {0x06, 0x48, -1, 0x14, flag(DEVANAGARI) | flag(GUJARATI)},
            /* malayalam letter II */
            {0x07, 0x57, -1, 0x08, flag(MALAYALAM)},
            /* devanagari letter UU */
            {0x09, 0x41, -1, 0x0A, flag(DEVANAGARI)},
            /* tamil, malayalam letter UU (some styles) */
            {0x09, 0x57, -1, 0x0A, flag(TAMIL) | flag(MALAYALAM)},
            /* malayalam letter AI */
            {0x0E, 0x46, -1, 0x10, flag(MALAYALAM)},
            /* devanagari candra E */
            {0x0F, 0x45, -1, 0x0D, flag(DEVANAGARI)},
            /* devanagari short E */
            {0x0F, 0x46, -1, 0x0E, flag(DEVANAGARI)},
            /* devanagari AI */
            {0x0F, 0x47, -1, 0x10, flag(DEVANAGARI)},
            /* oriya AI */
            {0x0F, 0x57, -1, 0x10, flag(ORIYA)},
            /* malayalam letter OO */
            {0x12, 0x3E, -1, 0x13, flag(MALAYALAM)},
            /* telugu, kannada letter AU */
            {0x12, 0x4C, -1, 0x14, flag(TELUGU) | flag(KANNADA)},
            /* telugu letter OO */
            {0x12, 0x55, -1, 0x13, flag(TELUGU)},
            /* tamil, malayalam letter AU */
            {0x12, 0x57, -1, 0x14, flag(TAMIL) | flag(MALAYALAM)},
            /* oriya letter AU */
            {0x13, 0x57, -1, 0x14, flag(ORIYA)},
            /* devanagari qa */
            {0x15, 0x3C, -1, 0x58, flag(DEVANAGARI)},
            /* devanagari, gurmukhi khha */
            {0x16, 0x3C, -1, 0x59, flag(DEVANAGARI) | flag(GURMUKHI)},
            /* devanagari, gurmukhi ghha */
            {0x17, 0x3C, -1, 0x5A, flag(DEVANAGARI) | flag(GURMUKHI)},
            /* devanagari, gurmukhi za */
            {0x1C, 0x3C, -1, 0x5B, flag(DEVANAGARI) | flag(GURMUKHI)},
            /* devanagari dddha, bengali, oriya rra */
            {0x21, 0x3C, -1, 0x5C, flag(DEVANAGARI) | flag(BENGALI) | flag(ORIYA)},
            /* devanagari, bengali, oriya rha */
            {0x22, 0x3C, -1, 0x5D, flag(DEVANAGARI) | flag(BENGALI) | flag(ORIYA)},
            /* malayalam chillu nn */
            {0x23, 0x4D, 0xFF, 0x7A, flag(MALAYALAM)},
            /* bengali khanda ta */
            {0x24, 0x4D, 0xFF, 0x4E, flag(BENGALI)},
            /* devanagari nnna */
            {0x28, 0x3C, -1, 0x29, flag(DEVANAGARI)},
            /* malayalam chillu n */
            {0x28, 0x4D, 0xFF, 0x7B, flag(MALAYALAM)},
            /* devanagari, gurmukhi fa */
            {0x2B, 0x3C, -1, 0x5E, flag(DEVANAGARI) | flag(GURMUKHI)},
            /* devanagari, bengali yya */
            {0x2F, 0x3C, -1, 0x5F, flag(DEVANAGARI) | flag(BENGALI)},
            /* telugu letter vocalic R */
            {0x2C, 0x41, 0x41, 0x0B, flag(TELUGU)},
            /* devanagari rra */
            {0x30, 0x3C, -1, 0x31, flag(DEVANAGARI)},
            /* malayalam chillu rr */
            {0x30, 0x4D, 0xFF, 0x7C, flag(MALAYALAM)},
            /* malayalam chillu l */
            {0x32, 0x4D, 0xFF, 0x7D, flag(MALAYALAM)},
            /* devanagari llla */
            {0x33, 0x3C, -1, 0x34, flag(DEVANAGARI)},
            /* malayalam chillu ll */
            {0x33, 0x4D, 0xFF, 0x7E, flag(MALAYALAM)},
            /* telugu letter MA */
            {0x35, 0x41, -1, 0x2E, flag(TELUGU)},
            /* devanagari, gujarati vowel sign candra O */
            {0x3E, 0x45, -1, 0x49, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari vowel sign short O */
            {0x3E, 0x46, -1, 0x4A, flag(DEVANAGARI)},
            /* devanagari, gujarati vowel sign O */
            {0x3E, 0x47, -1, 0x4B, flag(DEVANAGARI) | flag(GUJARATI)},
            /* devanagari, gujarati vowel sign AU */
            {0x3E, 0x48, -1, 0x4C, flag(DEVANAGARI) | flag(GUJARATI)},
            /* kannada vowel sign II */
            {0x3F, 0x55, -1, 0x40, flag(KANNADA)},
            /* gurmukhi vowel sign UU (when stacking) */
            {0x41, 0x41, -1, 0x42, flag(GURMUKHI)},
            /* tamil, malayalam vowel sign O */
            {0x46, 0x3E, -1, 0x4A, flag(TAMIL) | flag(MALAYALAM)},
            /* kannada vowel sign OO */
            {0x46, 0x42, 0x55, 0x4B, flag(KANNADA)},
            /* kannada vowel sign O */
            {0x46, 0x42, -1, 0x4A, flag(KANNADA)},
            /* malayalam vowel sign AI (if reordered twice) */
            {0x46, 0x46, -1, 0x48, flag(MALAYALAM)},
            /* telugu, kannada vowel sign EE */
            {0x46, 0x55, -1, 0x47, flag(TELUGU) | flag(KANNADA)},
            /* telugu, kannada vowel sign AI */
            {0x46, 0x56, -1, 0x48, flag(TELUGU) | flag(KANNADA)},
            /* tamil, malayalam vowel sign AU */
            {0x46, 0x57, -1, 0x4C, flag(TAMIL) | flag(MALAYALAM)},
            /* bengali, oriya vowel sign O, tamil, malayalam vowel sign OO */
            {0x47, 0x3E, -1, 0x4B, flag(BENGALI) | flag(ORIYA) | flag(TAMIL) | flag(MALAYALAM)},
            /* bengali, oriya vowel sign AU */
            {0x47, 0x57, -1, 0x4C, flag(BENGALI) | flag(ORIYA)},
            /* kannada vowel sign OO */
            {0x4A, 0x55, -1, 0x4B, flag(KANNADA)},
            /* gurmukhi letter I */
            {0x72, 0x3F, -1, 0x07, flag(GURMUKHI)},
            /* gurmukhi letter II */
            {0x72, 0x40, -1, 0x08, flag(GURMUKHI)},
            /* gurmukhi letter EE */
            {0x72, 0x47, -1, 0x0F, flag(GURMUKHI)},
            /* gurmukhi letter U */
            {0x73, 0x41, -1, 0x09, flag(GURMUKHI)},
            /* gurmukhi letter UU */
            {0x73, 0x42, -1, 0x0A, flag(GURMUKHI)},
            /* gurmukhi letter OO */
            {0x73, 0x4B, -1, 0x13, flag(GURMUKHI)},
    };

    static {
        for (final ScriptData sd : SCRIPTS.values()) {
            sd.decomposeMask = new BitSet(0x7F);
            for (final int[] decomposition : DECOMPOSITIONS) {
                final int ch = decomposition[0];
                final int flags = decomposition[4];
                if ((flags & sd.flag) != 0) {
                    sd.decomposeMask.set(ch);
                }
            }
        }
    }

    public IndicNormalizer(final TokenStream input) {
        super(input);
    }

    private static int flag(final Character.UnicodeBlock ub) {
        return SCRIPTS.get(ub).flag;
    }

    @Override
    public int filter(final char[] buffer, final int len) {
        return normalize(buffer, len);
    }

    /**
     * Normalizes input text, and returns the new length.
     * The length will always be less than or equal to the existing length.
     *
     * @param text input text
     * @param len  valid length
     * @return normalized length
     */
    public int normalize(final char[] text, int len) {
        for (int i = 0; i < len; i++) {
            final Character.UnicodeBlock block = of(text[i]);
            final ScriptData scriptData = SCRIPTS.get(block);
            if (scriptData != null) {
                final int character = text[i] - scriptData.base;
                if (scriptData.decomposeMask.get(character)) {
                    len = compose(character, block, scriptData, text, i, len);
                }
            }
        }
        return len;
    }

    /**
     * Compose into standard form any compositions in the decompositions table.
     */
    private int compose(final int ch0, final Character.UnicodeBlock block0, final ScriptData sd,
                        final char[] text, final int pos, int len) {
        if (pos + 1 >= len) /* need at least 2 chars! */ {
            return len;
        }

        final int ch1 = text[pos + 1] - sd.base;
        final Character.UnicodeBlock block1 = of(text[pos + 1]);
        if (block1 != block0) /* needs to be the same writing system */ {
            return len;
        }

        int ch2 = -1;

        if (pos + 2 < len) {
            ch2 = text[pos + 2] - sd.base;
            final Character.UnicodeBlock block2 = of(text[pos + 2]);
            if (text[pos + 2] == '\u200D') {
                // ZWJ
                ch2 = 0xFF;
            } else if (block2 != block1) {
                // still allow a 2-char match
                ch2 = -1;
            }
        }

        for (final int[] decomposition : DECOMPOSITIONS) {
            if (decomposition[0] == ch0 && (decomposition[4] & sd.flag) != 0) {
                if (decomposition[1] == ch1 && (decomposition[2] < 0 || decomposition[2] == ch2)) {
                    text[pos] = (char) (sd.base + decomposition[3]);
                    len = delete(text, pos + 1, len);
                    if (decomposition[2] >= 0) {
                        len = delete(text, pos + 1, len);
                    }
                    return len;
                }
            }
        }

        return len;
    }

    private static class ScriptData {
        final int flag;
        final int base;
        BitSet decomposeMask;

        ScriptData(final int flag, final int base) {
            this.flag = flag;
            this.base = base;
        }
    }
}


