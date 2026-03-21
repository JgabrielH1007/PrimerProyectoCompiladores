package com.example.compiladoresapp.uitheme.logic

sealed class NodoPKM {

    data class Seccion(
        val x: Double, val y: Double, val w: Double, val h: Double,
        val orientacion: String,
        val estilos: Map<String, String>,
        val elementos: List<NodoPKM>
    ) : NodoPKM()

    data class PreguntaAbierta(
        val x: Double, val y: Double,
        val etiqueta: String,
        val estilos: Map<String, String>
    ) : NodoPKM()

    data class Desplegable(
        val x: Double, val y: Double,
        val etiqueta: String,
        val opciones: List<String>,
        val estilos: Map<String, String>
    ) : NodoPKM()

    data class Multiple(
        val x: Double, val y: Double,
        val etiqueta: String,
        val opciones: List<String>,
        val estilos: Map<String, String>
    ) : NodoPKM()
}