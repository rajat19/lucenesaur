package io.github.rajat19.tokenizers;

/**
 * Adapted from Lucene 2.4.1's StandardTokenizerImpl.jflex.
 *
 * The following changes have been made:
 *
 * - added IRREGULAR_WORD definitions for things like "C++", "C#", "A+", etc
 * - removed the special HOST pattern for hostnames
 * - changed the COMPANY pattern to begin with "{LETTER}*" instead of "{ALPHA}"
 *   so we treat company names like "@home" as one token without losing the "@"
 * - removed the ACRONYM_DEP pattern
 * - Additional support for Indian languages
 */

import org.apache.lucene.analysis.Token;
import io.github.rajat19.constants.TokenizerConstants;

%%

%class IndicTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char

%{

public static final int ALPHANUM          = TokenizerConstants.ALPHANUM;
public static final int APOSTROPHE        = TokenizerConstants.APOSTROPHE;
public static final int ACRONYM           = TokenizerConstants.ACRONYM;
public static final int COMPANY           = TokenizerConstants.COMPANY;
public static final int EMAIL             = TokenizerConstants.EMAIL;
public static final int NUM               = TokenizerConstants.NUM;
public static final int IRREGULAR_WORD    = TokenizerConstants.IRREGULAR_WORD;
public static final int TRAILING_AT       = TokenizerConstants.TRAILING_AT;
public static final int INDIC_WORD        = TokenizerConstants.INDIC_WORD;
public static final int HINDI_WORD        = TokenizerConstants.HINDI_WORD;
public static final int CJ_WORD           = TokenizerConstants.CJ_WORD;
public static final String [] TOKEN_TYPES = TokenizerConstants.TOKEN_IMAGE;

public final int yychar()
{
    return (int) yychar;
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}
%}

// skipped https://en.wikipedia.org/wiki/Danda [period]
HINDI      = [\u0900-\u0963\u0966-\u097F]
/**
 * Supports other major indian language unicodes
 * Bengali, Punjabi, Gujarati, Oriya, Tamil, Telugu, Kannada, Malayalam
*/
INDIC      = [\u0980-\u09FF\u0A00-\u0A7F\u0A80-\u0AFF\u0B00-\u0B7F\u0B80-\u0BFF\u0C00-\u0C7F\u0C80-\u0CFF\u0D00-\u0D7F]

HINDI_WORD = ({HINDI}|[:digit:])+
INDIC_WORD = ({INDIC}|[:digit:])+

CJ_WORD = ({CJ}|[:digit:])+

// basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|[:digit:])+ | IRREGULAR_WORD

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} (("'"|"\u2019"|"\u2018"|"\u00b4"|"\u0060") {ALPHA})+

/*
 NOTE: Do not insert literal funky Unicode characters in this file.  Instead,
 use the \u0000 notation.  Otherwise, it causes problems on some platforms.

 Here are some of the Unicode symbols used below (see http://fileformat.info)
 \uFF0B = FULLWIDTH PLUS SIGN
 \uFF03 = FULLWIDTH NUMBER SIGN
 \uFF20 = FULLWIDTH COMMERCIAL AT
 \uFF06 = FULLWIDTH AMPERSAND
*/

IRREGULAR_WORD = ({C_PLUS_PLUS}|{C_SHARP}|{F_SHARP}|{A_PLUS}|{STAR_NIX})
C_PLUS_PLUS = ("C"|"c") ("++"|"\uFF0B\uFF0B")
C_SHARP = ("C"|"c") ("#"|"\uFF03")
F_SHARP = ("F"|"f") ("#"|"\uFF03")
A_PLUS =  ("A"|"a") ("+"|"\uFF0B")
STAR_NIX = "*" ("nix"|"NIX"|"Nix"|"NiX")

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
ACRONYM    =  {LETTER} "." ({LETTER} ".")+

// company names like AT&T and Excite@Home.
COMPANY    =  {LETTER}* ("&"|"@"|"\uFF06"|"\uFF20") {ALPHA}

// tokens ending in an "@" sign (common in Spain)
TRAILING_AT = {ALPHA} "@"

// email addresses
EMAIL      =  {ALPHANUM} (("."|"-"|"_") {ALPHANUM})* "@" {ALPHANUM} (("."|"-") {ALPHANUM})+

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation
P	         = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*

ALPHA      = ({LETTER})+

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:]|{CJ}|{INDIC}|{HINDI})

// Chinese and Japanese (but NOT Korean, which is included in [:letter:])
CJ         = [\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

WHITESPACE = \r\n | [ \r\n\t\f]

%%
{IRREGULAR_WORD}                                               { return IRREGULAR_WORD;}
{HINDI_WORD}                                                   { return HINDI_WORD; }
{INDIC_WORD}                                                   { return INDIC_WORD; }
{CJ_WORD}                                                      { return CJ_WORD;}
{TRAILING_AT}                                                  { return TRAILING_AT; }
{ALPHANUM}                                                     { return ALPHANUM; }
{APOSTROPHE}                                                   { return APOSTROPHE; }
{ACRONYM}                                                      { return ACRONYM; }
{COMPANY}                                                      { return COMPANY; }
{EMAIL}                                                        { return EMAIL; }
{NUM}                                                          { return NUM; }

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
