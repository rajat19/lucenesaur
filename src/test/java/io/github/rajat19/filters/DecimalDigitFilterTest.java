package io.github.rajat19.filters;

import org.apache.lucene.analysis.TokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

import static io.github.rajat19.utils.FilterTestUtils.assertReplacesChar;

public class DecimalDigitFilterTest {
    private final Function<? super TokenStream, ? extends IndicFilter> filterFunction = DecimalDigitFilter::new;

    @Test
    void testHindiDigits() throws IOException {
        assertReplacesChar(filterFunction, "०१२३४५६७८९", "0123456789");
    }

    @Test
    void testBengaliDigits() throws Exception {
        assertReplacesChar(filterFunction, "০১২৩৪৫৬৭৮৯", "0123456789");
    }

    @Test
    void testGurmukhiDigits() throws Exception {
        assertReplacesChar(filterFunction, "੦੧੨੩੪੫੬੭੮੯", "0123456789");
    }

    @Test
    void testGujaratiDigits() throws Exception {
        assertReplacesChar(filterFunction, "૦૧૨૩૪૫૬૭૮૯", "0123456789");
    }

    @Test
    void testOdiaDigits() throws Exception {
        assertReplacesChar(filterFunction, "୦୧୨୩୪୫୬୭୮୯", "0123456789");
    }

    @Test
    void testTamilDigits() throws Exception {
        assertReplacesChar(filterFunction, "௦௧௨௩௪௫௬௭௮௯", "0123456789");
    }

    @Test
    void testTeluguDigits() throws Exception {
        assertReplacesChar(filterFunction, "౦౧౨౩౪౫౬౭౮౯", "0123456789");
    }

    @Test
    void testKannadaDigits() throws Exception {
        assertReplacesChar(filterFunction, "೦೧೨೩೪೫೬೭೮೯", "0123456789");
    }

    @Test
    void testMalayalamDigits() throws Exception {
        assertReplacesChar(filterFunction, "൦൧൨൩൪൫൬൭൮൯", "0123456789");
    }
}
