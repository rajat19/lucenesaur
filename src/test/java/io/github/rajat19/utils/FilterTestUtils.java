package io.github.rajat19.utils;

import io.github.rajat19.filters.IndicFilter;
import io.github.rajat19.tokenizers.IndicTokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterTestUtils {

    public static void assertFilters(final Function<? super TokenStream, ? extends IndicFilter> filterFunction,
                                     final String input,
                                     final String output,
                                     final boolean checkCharactersRemoved
    ) throws IOException {
        final TokenStream originalTokenStream = new IndicTokenizer(new StringReader(input));
        TokenStream filteredTokenStream = new IndicTokenizer(new StringReader(input));
        filteredTokenStream = filterFunction.apply(filteredTokenStream);
        try {
            final Token filteredToken = filteredTokenStream.next(new Token());
            final Token originalToken = originalTokenStream.next(new Token());
            assertNotNull(filteredToken);
            assertEquals(output, filteredToken.term());
            final int originalTermLength = originalToken.termLength();
            final int filteredTermLength = filteredToken.termLength();
            assertTrue(checkCharactersRemoved ? originalTermLength > filteredTermLength : originalTermLength == filteredTermLength);
        } finally {
            filteredTokenStream.close();
            originalTokenStream.close();
        }
    }

    public static void assertReplacesChar(final Function<? super TokenStream, ? extends IndicFilter> filterFunction, final String input, final String output) throws IOException {
        assertFilters(filterFunction, input, output, false);
    }

    public static void assertRemovesChar(final Function<? super TokenStream, ? extends IndicFilter> filterFunction, final String input, final String output) throws IOException{
        assertFilters(filterFunction, input, output, true);
    }
}
