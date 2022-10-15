package io.github.rajat19.filters;

import org.apache.lucene.analysis.TokenStream;

/**
 * This is modified copy of
 * <a href="https://github.com/apache/lucene/blob/main/lucene/analysis/common/src/java/org/apache/lucene/analysis/hi/HindiNormalizer.java">
 * HindiNormalizer
 * </a> from lucene library
 * <p>
 * Normalizes text to remove some differences in spelling variations.
 * <p>
 * Implements the Hindi-language specific algorithm specified in:
 * <a href="http://web2py.iiit.ac.in/publications/default/download/inproceedings.pdf.3fe5b38c-02ee-41ce-9a8f-3e745670be32.pdf">
 * <i>Word normalization in Indian languages</i> by
 * Prasad Pingali and Vasudeva Varma.
 * </a>
 *
 * <p>
 * with the following additions from
 * <a href="http://maroo.cs.umass.edu/pub/web/getpdf.php?id=454">
 * <i>Hindi CLIR in Thirty Days</i> by
 * Leah S. Larkey, Margaret E. Connell, and Nasreen AbdulJaleel.
 * </a>:
 * <ul>
 *  <li>Internal Zero-width joiner and Zero-width non-joiners are removed
 *  <li>In addition to chandrabindu, NA+halant is normalized to anusvara
 * </ul>
 */
public class HindiNormalizer extends IndicFilter {

    public HindiNormalizer(final TokenStream input) {
        super(input);
    }

    private char handleNuktaReplacement(final char c) {
        switch (c) {
            case '\u0929':
                return '\u0928';
            case '\u0931':
                return '\u0930';
            case '\u0934':
                return '\u0933';
            case '\u0958':
                return '\u0915';
            case '\u0959':
                return '\u0916';
            case '\u095A':
                return '\u0917';
            case '\u095B':
                return '\u091C';
            case '\u095C':
                return '\u0921';
            case '\u095D':
                return '\u0922';
            case '\u095E':
                return '\u092B';
            case '\u095F':
                return '\u092F';
            default:
                return c;
        }
    }

    private char handleVowelDiacritics(final char c) {
        switch (c) {
            case '\u0940':
                return '\u093F';
            case '\u0942':
                return '\u0941';
            case '\u0944':
                return '\u0943';
            case '\u0963':
                return '\u0962';
            case '\u0945':
            case '\u0946':
            case '\u0948':
                return '\u0947';
            case '\u0949':
            case '\u094A':
            case '\u094C':
                return '\u094B';
            default:
                return c;
        }
    }

    private char handleChandraBindu(final char c) {
        if (c == '\u0901') {
            return '\u0902';
        }
        return c;
    }

    private char handleIndividualVowelsVariations(final char c) {
        switch (c) {
            case '\u0906':
            case '\u0972':
                return '\u0905';
            case '\u0908':
                return '\u0907';
            case '\u090A':
                return '\u0909';
            case '\u0960':
                return '\u090B';
            case '\u0961':
                return '\u090C';
            case '\u090D':
            case '\u090E':
            case '\u0910':
                return '\u090F';
            case '\u0911':
            case '\u0912':
            case '\u0914':
                return '\u0913';
            default:
                return c;
        }
    }

    private int handleDeletions(final char[] buffer, final int positionToDelete, final int bufferMaxLength) {
        switch (buffer[positionToDelete]) {
            case '\u094D':
                // dead n -> bindu [only if न not in start] else convert to न
                if (positionToDelete > 1 && buffer[positionToDelete - 1] == '\u0928') {
                    buffer[positionToDelete - 1] = '\u0902';
                }
                return delete(buffer, positionToDelete, bufferMaxLength);
            // nukta deletions
            case '\u093C':
                // zwj/zwnj -> delete
            case '\u200D':
            case '\u200C':
                return delete(buffer, positionToDelete, bufferMaxLength);
            default:
                return bufferMaxLength;
        }
    }

    /**
     * Normalize an input buffer of Hindi text
     *
     * @param buffer    input buffer
     * @param len       length of input buffer
     * @return length of input buffer after normalization
     */
    private int normalize(final char[] buffer, int len) {

        int i = 0;
        while(i < len) {
            final int newLength = handleDeletions(buffer, i, len);
            if (newLength < len) {
                i--;
            }
            len = newLength;
            buffer[i] = handleChandraBindu(buffer[i]);
            buffer[i] = handleNuktaReplacement(buffer[i]);
            buffer[i] = handleIndividualVowelsVariations(buffer[i]);
            buffer[i] = handleVowelDiacritics(buffer[i]);
            i++;
        }

        return len;
    }

    @Override
    public int filter(final char[] buffer, final int len) {
        return normalize(buffer, len);
    }
}

