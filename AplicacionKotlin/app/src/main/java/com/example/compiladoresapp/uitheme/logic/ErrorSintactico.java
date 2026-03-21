/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.compiladoresapp.uitheme.logic;

/**
 *
 * @author gabrielh
 */
public class ErrorSintactico {
    private String lexema;
    private int linea;
    private int columna;
    private String descripcion;

    public ErrorSintactico(String lexema, int linea, int columna, String descripcion) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.descripcion = descripcion;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getDescripcion() {
        return descripcion;
    }
    
    
}
