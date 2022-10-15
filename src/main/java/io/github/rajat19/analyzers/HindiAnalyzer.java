package io.github.rajat19.analyzers;

import io.github.rajat19.filters.HindiNormalizer;
import org.apache.lucene.analysis.TokenStream;

public class HindiAnalyzer extends IndicAnalyzer{
    @Override
    public TokenStream getLanguageSpecificFilter(final TokenStream input) {
        return new HindiNormalizer(input);
    }
}
