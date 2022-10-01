package io.github.rajat19.analyzers;

import io.github.rajat19.filters.DecimalDigitFilter;
import io.github.rajat19.filters.IndicNormalizer;
import io.github.rajat19.tokenizers.IndicTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class IndicAnalyzer extends Analyzer {
    private static final Set<String> NO_STOPWORD_FILTER_FIELDS = Collections.emptySet();
    private static final Set<String> NO_LOWERCASE_FILTER_FIELDS = Collections.emptySet();
    private final Set<String> stopSet;

    public IndicAnalyzer(final Set<String> stopSet) {
        this.stopSet = stopSet;
    }

    public IndicAnalyzer() {
        this(new HashSet<>());
    }

    @Override
    public TokenStream tokenStream(final String fieldName, final Reader reader) {
        TokenStream result = new IndicTokenizer(reader);
        result = new StandardFilter(result);
        if (shouldLowerCase(fieldName)) {
            result = new LowerCaseFilter(result);
        }
        result = new DecimalDigitFilter(result);
        result = new IndicNormalizer(result);
        result = getLanguageSpecificFilter(result);
        if (shouldFilterStopwords(fieldName)) {
            result = new StopFilter(result, stopSet);
        }
        return result;
    }

    public abstract TokenStream getLanguageSpecificFilter(TokenStream input);

    public static boolean shouldLowerCase(final String fieldName) {
        return !NO_LOWERCASE_FILTER_FIELDS.contains(fieldName);
    }

    private boolean shouldFilterStopwords(final String fieldName) {
        return !NO_STOPWORD_FILTER_FIELDS.contains(fieldName);
    }
}
