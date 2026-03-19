/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.analizador;

/**
 *
 * @author gabrielh
 */
public class ErrorLexico {
    public final String lexema;
    public final int linea;
    public final int columna;
    
    public ErrorLexico(String lexema, int linea, int columna){
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }
    
    @Override
    public String toString() {
        return "Error léxico: '" + lexema + 
               "' en línea " + linea + 
               ", columna " + columna;
    }
}
