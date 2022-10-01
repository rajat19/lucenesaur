package io.github.rajat19.filters;

import org.apache.lucene.analysis.TokenStream;

public class DecimalDigitFilter extends IndicFilter{

    private static final int DIGIT_CODE_POINT = 0x7F;

    public DecimalDigitFilter(final TokenStream input) {
        super(input);
    }

    private char getCharValue(final int codePoint) {
        return (char) ('0' + Character.getNumericValue(codePoint));
    }

    @Override
    public int filter(final char[] buffer, final int len) {
        int sourceIndex = 0;
        int destinationIndex = 0;

        while (sourceIndex < len) {
            final int codePoint = Character.codePointAt(buffer, sourceIndex, len);
            // handle supplementary code point, which requires two "char"
            final int step = Character.charCount(codePoint);
            // look for digits outside of basic latin
            if ((codePoint > DIGIT_CODE_POINT) && Character.isDigit(codePoint)) {
                // replace with equivalent basic latin digit
                buffer[destinationIndex] = getCharValue(codePoint);
                sourceIndex += step;
                destinationIndex++;
            } else {
                for (int i = 0; i < step; i++) {
                    buffer[destinationIndex] = buffer[sourceIndex];
                    sourceIndex++;
                    destinationIndex++;
                }
            }
        }
        return destinationIndex;
    }
}
