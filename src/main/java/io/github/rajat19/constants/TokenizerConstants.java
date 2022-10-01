package io.github.rajat19.constants;

public interface TokenizerConstants {
    int ALPHANUM = 0;
    int APOSTROPHE = 1;
    int ACRONYM = 2;
    int COMPANY = 3;
    int EMAIL = 4;
    int NUM = 5;
    int CJ_WORD = 6;
    int IRREGULAR_WORD = 7;
    int TRAILING_AT = 8;
    int INDIC_WORD = 9;
    int HINDI_WORD = 10;

    String[] TOKEN_IMAGE = {
            "<ALPHANUM>",
            "<APOSTROPHE>",
            "<ACRONYM>",
            "<IRREGULAR_WORD>",
            "<EMAIL>",
            "<NUM>",
            "<COMPANY>",
            "<TRAILING_AT>",
            "<CJ_WORD>",
            "<INDIC_WORD>",
            "<HINDI_WORD>",
    };
}
