package com.example.compiladoresapp.uitheme.logic

import com.example.aplicacioncompi1.uitheme.logic.GeneradorReportes
import java.io.StringReader

class Analizador(private val input: String) {
    private lateinit var lexerPKM: LexerPKM
    private lateinit var lexerFormulario: LexerFormulario
    public lateinit var parserFormulario: Parser
    public lateinit var parserPKM: ParserPKM


    var reporteErrores: String = ""
        private set

    var astFormulario: List<Instruccion>? = null
        private set
    var resultadoFinal: java_cup.runtime.Symbol? = null

    fun analizarPKM(){
        val reader = StringReader(input)
        lexerPKM = LexerPKM(reader)
        parserPKM = ParserPKM(lexerPKM)
        try {
            resultadoFinal = parserPKM.parse()

            val generadorReportes = GeneradorReportes()
            if(generadorReportes.hayErroresPkm(parserPKM)){
                reporteErrores = generadorReportes.generarReportePkm(parserPKM)
            }
        } catch (e: Exception) {
            val generadorReportes = GeneradorReportes()
            reporteErrores = "Error critico en el analisis. \n\n" + generadorReportes.generarReportePkm(parserPKM)
        }
    }

    fun analizarFormulario() {
        val reader = StringReader(input)
        lexerFormulario = LexerFormulario(reader)
        parserFormulario = Parser(lexerFormulario)
        try {
            val resultado = parserFormulario.parse()
            val generadorReportes = GeneradorReportes()

            if (generadorReportes.hayErroresForm(parserFormulario)) {
                reporteErrores = generadorReportes.generarReportesFormulario(parserFormulario)
                astFormulario = null
            } else {
                reporteErrores = ""

                @Suppress("UNCHECKED_CAST")
                astFormulario = resultado.value as? List<Instruccion>
            }

        } catch (e: Exception) {
            val generadorReportes = GeneradorReportes()
            reporteErrores = "Error crítico en el análisis.\n\n" + generadorReportes.generarReportesFormulario(parserFormulario)
            astFormulario = null
        }
    }
}