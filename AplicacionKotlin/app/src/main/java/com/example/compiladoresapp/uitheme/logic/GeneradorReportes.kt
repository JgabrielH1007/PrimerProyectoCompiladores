package com.example.aplicacioncompi1.uitheme.logic

import com.example.compiladoresapp.uitheme.logic.Parser
import com.example.compiladoresapp.uitheme.logic.ParserPKM


class GeneradorReportes(){


    fun hayErroresPkm(parserPKM: ParserPKM): Boolean{
        var lexer = parserPKM.lexer
        if (lexer.errores.isNotEmpty()|| parserPKM.erroresSintacticos.isNotEmpty()){
            return true
        }
        return false
    }

    fun hayErroresForm(parserFormulario: Parser): Boolean{
        var lexer = parserFormulario.lexer
        if(lexer.errores.isNotEmpty()||parserFormulario.erroresSintacticos.isNotEmpty()){
            return true
        }
        return false
    }

    fun generarReportePkm(parserPKM: ParserPKM):String{
        val erroresSintacticos = parserPKM.erroresSintacticos
        val lexer = parserPKM.lexer
        val erroresLexicos = lexer.errores

        if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty()) {
            return "<html><body style='text-align:center; margin-top:50px;'><h2>✅ Análisis Exitoso</h2><p>El archivo PKM no contiene errores.</p></body></html>"
        }

        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Reporte de Errores PKM</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background-color: #f9f9f9;}
                        h1 { color: #d32f2f; text-align: center; }
                        h2 { color: #333; }
                        table { border-collapse: collapse; width: 100%; margin-bottom: 30px; background-color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); }
                        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                        th { background-color: #3f51b5; color: white; }
                        tr:nth-child(even){background-color: #f2f2f2;}
                        .error-tipo { font-weight: bold; color: #d32f2f; }
                    </style>
                </head>
                <body>
                    <h1>Reporte de Errores - Archivo PKM</h1>
            """.trimIndent())

            if (erroresLexicos.isNotEmpty()) {
                append("<h2>Errores Léxicos</h2>")
                append("<table>")
                append("<tr><th>#</th><th>Tipo</th><th>Línea</th><th>Columna</th><th>Descripción</th></tr>")

                erroresLexicos.forEachIndexed { index, error ->
                    append("<tr>")
                    append("<td>${index + 1}</td>")
                    append("<td class='error-tipo'>Léxico</td>")
                    append("<td>${error.linea}</td>")
                    append("<td>${error.columna}</td>")
                    append("<td>Símbolo no existe en el lenguaje}</td>")
                    append("</tr>")
                }
                append("</table>")
            }

            if (erroresSintacticos.isNotEmpty()) {
                append("<h2>Errores Sintácticos</h2>")
                append("<table>")
                append("<tr><th>#</th><th>Lexema</th><th>Línea</th><th>Columna</th><th>Descripción</th></tr>")

                erroresSintacticos.forEachIndexed { index, error ->
                    append("<tr>")
                    append("<td>${index + 1}</td>")
                    append("<td><b>${error.lexema ?: "EOF"}</b></td>")
                    append("<td>${error.linea}</td>")
                    append("<td>${error.columna}</td>")
                    append("<td>${error.descripcion}</td>")
                    append("</tr>")
                }
                append("</table>")
            }

            append("""
                </body>
                </html>
            """.trimIndent())
        }
    }

    fun generarReportesFormulario(parserFormulario: Parser):String{
        val erroresSintacticos = parserFormulario.erroresSintacticos
        val lexer = parserFormulario.lexer
        val erroresLexicos = lexer.errores

        if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty()) {
            return "<html><body style='text-align:center; margin-top:50px;'><h2>✅ Análisis Exitoso</h2><p>El archivo FORM no contiene errores.</p></body></html>"
        }

        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Reporte de Errores FORM</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background-color: #f9f9f9;}
                        h1 { color: #d32f2f; text-align: center; }
                        h2 { color: #333; }
                        table { border-collapse: collapse; width: 100%; margin-bottom: 30px; background-color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); }
                        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                        th { background-color: #3f51b5; color: white; }
                        tr:nth-child(even){background-color: #f2f2f2;}
                        .error-tipo { font-weight: bold; color: #d32f2f; }
                    </style>
                </head>
                <body>
                    <h1>Reporte de Errores - Archivo FORM</h1>
            """.trimIndent())

            if (erroresLexicos.isNotEmpty()) {
                append("<h2>Errores Léxicos</h2>")
                append("<table>")
                append("<tr><th>#</th><th>Tipo</th><th>Línea</th><th>Columna</th><th>Descripción</th></tr>")

                erroresLexicos.forEachIndexed { index, error ->
                    append("<tr>")
                    append("<td>${index + 1}</td>")
                    append("<td class='error-tipo'>Léxico</td>")
                    append("<td>${error.linea}</td>")
                    append("<td>${error.columna}</td>")
                    append("<td>Símbolo no existe en el lenguaje}</td>")
                    append("</tr>")
                }
                append("</table>")
            }

            if (erroresSintacticos.isNotEmpty()) {
                append("<h2>Errores Sintácticos</h2>")
                append("<table>")
                append("<tr><th>#</th><th>Lexema</th><th>Línea</th><th>Columna</th><th>Descripción</th></tr>")

                erroresSintacticos.forEachIndexed { index, error ->
                    append("<tr>")
                    append("<td>${index + 1}</td>")
                    append("<td><b>${error.lexema ?: "EOF"}</b></td>")
                    append("<td>${error.linea}</td>")
                    append("<td>${error.columna}</td>")
                    append("<td>${error.descripcion}</td>")
                    append("</tr>")
                }
                append("</table>")
            }

            append("""
                </body>
                </html>
            """.trimIndent())
        }
    }
}