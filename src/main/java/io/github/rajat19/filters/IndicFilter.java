package io.github.rajat19.filters;

import lombok.NonNull;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

public abstract class IndicFilter extends TokenFilter {
    protected IndicFilter(final TokenStream input) {
        super(input);
    }

    /**
     * Delete a character in-place and returns new length of buffer
     *
     * @param buffer Input Buffer
     * @param pos    Position of character to delete
     * @param len    length of input buffer
     * @return length of input buffer after deletion
     */
    public static int delete(final char[] buffer, final int pos, final int len) {
        assert pos < len;
        if (pos < len - 1) { // don't arraycopy if asked to delete last character
            System.arraycopy(buffer, pos + 1, buffer, pos, len - pos - 1);
        }
        return len - 1;
    }

    protected abstract int filter(char[] buffer, int len);

    @Override
    public Token next(final @NonNull Token reusableToken) throws IOException {
        final Token nextToken = input.next(reusableToken);
        if (nextToken != null) {
            nextToken.setTermLength(filter(nextToken.termBuffer(), nextToken.termLength()));
            return nextToken;
        }
        return null;
    }
}
