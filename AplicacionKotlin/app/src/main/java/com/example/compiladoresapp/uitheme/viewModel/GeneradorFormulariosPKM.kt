package com.example.compiladoresapp.uitheme.logic

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.*

class GeneradorFormulariosPKM(
    private val contexto: Context,
    private val contenedorRaiz: LinearLayout
) {

    fun ejecutarAST(nodos: List<NodoPKM>) {
        dibujarBloque(nodos, contenedorRaiz)
    }

    private fun dibujarBloque(nodos: List<NodoPKM>, vistaPadre: LinearLayout) {
        for (nodo in nodos) {
            when (nodo) {
                is NodoPKM.Seccion -> {
                    val orientacionAndroid = if (nodo.orientacion.uppercase() == "HORIZONTAL") {
                        LinearLayout.HORIZONTAL
                    } else {
                        LinearLayout.VERTICAL
                    }

                    val layoutSeccion = LinearLayout(contexto).apply {
                        orientation = orientacionAndroid
                        setBackgroundColor(Color.parseColor("#E0E0E0")) // Fondo por defecto
                        setPadding(20, 20, 20, 20)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 15, 0, 15) }
                    }

                    aplicarEstilosPKM(layoutSeccion, nodo.estilos)
                    dibujarBloque(nodo.elementos, layoutSeccion)
                    vistaPadre.addView(layoutSeccion)
                }

                is NodoPKM.PreguntaAbierta -> {
                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(nodo.etiqueta)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(15, 15, 15, 15)
                        setBackgroundColor(Color.parseColor("#F5F5F5"))
                    }
                    val editText = EditText(contexto).apply { hint = "Escribe tu respuesta..." }

                    aplicarEstilosPKM(textView, nodo.estilos)

                    vistaPadre.addView(textView)
                    vistaPadre.addView(editText)
                }

                is NodoPKM.Desplegable -> {
                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(nodo.etiqueta)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(15, 15, 15, 15)
                    }

                    val spinner = Spinner(contexto)
                    val adapter = ArrayAdapter(contexto, android.R.layout.simple_spinner_item, nodo.opciones)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    aplicarEstilosPKM(textView, nodo.estilos)

                    vistaPadre.addView(textView)
                    vistaPadre.addView(spinner)
                }

                is NodoPKM.Multiple -> {
                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(nodo.etiqueta)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(15, 15, 15, 15)
                    }
                    aplicarEstilosPKM(textView, nodo.estilos)
                    vistaPadre.addView(textView)

                    // Generamos los CheckBoxes
                    for (opcion in nodo.opciones) {
                        val checkBox = android.widget.CheckBox(contexto).apply {
                            text = opcion
                            setPadding(0, 5, 0, 5)
                        }
                        vistaPadre.addView(checkBox)
                    }
                }
            }
        }
    }


    private fun aplicarEstilosPKM(vista: View, estilos: Map<String, String>) {
        for ((clave, valor) in estilos) {
            val claveLimpia = clave.lowercase().trim()
            val valorStr = valor.uppercase().trim()

            when {
                claveLimpia.contains("color") -> {
                    if (vista is android.widget.TextView) {
                        vista.setTextColor(parsearColor(valorStr, Color.BLACK))
                    }
                }
                claveLimpia.contains("background") -> {
                    vista.setBackgroundColor(parsearColor(valorStr, Color.TRANSPARENT))
                }
                claveLimpia.contains("font") -> {
                    if (vista is android.widget.TextView) {
                        when (valorStr) {
                            "MONO" -> vista.typeface = Typeface.MONOSPACE
                            "SANS_SERIF" -> vista.typeface = Typeface.SANS_SERIF
                            "CURSIVE" -> vista.typeface = Typeface.create("cursive", Typeface.NORMAL)
                        }
                    }
                }
                claveLimpia.contains("size") -> {
                    if (vista is android.widget.TextView) {
                        try { vista.textSize = valorStr.toFloat() } catch (e: Exception) {}
                    }
                }
            }
        }
    }

    private fun parsearColor(colorStr: String, default: Int): Int {
        return when (colorStr) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "GREEN" -> Color.GREEN
            "YELLOW" -> Color.YELLOW
            "BLACK" -> Color.BLACK
            "WHITE" -> Color.WHITE
            else -> {
                if (colorStr.startsWith("#")) {
                    try { Color.parseColor(colorStr) } catch (e: Exception) { default }
                } else default
            }
        }
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
}