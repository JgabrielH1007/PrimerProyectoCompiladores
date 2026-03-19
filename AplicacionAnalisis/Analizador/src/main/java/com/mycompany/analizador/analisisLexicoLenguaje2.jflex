
package com.mycompany.analizador;

import java_cup.runtime.*;
import java.util.List;
import java.util.ArrayList;
import com.mycompany.analizador.ErrorLexico; 

%%
%public
%unicode
%cupsym symPKM
%class LexerPKM
%cup
%line
%column
%ignorecase  
%state DENTRO_CADENA 
%eofval{
    return new Symbol(symPKM.EOF, yyline + 1, yycolumn + 1, "EOF");
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

NUMERO = "-"? [0-9]+ ("." [0-9]+)?

COLORHEX = "#" [0-9a-fA-F]{6}
COLORRGB = "(" [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* ")"
COLORHSL = "<" [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* "," [ ]* {NUMERO} [ ]* ">"

FECHA_FORMATO = [0-9]{2} "/" [0-9]{2} "/" [0-9]{2,4}
HORA_FORMATO = [0-9]{2} ":" [0-9]{2}

COMENTARIO_NUMERAL = "#" [ \t]+ [^\r\n]* {FINALLINEA}? | "#" {FINALLINEA}?

SMILE   = "@[:" ")"+ "]" | "@[:smile:]"
SAD     = "@[:" "("+ "]" | "@[:sad:]"
SERIOUS = "@[:" "]"+ "]" | "@[:serious:]"
HEART   = "@[" "<"+ "3"+ "]" | "@[:heart:]"
STAR    = "@[:star:]" | "@[:star:" [0-9]+ ":]" | "@[:star-" [0-9]+ "-:]" 
CAT     = "@[:^^:]" | "@[:cat:]"

%%
<YYINITIAL> {

    "###"                     { return symbol(symPKM.METADATA_SEP, yytext()); }
    "Author:"                 { return symbol(symPKM.AUTHOR, yytext()); }
    "Fecha:"                  { return symbol(symPKM.FECHA, yytext()); }
    "Hora:"                   { return symbol(symPKM.HORA, yytext()); }
    "Description:"            { return symbol(symPKM.DESCRIPTION, yytext()); }
    "Total de Secciones:"     { return symbol(symPKM.TOTAL_SECCIONES, yytext()); }
    "Total de Preguntas:"     { return symbol(symPKM.TOTAL_PREGUNTAS, yytext()); }
    "Abiertas:"               { return symbol(symPKM.ABIERTAS, yytext()); }
    "Desplegables:"           { return symbol(symPKM.DESPLEGABLES, yytext()); }
    "Selección:" | "Seleccion:"  { return symbol(symPKM.SELECCION, yytext()); }
    "Múltiples:" | "Multiples:" { return symbol(symPKM.MULTIPLES, yytext()); }

    "style"                   { return symbol(symPKM.STYLE, yytext()); }
    "color"                   { return symbol(symPKM.COLOR, yytext()); }
    "background color"        { return symbol(symPKM.BACKGROUND_COLOR, yytext()); }
    "font family"             { return symbol(symPKM.FONT_FAMILY, yytext()); }
    "text size"               { return symbol(symPKM.TEXT_SIZE, yytext()); }
    "border"                  { return symbol(symPKM.BORDER, yytext()); }
    
    "section"                 { return symbol(symPKM.SECTION, yytext()); }
    "content"                 { return symbol(symPKM.CONTENT, yytext()); }
    
    "table"                   { return symbol(symPKM.TABLE, yytext()); }
    "line"                    { return symbol(symPKM.LINE, yytext()); }
    "element"                 { return symbol(symPKM.ELEMENT, yytext()); }
    
    "open"                    { return symbol(symPKM.OPEN, yytext()); }
    "drop"                    { return symbol(symPKM.DROP, yytext()); }
    "select"                  { return symbol(symPKM.SELECT, yytext()); }
    "multiple"                { return symbol(symPKM.MULTIPLE, yytext()); }

    "MONO"                    { return symbol(symPKM.MONO, yytext()); }
    "SANS_SERIF"              { return symbol(symPKM.SANS_SERIF, yytext()); }
    "CURSIVE"                 { return symbol(symPKM.CURSIVE, yytext()); }
    "DOTTED"                  { return symbol(symPKM.DOTTED, yytext()); }
    "DOUBLE"                  { return symbol(symPKM.DOUBLE_STYLE, yytext()); }
    "VERTICAL"                { return symbol(symPKM.VERTICAL, yytext()); }
    "HORIZONTAL"              { return symbol(symPKM.HORIZONTAL, yytext()); }

    "<"                       { return symbol(symPKM.MENOR, yytext()); }
    ">"                       { return symbol(symPKM.MAYOR, yytext()); }
    "</"                      { return symbol(symPKM.CIERRE_ETIQUETA, yytext()); }
    "/>"                      { return symbol(symPKM.ETIQUETA_AUTOCIERRE, yytext()); }
    "="                       { return symbol(symPKM.IGUAL, yytext()); }
    ","                       { return symbol(symPKM.COMA, yytext()); }
    "{"                       { return symbol(symPKM.LLA_IZQ, yytext()); }
    "}"                       { return symbol(symPKM.LLA_DER, yytext()); }

    "\""                      { yybegin(DENTRO_CADENA); return symbol(symPKM.COMILLA, yytext()); }

    {FECHA_FORMATO}           { return symbol(symPKM.VALOR_FECHA, yytext()); }
    {HORA_FORMATO}            { return symbol(symPKM.VALOR_HORA, yytext()); }
    {NUMERO}                  { return symbol(symPKM.NUMERO, Double.parseDouble(yytext())); }
    {COLORHEX}                { return symbol(symPKM.COLORHEX, yytext()); }
    {COLORRGB}                { return symbol(symPKM.COLORRGB, yytext()); }
    {COLORHSL}                { return symbol(symPKM.COLORHSL, yytext()); }

    [a-zA-Z_][a-zA-Z0-9_]* { return symbol(symPKM.IDENTIFICADOR, yytext()); }

    {ESPACIO}                 { /* Ignorar */ }
    {COMENTARIO_NUMERAL}      { /* Ignorar */ }

    [^]                       {
                                errores.add(new ErrorLexico("Símbolo no reconocido en PKM: " + yytext(), yyline, yycolumn));
                              }
}

<DENTRO_CADENA> {
    {SMILE}              { return symbol(symPKM.EMOJI, yytext()); }
    {SAD}                { return symbol(symPKM.EMOJI, yytext()); }
    {SERIOUS}            { return symbol(symPKM.EMOJI, yytext()); }
    {HEART}              { return symbol(symPKM.EMOJI, yytext()); }
    {STAR}               { return symbol(symPKM.EMOJI, yytext()); }
    {CAT}                { return symbol(symPKM.EMOJI, yytext()); }

    [^\"@]+              { return symbol(symPKM.TEXTO_PLANO, yytext()); }
    "@"                  { return symbol(symPKM.TEXTO_PLANO, yytext()); }
    "\""                 { yybegin(YYINITIAL); return symbol(symPKM.COMILLA, yytext()); }
}