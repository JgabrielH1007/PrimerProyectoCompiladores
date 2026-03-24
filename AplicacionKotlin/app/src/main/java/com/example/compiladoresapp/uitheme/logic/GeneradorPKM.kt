package com.example.compiladoresapp.uitheme.logic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeneradorPKM(private val entorno: MutableMap<String, Any?>) {

    private var totalSecciones = 0
    private var totalPreguntas = 0
    private var totalAbiertas = 0
    private var totalDesplegables = 0
    private var totalSeleccion = 0
    private var totalMultiples = 0

    fun generarArchivoPKM(instrucciones: List<Instruccion>): String {
        // Reiniciamos contadores
        totalSecciones = 0; totalPreguntas = 0; totalAbiertas = 0
        totalDesplegables = 0; totalSeleccion = 0; totalMultiples = 0

        val cuerpoArchivo = generarBloque(instrucciones)

        return buildString {
            append("###\n")
            append("Author: \"GeneradorAutomatico\"\n")
            append("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}\n")
            append("Hora: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}\n")
            append("Description: \"Formulario compilado\"\n")
            append("Total de Secciones: $totalSecciones\n")
            append("Total de Preguntas: $totalPreguntas\n")
            append("Abiertas: $totalAbiertas\n")
            append("Desplegables: $totalDesplegables\n")
            append("Selección: $totalSeleccion\n")
            append("Múltiples: $totalMultiples\n")
            append("###\n\n") // Separador de metadatos
            append(cuerpoArchivo)
        }
    }

    private fun generarBloque(instrucciones: List<Instruccion>): String {
        val sb = StringBuilder()
        for (instruccion in instrucciones) {
            when (instruccion) {

                is Instruccion.Declaracion -> {
                    entorno[instruccion.id] = evaluarSiEsExpresion(instruccion.valor)
                }


                is Instruccion.InvocacionDibujar -> {
                    val componente = entorno[instruccion.idVariable]
                    if (componente is Instruccion.ComponenteUI) {
                        sb.append(escribirComponentePKM(componente))
                    }
                }

                is Instruccion.EstructuraIf -> {
                    val condicion = evaluarExp(instruccion.condicion) as? Boolean ?: false
                    if (condicion) {
                        sb.append(generarBloque(instruccion.bloqueIf))
                    } else if (instruccion.bloqueElse != null) {
                        val bloque = instruccion.bloqueElse
                        if (bloque is List<*>) {
                            sb.append(generarBloque(bloque as List<Instruccion>))
                        } else if (bloque is Instruccion.EstructuraIf) {
                            sb.append(generarBloque(listOf(bloque)))
                        }
                    }
                }

                is Instruccion.CicloFor -> {
                    val inicio = (evaluarExp(instruccion.inicio) as Double).toInt()
                    val fin = (evaluarExp(instruccion.fin) as Double).toInt()

                    for (i in inicio..fin) {
                        entorno[instruccion.iterador] = i.toDouble()
                        sb.append(generarBloque(instruccion.bloque))
                    }
                }

                is Instruccion.ComponenteUI -> {
                    sb.append(escribirComponentePKM(instruccion))
                }

                else -> {}
            }
        }
        return sb.toString()
    }

    private fun escribirComponentePKM(comp: Instruccion.ComponenteUI): String {
        val estilosStr = generarEstilos(comp.atributos["styles"])

        return when (comp) {
            is Instruccion.ComponenteUI.Section -> {
                totalSecciones++
                val ori = comp.atributos["orientation"]?.toString() ?: "VERTICAL"
                buildString {
                    append("<section=0,0,0,0,$ori>\n")
                    append(estilosStr) // Aquí se insertan los estilos de la sección
                    append("<content>\n")
                    val elementos = comp.atributos["elements"] as? List<Instruccion> ?: emptyList()
                    append(generarBloque(elementos))
                    append("</content>\n</section>\n")
                }
            }

            is Instruccion.ComponenteUI.Table -> {
                buildString {
                    append("<table>\n")
                    append(estilosStr)
                    append("<content>\n")
                    val elementos = comp.atributos["elements"] as? List<Instruccion> ?: emptyList()

                    for (elem in elementos) {
                        append("<line>\n<element>\n")
                        append(generarBloque(listOf(elem)))
                        append("</element>\n</line>\n")
                    }

                    append("</content>\n</table>\n")
                }
            }

            is Instruccion.ComponenteUI.Text -> {
                totalPreguntas++; totalAbiertas++
                val txt = obtenerTexto("content", comp.atributos)
                if (estilosStr.isNotEmpty()) "<open=0,0,\"$txt\">\n$estilosStr</open>\n"
                else "<open=0,0,\"$txt\"/>\n"
            }

            is Instruccion.ComponenteUI.OpenQuestion -> {
                totalPreguntas++; totalAbiertas++
                val lbl = obtenerTexto("label", comp.atributos)
                if (estilosStr.isNotEmpty()) "<open=0,0,\"$lbl\">\n$estilosStr</open>\n"
                else "<open=0,0,\"$lbl\"/>\n"
            }

            is Instruccion.ComponenteUI.MultipleQuestion -> {
                totalPreguntas++; totalMultiples++
                val lbl = obtenerTexto("label", comp.atributos)
                val opc = obtenerListaOpciones("options", comp.atributos)
                if (estilosStr.isNotEmpty()) "<multiple=0,0,\"$lbl\",{$opc},{}>\n$estilosStr</multiple>\n"
                else "<multiple=0,0,\"$lbl\",{$opc},{}>\n"
            }

            is Instruccion.ComponenteUI.DropQuestion-> {
                totalPreguntas++; totalSeleccion++
                val lbl = obtenerTexto("label", comp.atributos)
                val opc = obtenerListaOpciones("options", comp.atributos)
                if (estilosStr.isNotEmpty()) "<select=0,0,\"$lbl\",{$opc},0>\n$estilosStr</select>\n"
                else "<select=0,0,\"$lbl\",{$opc},0/>\n"
            }

            is Instruccion.ComponenteUI.SelectQuestion -> {
                totalPreguntas++; totalDesplegables++
                val lbl = obtenerTexto("label", comp.atributos)
                val opc = obtenerListaOpciones("options", comp.atributos)
                if (estilosStr.isNotEmpty()) "<drop=0,0,\"$lbl\",{$opc},0>\n$estilosStr</drop>\n"
                else "<drop=0,0,\"$lbl\",{$opc},0/>\n"
            }

            else -> ""
        }
    }

    private fun generarEstilos(estilosRaw: Any?): String {
        if (estilosRaw !is Map<*, *> || estilosRaw.isEmpty()) return ""

        return buildString {
            append("<style>\n")
            for ((clave, valor) in estilosRaw) {
                val cl = clave.toString().replace("\"", "").trim().lowercase()

                val vl = valor.toString().replace("\"", "").replace(" ", "")

                when {
                    cl.contains("background") || cl.contains("fondo") -> append("<background color=$vl/>\n")
                    cl.contains("color") || cl.contains("texto") -> append("<color=$vl/>\n")
                    cl.contains("font") || cl.contains("fuente") -> append("<font family=$vl/>\n")
                    cl.contains("size") || cl.contains("tamaño") -> append("<text size=$vl/>\n")
                }
            }
            append("</style>\n")
        }
    }

    private fun obtenerTexto(clave: String, atributos: Map<String, Any?>): String {
        return evaluarSiEsExpresion(atributos[clave]).toString()
    }

    private fun obtenerListaOpciones(clave: String, atributos: Map<String, Any?>): String {
        val eval = evaluarSiEsExpresion(atributos[clave])
        return if (eval is List<*>) eval.joinToString(",") { "\"$it\"" } else "\"$eval\""
    }

    private fun evaluarSiEsExpresion(valor: Any?): Any? = if (valor is Expresion) evaluarExp(valor) else valor

    private fun evaluarExp(expr: Expresion): Any {
        return when (expr) {
            is Expresion.Numero -> expr.valor
            is Expresion.Cadena -> expr.valor
            is Expresion.Identificador -> entorno[expr.nombre] ?: 0.0
            is Expresion.OperacionAritmetica -> {
                val vI = (evaluarExp(expr.izq!!) as Number).toDouble()
                val vD = (evaluarExp(expr.der!!) as Number).toDouble()
                when (expr.operador) {
                    "+" -> vI + vD; "-" -> vI - vD; "*" -> vI * vD; "/" -> vI / vD; else -> 0.0
                }
            }
            is Expresion.OperacionRelacional -> {
                val vI = (evaluarExp(expr.izq!!) as Number).toDouble()
                val vD = (evaluarExp(expr.der!!) as Number).toDouble()
                when (expr.operador) {
                    ">" -> vI > vD; "<" -> vI < vD; ">=" -> vI >= vD; "<=" -> vI <= vD; "==" -> vI == vD; else -> false
                }
            }
            is Expresion.OperacionLogica -> {
                val vI = evaluarExp(expr.izq!!) as Boolean
                val vD = evaluarExp(expr.der!!) as Boolean
                when (expr.operador) {
                    "&&" -> vI && vD
                    "||" -> vI || vD
                    else -> false
                }
            }
            is Expresion.LlamadaPokeApi -> {
                val ini = (evaluarExp(expr.inicio!!) as Number).toInt()
                val fin = (evaluarExp(expr.fin!!) as Number).toInt()
                obtenerPokemonesSincrono(ini, fin)
            }
            else -> false
        }
    }

    private fun obtenerPokemonesSincrono(inicio: Int, fin: Int): List<String> {
        val lista = mutableListOf<String>()
        for (i in inicio..fin) {
            try {
                val url = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i")
                val conn = url.openConnection() as java.net.HttpURLConnection
                if (conn.responseCode == 200) {
                    val res = conn.inputStream.bufferedReader().use { it.readText() }
                    lista.add(org.json.JSONObject(res).getString("name").replaceFirstChar { it.uppercase() })
                }
            } catch (e: Exception) { lista.add("Poke-$i") }
        }
        return lista
    }
}