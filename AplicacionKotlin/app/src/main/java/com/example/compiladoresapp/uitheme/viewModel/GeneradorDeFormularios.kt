package com.example.compiladoresapp.uitheme.viewModel

import android.content.Context
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.compiladoresapp.uitheme.logic.Expresion
import com.example.compiladoresapp.uitheme.logic.Instruccion
import kotlinx.coroutines.launch
import android.graphics.Typeface
import android.view.View


class GeneradorDeFormularios(
    private val contexto: Context,
    private val contenedorRaiz: LinearLayout
) {
    private val entornoGlobal = mutableMapOf<String, Any?>()

    fun ejecutarAST(instrucciones: List<Instruccion>) {
        //contenedorRaiz.removeAllViews()
        ejecutarBloque(instrucciones, entornoGlobal, contenedorRaiz)
    }

    private fun ejecutarBloque(instrucciones: List<Instruccion>, entorno: MutableMap<String, Any?>, vistaPadre: LinearLayout) {
        for (instruccion in instrucciones) {
            when (instruccion) {

                is Instruccion.Declaracion -> {
                    val valorEvaluado = siEsExpresionEvaluar(instruccion.valor, entorno)
                    entorno[instruccion.id] = valorEvaluado
                }

                is Instruccion.Asignacion -> {
                    if (entorno.containsKey(instruccion.id)) {
                        entorno[instruccion.id] = siEsExpresionEvaluar(instruccion.valor, entorno)
                    } else {
                        android.widget.Toast.makeText(contexto, "Error: Variable '${instruccion.id}' no declarada", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                is Instruccion.EstructuraIf -> {
                    val condicion = evaluarExpresion(instruccion.condicion, entorno) as Boolean
                    if (condicion) {
                        ejecutarBloque(instruccion.bloqueIf, entorno, vistaPadre)
                    } else if (instruccion.bloqueElse != null) {
                        if (instruccion.bloqueElse is List<*>) {
                            @Suppress("UNCHECKED_CAST")
                            ejecutarBloque(instruccion.bloqueElse as List<Instruccion>, entorno, vistaPadre)
                        } else if (instruccion.bloqueElse is Instruccion.EstructuraIf) {
                            ejecutarBloque(listOf(instruccion.bloqueElse), entorno, vistaPadre)
                        }
                    }
                }

                is Instruccion.CicloWhile -> {
                    while (evaluarExpresion(instruccion.condicion, entorno) as Boolean) {
                        ejecutarBloque(instruccion.bloque, entorno, vistaPadre)
                    }
                }

                is Instruccion.CicloDoWhile -> {
                    do {
                        ejecutarBloque(instruccion.bloque, entorno, vistaPadre)
                    } while (evaluarExpresion(instruccion.condicion, entorno) as Boolean)
                }

                is Instruccion.CicloFor -> {
                    val inicio = evaluarExpresion(instruccion.inicio, entorno) as Double
                    val fin = evaluarExpresion(instruccion.fin, entorno) as Double

                    for (i in inicio.toInt()..fin.toInt()) {
                        entorno[instruccion.iterador] = i.toDouble()
                        ejecutarBloque(instruccion.bloque, entorno, vistaPadre)
                    }
                }

                is Instruccion.ComponenteUI.Section -> {
                    val layoutSeccion = LinearLayout(contexto).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setPadding(20, 20, 20, 20)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 15, 0, 15) }
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                    }
                    aplicarEstilosVisuales(layoutSeccion, instruccion.atributos["styles"])
                    val elementosRaw = instruccion.atributos["elements"] as? List<Instruccion> ?: emptyList()
                    ejecutarBloque(elementosRaw, entorno, layoutSeccion)
                    vistaPadre.addView(layoutSeccion)
                }

                is Instruccion.ComponenteUI.Table -> {
                    val layoutTabla = LinearLayout(contexto).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundColor(Color.parseColor("#FAFAFA"))
                        setPadding(10, 10, 10, 10)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 10, 0, 10) }
                    }
                    aplicarEstilosVisuales(layoutTabla, instruccion.atributos["styles"])
                    val elementosRaw = instruccion.atributos["elements"] as? List<Instruccion> ?: emptyList()
                    ejecutarBloque(elementosRaw, entorno, layoutTabla)

                    vistaPadre.addView(layoutTabla)
                }

                is Instruccion.ComponenteUI.Text -> {
                    val textoRaw = instruccion.atributos["content"] ?: "Texto vacío"
                    val contenidoFinal = siEsExpresionEvaluar(textoRaw, entorno).toString()

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(contenidoFinal)
                        textSize = 16f
                        setTextColor(Color.BLACK)
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setPadding(0, 10, 0, 10)
                    }
                    aplicarEstilosVisuales(textView, instruccion.atributos["styles"])
                    vistaPadre.addView(textView)
                }

                is Instruccion.ComponenteUI.OpenQuestion -> {
                    val labelRaw = instruccion.atributos["label"] ?: "Pregunta"
                    val labelFinal = siEsExpresionEvaluar(labelRaw, entorno).toString()

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(labelFinal)
                        textSize = 15f
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setPadding(0, 20, 0, 5)
                    }
                    val editText = EditText(contexto).apply { hint = "Escribe tu respuesta..." }
                    aplicarEstilosVisuales(textView, instruccion.atributos["styles"])
                    vistaPadre.addView(textView)
                    vistaPadre.addView(editText)
                }

                is Instruccion.ComponenteUI.DropQuestion, is Instruccion.ComponenteUI.SelectQuestion -> {
                    val componente = instruccion as Instruccion.ComponenteUI

                    val labelRaw = componente.atributos["label"] ?: "Selecciona:"
                    val labelFinal = siEsExpresionEvaluar(labelRaw, entorno).toString()

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(labelFinal)
                        textSize = 15f
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setPadding(0, 20, 0, 5)
                    }

                    val spinner = Spinner(contexto)
                    aplicarEstilosVisuales(textView, instruccion.atributos["styles"])
                    vistaPadre.addView(textView)
                    vistaPadre.addView(spinner)

                    val optionsAttr = componente.atributos["options"]

                    if (optionsAttr is Expresion.LlamadaPokeApi) {

                        val adapterCargando = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, listOf("Cargando Pokémon... ⏳"))
                        spinner.adapter = adapterCargando

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            try {
                                val inicio = (evaluarExpresion(optionsAttr.inicio, entorno) as Double).toInt()
                                val fin = (evaluarExpresion(optionsAttr.fin, entorno) as Double).toInt()

                                val listaPokemon = obtenerPokemones(inicio, fin)

                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    val adapterReal = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, listaPokemon)
                                    adapterReal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinner.adapter = adapterReal
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    spinner.adapter = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, listOf("❌ Error de red"))
                                }
                            }
                        }
                    } else {
                        val opcionesEval = siEsExpresionEvaluar(optionsAttr, entorno)
                        val listaOpcionesTexto = if (opcionesEval is List<*>) {
                            opcionesEval.map { siEsExpresionEvaluar(it, entorno).toString() }
                        } else {
                            listOf("Sin opciones")
                        }
                        val adapter = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, listaOpcionesTexto)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
                }

                is Instruccion.ComponenteUI.MultipleQuestion -> {
                    val componente = instruccion as Instruccion.ComponenteUI

                    val labelRaw = componente.atributos["label"] ?: "Selecciona varias:"
                    val labelFinal = siEsExpresionEvaluar(labelRaw, entorno).toString()

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(labelFinal)
                        textSize = 15f
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                        setPadding(0, 20, 0, 10)
                    }
                    aplicarEstilosVisuales(textView, instruccion.atributos["styles"])
                    vistaPadre.addView(textView)

                    val optionsAttr = componente.atributos["options"]

                    if (optionsAttr is Expresion.LlamadaPokeApi) {

                        val loadingText = TextView(contexto).apply {
                            text = "Cargando Pokémon... ⏳"
                            setTextColor(Color.GRAY)
                        }
                        vistaPadre.addView(loadingText)

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            try {
                                val inicio = (evaluarExpresion(optionsAttr.inicio, entorno) as Double).toInt()
                                val fin = (evaluarExpresion(optionsAttr.fin, entorno) as Double).toInt()

                                val listaPokemon = obtenerPokemones(inicio, fin)

                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    vistaPadre.removeView(loadingText)

                                    for (pokemon in listaPokemon) {
                                        val checkBox = android.widget.CheckBox(contexto).apply {
                                            text = pokemon
                                            setPadding(0, 5, 0, 5)
                                        }
                                        vistaPadre.addView(checkBox)
                                    }
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    loadingText.text = "❌ Error al cargar Pokémon"
                                }
                            }
                        }
                    } else {
                        val opcionesEval = siEsExpresionEvaluar(optionsAttr, entorno)
                        val listaOpcionesTexto = if (opcionesEval is List<*>) {
                            opcionesEval.map { siEsExpresionEvaluar(it, entorno).toString() }
                        } else {
                            listOf("Opción")
                        }

                        for (opcionTexto in listaOpcionesTexto) {
                            val checkBox = android.widget.CheckBox(contexto).apply {
                                text = opcionTexto
                                setPadding(0, 5, 0, 5)
                            }
                            vistaPadre.addView(checkBox)
                        }
                    }
                }

                is Instruccion.InvocacionDibujar -> {
                    val variableUI = entorno[instruccion.idVariable]
                    if (variableUI is Instruccion.ComponenteUI) {
                        ejecutarBloque(listOf(variableUI), entorno, vistaPadre)
                    } else {
                        android.widget.Toast.makeText(
                            contexto,
                            "Error de Ejecución: La variable '${instruccion.idVariable}' está vacía o no es un componente gráfico.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }

                else -> { /* No hacer nada con instrucciones no reconocidas */ }
            }
        }
    }

    private fun evaluarExpresion(expr: Expresion, entorno: MutableMap<String, Any?>): Any {
        return when (expr) {
            is Expresion.Numero -> expr.valor
            is Expresion.Cadena -> expr.valor
            is Expresion.Identificador -> entorno[expr.nombre] ?: throw Exception("Variable ${expr.nombre} no existe.")
            is Expresion.OperacionAritmetica -> {
                val valIzq = evaluarExpresion(expr.izq, entorno) as Double
                val valDer = evaluarExpresion(expr.der, entorno) as Double
                when (expr.operador) {
                    "+" -> valIzq + valDer
                    "-" -> valIzq - valDer
                    "*" -> valIzq * valDer
                    "/" -> valIzq / valDer
                    else -> 0.0
                }
            }
            is Expresion.OperacionRelacional -> {
                val valIzq = evaluarExpresion(expr.izq, entorno) as Double
                val valDer = evaluarExpresion(expr.der, entorno) as Double
                when (expr.operador) {
                    ">" -> valIzq > valDer
                    "<" -> valIzq < valDer
                    ">=" -> valIzq >= valDer
                    "<=" -> valIzq <= valDer
                    "==" -> valIzq == valDer
                    "!=" -> valIzq != valDer
                    else -> false
                }
            }
            is Expresion.OperacionLogica -> {
                val valDer = evaluarExpresion(expr.der, entorno) as Boolean
                if (expr.operador == "NOT") {
                    !valDer
                } else {
                    val valIzq = evaluarExpresion(expr.izq!!, entorno) as Boolean
                    when (expr.operador) {
                        "AND" -> valIzq && valDer
                        "OR" -> valIzq || valDer
                        else -> false
                    }
                }
            }
            is Expresion.Comodin -> {
                "*"
            }
            is Expresion.LlamadaPokeApi -> {
                val inicio = (evaluarExpresion(expr.inicio, entorno) as Double).toInt()
                val fin = (evaluarExpresion(expr.fin, entorno) as Double).toInt()

                obtenerPokemones(inicio, fin)
            }
        }
    }

    private fun obtenerPokemones(inicio: Int, fin: Int): List<String> {
        val listaNombres = mutableListOf<String>()

        for (i in inicio..fin) {
            try {
                val url = java.net.URL("https://pokeapi.co/api/v2/pokemon/$i")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    val jsonObject = org.json.JSONObject(response)
                    val nombre = jsonObject.getString("name")

                    listaNombres.add(nombre.replaceFirstChar { it.uppercase() })
                } else {
                    listaNombres.add("Desconocido ($i)")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listaNombres.add("Error de red ($i)")
            }
        }
        return listaNombres
    }

    private fun siEsExpresionEvaluar(valor: Any?, entorno: MutableMap<String, Any?>): Any? {
        if (valor is Expresion) {
            return evaluarExpresion(valor, entorno)
        }

        return valor
    }

    private fun procesarEmojisGraficos(textoOriginal: String): String {
        var textoFinal = textoOriginal

        textoFinal = textoFinal.replace(Regex("@\\[:\\)+\\]|@\\[:smile:\\]"), "😀")

        textoFinal = textoFinal.replace(Regex("@\\[:\\(+\\]|@\\[:sad:\\]"), "😢")

        textoFinal = textoFinal.replace(Regex("@\\[:\\]+\\]|@\\[:serious:\\]"), "😐")

        textoFinal = textoFinal.replace(Regex("@\\[<3+\\]|@\\[:heart:\\]"), "❤️")

        textoFinal = textoFinal.replace(Regex("@\\[:star:\\]|@\\[:star:\\d+:\\]|@\\[:star-\\d+-:\\]"), "⭐")

        textoFinal = textoFinal.replace(Regex("@\\[:\\^\\^:\\]|@\\[:cat:\\]"), "🐱")

        return textoFinal
    }

    private fun aplicarEstilosVisuales(vista: View, estilosRaw: Any?) {
        if (estilosRaw !is Map<*, *>) return

        for ((clave, valor) in estilosRaw) {
            val claveLimpia = clave.toString().trim().replace("\"", "").lowercase()
            val valorStr = valor.toString().trim().replace("\"", "")

            when {
                claveLimpia.contains("color") || claveLimpia.contains("texto") -> {
                    if (vista is android.widget.TextView) {
                        vista.setTextColor(parsearColor(valorStr, Color.BLACK))
                    }
                }

                claveLimpia.contains("background") || claveLimpia.contains("fondo") -> {
                    vista.setBackgroundColor(parsearColor(valorStr, Color.TRANSPARENT))
                }

                claveLimpia.contains("font") || claveLimpia.contains("fuente") -> {
                    if (vista is android.widget.TextView) {
                        when (valorStr.uppercase()) {
                            "MONO" -> vista.typeface = Typeface.MONOSPACE
                            "SANS_SERIF" -> vista.typeface = Typeface.SANS_SERIF
                            "CURSIVE" -> vista.typeface = Typeface.create("cursive", Typeface.NORMAL)
                        }
                    }
                }

                claveLimpia.contains("size") || claveLimpia.contains("tamaño") -> {
                    if (vista is android.widget.TextView) {
                        try {
                            vista.textSize = valorStr.toFloat()
                        } catch (e: Exception) { /* Ignorar si no es número */ }
                    }
                }
            }
        }
    }

    private fun parsearColor(colorStr: String, colorPorDefecto: Int): Int {
        val colorUpper = colorStr.uppercase()
        return when (colorUpper) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "GREEN" -> Color.GREEN
            "YELLOW" -> Color.YELLOW
            "BLACK" -> Color.BLACK
            "WHITE" -> Color.WHITE
            "PURPLE" -> Color.parseColor("#800080")
            "SKY" -> Color.parseColor("#87CEEB")
            else -> {
                try {
                    if (colorUpper.startsWith("#")) {
                        // Color Hexadecimal
                        Color.parseColor(colorUpper)
                    } else if (colorUpper.startsWith("(")) {
                        // Color RGB: ( 255 , 0 , 0 ) -> Quitamos paréntesis y espacios
                        val rgb = colorUpper.replace(Regex("[() ]"), "").split(",")
                        Color.rgb(rgb[0].toInt(), rgb[1].toInt(), rgb[2].toInt())
                    } else {
                        colorPorDefecto
                    }
                } catch (e: Exception) {
                    colorPorDefecto
                }
            }
        }
    }
}