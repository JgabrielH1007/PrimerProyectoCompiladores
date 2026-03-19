/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.analizador;

import java.io.StringReader;
import java.util.List;

/**
 *
 * @author gabrielh
 */


public class Analizador {

    public static void main(String[] args) {
        
        // 1. Creamos un código de prueba con la sintaxis del Lenguaje 1
        String codigoPrueba = 
            "###\n" +
            "###\n" +
            "Author: \"Ingeniero Estrella\"\n" +
            "Fecha: 18/03/2026\n" +
            "Hora: 19:21\n" +
            "Description: \"Examen final muy complejo con tablas y anidamientos @[:serious:]\"\n" +
            "Total de Secciones: 2\n" +
            "Total de Preguntas: 4\n" +
            "Abiertas: 1\n" +
            "Desplegables: 1\n" +
            "Selección: 1\n" +
            "Múltiples: 1\n" +
            "\n" +
            "# Probando estilos globales y el bug del HEX resuelto\n" +
            "<style>\n" +
            "    <background color = #1A2B3C />\n" +
            "    <text size = 18>\n" +
            "    <border , 3 , DOUBLE , color = (255, 255, 0) />\n" +
            "</style>\n" +
            "\n" +
            "<section = 800, 600, 0, 0, HORIZONTAL>\n" +
            "    <style>\n" +
            "        <color = <120, 100, 50> />\n" +
            "    </style>\n" +
            "    <content>\n" +
            "        # Pregunta abierta normal\n" +
            "        <open = 1, 10, \"¿Qué opinas del curso? @[:smile:]\" />\n" +
            "    </content>\n" +
            "</section>\n" +
            "\n" +
            "<section = 800, 600, 0, 600, VERTICAL>\n" +
            "    <content>\n" +
            "        # Una tabla compleja con varios elementos\n" +
            "        <table>\n" +
            "            <content>\n" +
            "                <line>\n" +
            "                    <element>\n" +
            "                        # Dropdown con estilos propios\n" +
            "                        <drop = 2, 5, \"Selecciona la fase\", {\"Léxico\", \"Sintáctico\", \"Semántico\"}, 2>\n" +
            "                            <style>\n" +
            "                                <font family = CURSIVE />\n" +
            "                            </style>\n" +
            "                        </drop>\n" +
            "                    </element>\n" +
            "                    <element>\n" +
            "                        # Select normal\n" +
            "                        <select = 3, 5, \"¿Aprobaremos?\", {\"Sí\", \"Definitivamente\"}, 1 />\n" +
            "                    </element>\n" +
            "                </line>\n" +
            "                <line>\n" +
            "                    <element>\n" +
            "                        # Pregunta múltiple SIN respuestas correctas guardadas (lista vacía)\n" +
            "                        <multiple = 4, 10, \"Marca tus lenguajes favoritos @[:heart:]\", {\"Java\", \"Kotlin\", \"C++\"}, {} />\n" +
            "                    </element>\n" +
            "                </line>\n" +
            "            </content>\n" +
            "        </table>\n" +
            "    </content>\n" +
            "</section>\n";

        System.out.println("Iniciando análisis del código...");
        System.out.println("-------------------------------------------------");

        // 2. Inicializamos el Lexer pasándole el texto de prueba
        LexerPKM lexer = new LexerPKM(new StringReader(codigoPrueba));
        
        // 3. Inicializamos el Parser pasándole el Lexer
        ParserPKM sintactico = new ParserPKM(lexer);

        try {
            // 4. Ejecutamos el análisis
            sintactico.parse();
            
            // 5. Verificamos si hubo errores léxicos o sintácticos
            List<ErrorLexico> erroresLexicos = lexer.getErrores();
            List<ErrorSintactico> erroresSintacticos = sintactico.getErroresSintacticos();

            if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty()) {
                System.out.println("✅ ¡Análisis completado con ÉXITO!");
                System.out.println("El código no tiene errores léxicos ni sintácticos.");
            } else {
                System.out.println("❌ Se encontraron errores en el código:");
                
                // Imprimir errores léxicos
                for (ErrorLexico error : erroresLexicos) {
                    System.out.println("   - Léxico: " + error.toString());
                }
                
                // Imprimir errores sintácticos
                for (ErrorSintactico error : erroresSintacticos) {
                    System.out.println("   - Sintáctico: Error en '" + error.getLexema() + "' | Línea: " + error.getLinea() + " | Columna: " + error.getColumna() + " -> " + error.getDescripcion());
                }
            }

        } catch (Exception e) {
            System.err.println("💥 Ocurrió un error fatal durante el análisis:");
            e.printStackTrace();
        }
    }
}