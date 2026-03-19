package com.mycompany.analizador;
import java_cup.runtime.*;
import java.util.List;
import java.util.ArrayList;

%%
%public
%unicode
%class LexerFormulario
%cup
%line
%column
%state DENTRO_CADENA
%eofval{
    return new Symbol(sym.EOF, yyline + 1, yycolumn + 1, "EOF");
%eofval}

%{
    private List<ErrorLexico> errores = new ArrayList<>();

    public List<ErrorLexico> getErrores() {
        return errores;
    }

    private Symbol symbol(int tipo) {
        return new Symbol(tipo, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int tipo, Object valor) {
        return new Symbol(tipo, yyline + 1, yycolumn + 1, valor);
    }
%}

FINALLINEA = \r|\n|\r\n
ESPACIO = {FINALLINEA}|[ \t\f]

COMETARIOUNALINEA = "$" [^\r\n]* {FINALLINEA}?
MULTILINEACOMENTARIO = "/*" [^*]* ~"*/" | "/*" "*"+ "/"

ID = [a-zA-Z_][a-zA-Z0-9_]*
ENTERO = [0-9]+ 
NUMERO = [0-9]+ ("." [0-9]+)?

COLORHEX = "#" [0-9a-fA-F]{6}
COLORRGB = "(" [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* ")"
COLORHSL = "<" [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* ">"

SMILE   = "@[:" ")"+ "]" | "@[:smile:]"
SAD     = "@[:" "("+ "]" | "@[:sad:]"
SERIOUS = "@[:" "]"+ "]" | "@[:serious:]"
HEART   = "@[" "<"+ "3"+ "]" | "@[:heart:]"
STAR    = "@[:star:]" | "@[:star:" {ENTERO} ":]" | "@[:star-" {ENTERO} "-:]" 
CAT     = "@[:^^:]" | "@[:cat:]"

%%
<YYINITIAL> {
// palabras reservadas: ESTRUCTURAS
"SECTION"             { return symbol(sym.SECTION, yytext()); }
"TABLE"               { return symbol(sym.TABLE, yytext()); }
"TEXT"                { return symbol(sym.TEXT, yytext()); }
// palabras reservadas: PREGUNTAS
"OPEN_QUESTION"       { return symbol(sym.OPEN_QUESTION, yytext()); }
"DROP_QUESTION"       { return symbol(sym.DROP_QUESTION, yytext()); }
"SELECT_QUESTION"     { return symbol(sym.SELECT_QUESTION, yytext()); }
"MULTIPLE_QUESTION"   { return symbol(sym.MULTIPLE_QUESTION, yytext()); }
// palabras reservadas: ATRIBUTOS
"width"               { return symbol(sym.WIDTH, yytext()); }
"height"              { return symbol(sym.HEIGHT, yytext()); }
"pointX"              { return symbol(sym.POINTX, yytext()); }
"pointY"              { return symbol(sym.POINTY, yytext()); }
"orientation"         { return symbol(sym.ORIENTATION, yytext()); }
"elements"            { return symbol(sym.ELEMENTS, yytext()); }
"styles"              { return symbol(sym.STYLES, yytext()); }
"label"               { return symbol(sym.LABEL, yytext()); }
"content"             { return symbol(sym.CONTENT, yytext()); }
"options"             { return symbol(sym.OPTIONS, yytext()); }
"correct"             { return symbol(sym.CORRECT, yytext()); }
// palabras reservadas: VALORES Y CONSTANTES
"VERTICAL"            { return symbol(sym.VERTICAL, yytext()); }
"HORIZONTAL"          { return symbol(sym.HORIZONTAL, yytext()); }
"MONO"                { return symbol(sym.MONO, yytext()); }
"SANS_SERIF"          { return symbol(sym.SANS_SERIF, yytext()); }
"CURSIVE"             { return symbol(sym.CURSIVE, yytext()); }
"LINE"                { return symbol(sym.LINE, yytext()); }
"DOTTED"              { return symbol(sym.DOTTED, yytext()); }
"DOUBLE"              { return symbol(sym.DOUBLE, yytext()); }
// Colores base
"RED"                 { return symbol(sym.RED, yytext()); }
"BLUE"                { return symbol(sym.BLUE, yytext()); }
"GREEN"               { return symbol(sym.GREEN, yytext()); }
"PURPLE"              { return symbol(sym.PURPLE, yytext()); }
"SKY"                 { return symbol(sym.SKY, yytext()); }
"YELLOW"              { return symbol(sym.YELLOW, yytext()); }
"BLACK"               { return symbol(sym.BLACK, yytext()); }
"WHITE"               { return symbol(sym.WHITE, yytext()); }
// palabras reservadas: Variables y Funciones 
"number"              { return symbol(sym.TYPE_NUMBER, yytext()); }
"string"              { return symbol(sym.TYPE_STRING, yytext()); }
"special"             { return symbol(sym.TYPE_SPECIAL, yytext()); }
"who_is_that_pokemon" { return symbol(sym.WHO_IS_THAT_POKEMON, yytext()); }
"draw"                { return symbol(sym.DRAW, yytext()); }
// palabras reservadas: control flujo
"IF"                  { return symbol(sym.IF, yytext()); }
"ELSE"                { return symbol(sym.ELSE, yytext()); }
"WHILE"               { return symbol(sym.WHILE, yytext()); }
"DO"                  { return symbol(sym.DO, yytext()); }
"FOR"                 { return symbol(sym.FOR, yytext()); }
"in"                  { return symbol(sym.IN, yytext()); }
// simbolos y operadores
"+"                   { return symbol(sym.MAS, yytext()); }
"-"                   { return symbol(sym.MENOS, yytext()); }
"*"                   { return symbol(sym.POR, yytext()); }
"/"                   { return symbol(sym.DIV, yytext()); }
"^"                   { return symbol(sym.POTENCIA, yytext()); }
"%"                   { return symbol(sym.MODULO, yytext()); }
">"                   { return symbol(sym.MAYOR, yytext()); }
"<"                   { return symbol(sym.MENOR, yytext()); }
">="                  { return symbol(sym.MAYOR_IGUAL, yytext()); }
"<="                  { return symbol(sym.MENOR_IGUAL, yytext()); }
"=="                  { return symbol(sym.IGUALDAD, yytext()); }
"!!"                  { return symbol(sym.DIFERENTE, yytext()); }
"!"                   { return symbol(sym.NOT, yytext()); }
"&&"                  { return symbol(sym.AND, yytext()); }
"||"                  { return symbol(sym.OR, yytext()); }
"="                   { return symbol(sym.ASIGNACION, yytext()); }
"?"                   { return symbol(sym.COMODIN, yytext()); }
// puntuacion y agrupacion
"("                   { return symbol(sym.PAR_IZQ, yytext()); }
")"                   { return symbol(sym.PAR_DER, yytext()); }
"["                   { return symbol(sym.COR_IZQ, yytext()); }
"]"                   { return symbol(sym.COR_DER, yytext()); }
"{"                   { return symbol(sym.LLA_IZQ, yytext()); }
"}"                   { return symbol(sym.LLA_DER, yytext()); }
","                   { return symbol(sym.COMA, yytext()); }
":"                   { return symbol(sym.DOS_PUNTOS, yytext()); }
";"                   { return symbol(sym.PUNTO_COMA, yytext()); }
"."                   { return symbol(sym.PUNTO, yytext()); }

"\""                  { yybegin(DENTRO_CADENA); return symbol(sym.COMILLA, yytext()); }

{ID}                 { return symbol(sym.IDENTIFICADOR, yytext()); }
{NUMERO}             { return symbol(sym.NUMBER, Double.parseDouble(yytext()));}
{COLORHEX}           { return symbol(sym.COLOR_HEX, yytext()); }
{COLORRGB}           { return symbol(sym.COLOR_RGB, yytext()); }
{COLORHSL}           { return symbol(sym.COLOR_HSL, yytext()); }
{ESPACIO}            { }
{COMETARIOUNALINEA}  { }
{MULTILINEACOMENTARIO} { }
[^]                  {
                        errores.add(new ErrorLexico("Símbolo no reconocido: " + yytext(), yyline, yycolumn));
                     }
}

<DENTRO_CADENA> {
    {SMILE}              { return symbol(sym.EMOJI, yytext()); }
    {SAD}                { return symbol(sym.EMOJI, yytext()); }
    {SERIOUS}            { return symbol(sym.EMOJI, yytext()); }
    {HEART}              { return symbol(sym.EMOJI, yytext()); }
    {STAR}               { return symbol(sym.EMOJI, yytext()); }
    {CAT}                { return symbol(sym.EMOJI, yytext()); }

    [^\"@]+              { return symbol(sym.TEXTO_PLANO, yytext()); }

    "@"                  { return symbol(sym.TEXTO_PLANO, yytext()); }

    "\""                 { yybegin(YYINITIAL); return symbol(sym.COMILLA, yytext()); }
}