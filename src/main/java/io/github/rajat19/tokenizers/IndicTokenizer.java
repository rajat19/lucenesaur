package io.github.rajat19.tokenizers;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.io.Reader;

public class IndicTokenizer extends Tokenizer {
    private final IndicTokenizerImpl scanner;

    /** Set the max allowed token length.  Any token longer than this is skipped. */
    private static final int MAX_TOKEN_LENGTH = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

    public IndicTokenizer(final Reader input) {
        this.input = input;
        this.scanner = new IndicTokenizerImpl(input);
    }

    @Override
    public Token next(final Token reusableToken) throws IOException {
        assert reusableToken != null;
        int posIncr = 1;

        while(true) {
            final int tokenType = scanner.getNextToken();

            if (tokenType == IndicTokenizerImpl.YYEOF) {
                return null;
            }

            if (scanner.yylength() <= MAX_TOKEN_LENGTH) {
                reusableToken.clear();
                reusableToken.setPositionIncrement(posIncr);
                scanner.getText(reusableToken);
                final int start = scanner.yychar();
                reusableToken.setStartOffset(start);
                reusableToken.setEndOffset(start + reusableToken.termLength());
                reusableToken.setType(IndicTokenizerImpl.TOKEN_TYPES[tokenType]);

                return reusableToken;
            } else {
                // When we skip a too-long term, we still increment the
                // position increment
                posIncr++;
            }
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        scanner.yyreset(input);
    }

    @Override
    public void reset(final Reader reader) throws IOException {
        input = reader;
        reset();
    }
}
