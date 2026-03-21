package com.example.compiladoresapp.uitheme.logic

sealed class Expresion {
    data class Numero(val valor: Double) : Expresion()
    data class Cadena(val valor: String) : Expresion()
    data class Identificador(val nombre: String) : Expresion()
    data class OperacionAritmetica(val izq: Expresion, val der: Expresion, val operador: String) : Expresion()
    data class OperacionRelacional(val izq: Expresion, val der: Expresion, val operador: String) : Expresion()
    data class OperacionLogica(val izq: Expresion?, val der: Expresion, val operador: String) : Expresion() // izq es nullable por el NOT
    data class LlamadaPokeApi(val inicio: Expresion, val fin: Expresion) : Expresion()
    data class Comodin(val valor: String = "comodin") : Expresion()
}

sealed class Instruccion {
    data class Declaracion(val tipo: String, val id: String, val valor: Any?) : Instruccion()
    data class Asignacion(val id: String, val valor: Any) : Instruccion()

    data class EstructuraIf(val condicion: Expresion, val bloqueIf: List<Instruccion>, val bloqueElse: Any?) : Instruccion() // bloqueElse puede ser List o EstructuraIf
    data class CicloWhile(val condicion: Expresion, val bloque: List<Instruccion>) : Instruccion()
    data class CicloDoWhile(val bloque: List<Instruccion>, val condicion: Expresion) : Instruccion()
    data class CicloFor(val iterador: String, val inicio: Expresion, val fin: Expresion, val bloque: List<Instruccion>) : Instruccion()

    data class InvocacionDibujar(val idVariable: String, val comodines: List<Expresion>?) : Instruccion()

    sealed class ComponenteUI : Instruccion() {
        abstract val atributos: Map<String, Any>
        data class Section(override val atributos: Map<String, Any>) : ComponenteUI()
        data class Table(override val atributos: Map<String, Any>) : ComponenteUI()
        data class Text(override val atributos: Map<String, Any>) : ComponenteUI()
        data class OpenQuestion(override val atributos: Map<String, Any>) : ComponenteUI()
        data class DropQuestion(override val atributos: Map<String, Any>) : ComponenteUI()
        data class SelectQuestion(override val atributos: Map<String, Any>) : ComponenteUI()
        data class MultipleQuestion(override val atributos: Map<String, Any>) : ComponenteUI()
    }
}