package com.example.compiladoresapp.uitheme.logic

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.*
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
                        setPadding(20, 20, 20, 20)

                        // Si el padre es horizontal (como en una LINEA de TABLA), nos repartimos el espacio (weight = 1)
                        layoutParams = if (vistaPadre.orientation == LinearLayout.HORIZONTAL) {
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(10, 10, 10, 10) }
                        } else {
                            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 15, 0, 15) }
                        }
                    }

                    aplicarEstilosPKM(layoutSeccion, nodo.estilos)
                    dibujarBloque(nodo.elementos, layoutSeccion)
                    vistaPadre.addView(layoutSeccion)
                }

                is NodoPKM.PreguntaAbierta -> {
                    val contenedor = crearContenedorPregunta(vistaPadre.orientation)

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(nodo.etiqueta)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(15, 15, 15, 15)
                    }
                    val editText = EditText(contexto).apply { hint = "Escribe tu respuesta..." }

                    aplicarEstilosPKM(contenedor, nodo.estilos) // El fondo va al contenedor
                    aplicarEstilosPKM(textView, nodo.estilos)   // La fuente/color va al texto

                    contenedor.addView(textView)
                    contenedor.addView(editText)
                    vistaPadre.addView(contenedor)
                }

                is NodoPKM.Desplegable -> {
                    val contenedor = crearContenedorPregunta(vistaPadre.orientation)

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

                    aplicarEstilosPKM(contenedor, nodo.estilos)
                    aplicarEstilosPKM(textView, nodo.estilos)

                    contenedor.addView(textView)
                    contenedor.addView(spinner)
                    vistaPadre.addView(contenedor)
                }

                is NodoPKM.Multiple -> {
                    val contenedor = crearContenedorPregunta(vistaPadre.orientation)

                    val textView = TextView(contexto).apply {
                        text = procesarEmojisGraficos(nodo.etiqueta)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(15, 15, 15, 15)
                    }
                    aplicarEstilosPKM(contenedor, nodo.estilos)
                    aplicarEstilosPKM(textView, nodo.estilos)
                    contenedor.addView(textView)

                    for (opcion in nodo.opciones) {
                        val checkBox = android.widget.CheckBox(contexto).apply {
                            text = opcion
                            setPadding(0, 5, 0, 5)
                        }
                        contenedor.addView(checkBox)
                    }
                    vistaPadre.addView(contenedor)
                }
            }
        }
    }

    // =======================================================
    // FUNCIÓN AUXILIAR: Crea una "Caja Vertical" para agrupar título y respuesta
    // =======================================================
    private fun crearContenedorPregunta(orientacionPadre: Int): LinearLayout {
        return LinearLayout(contexto).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = if (orientacionPadre == LinearLayout.HORIZONTAL) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(10, 10, 10, 10) }
            } else {
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 10, 0, 10) }
            }
        }
    }

    // =======================================================
    // MOTOR DE ESTILOS CORREGIDO
    // =======================================================
    private fun aplicarEstilosPKM(vista: View, estilos: Map<String, String>) {
        for ((clave, valor) in estilos) {
            val claveLimpia = clave.lowercase().trim()
            val valorStr = valor.uppercase().trim()

            when {
                // FIX: Chequear "background" primero para que "color" no se lo robe
                claveLimpia.contains("background") -> {
                    vista.setBackgroundColor(parsearColor(valorStr, Color.TRANSPARENT))
                }
                claveLimpia.contains("color") -> {
                    if (vista is android.widget.TextView) {
                        vista.setTextColor(parsearColor(valorStr, Color.BLACK))
                    }
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

    // =======================================================
    // PARSEADOR DE COLORES (Ahora entiende RGB y HSL)
    // =======================================================
    private fun parsearColor(colorStr: String, default: Int): Int {
        return try {
            when {
                colorStr == "RED" -> Color.RED
                colorStr == "BLUE" -> Color.BLUE
                colorStr == "GREEN" -> Color.GREEN
                colorStr == "YELLOW" -> Color.YELLOW
                colorStr == "BLACK" -> Color.BLACK
                colorStr == "WHITE" -> Color.WHITE
                colorStr == "SKY" -> Color.parseColor("#87CEEB")
                colorStr == "PURPLE" -> Color.parseColor("#9C27B0")

                colorStr.startsWith("#") -> Color.parseColor(colorStr)

                // Formato RGB: (255,0,0)
                colorStr.startsWith("(") && colorStr.endsWith(")") -> {
                    val partes = colorStr.removeSurrounding("(", ")").split(",")
                    Color.rgb(partes[0].trim().toInt(), partes[1].trim().toInt(), partes[2].trim().toInt())
                }

                // Formato HSL: <45,100,50> (Android usa HSV, hacemos una conversión rápida)
                colorStr.startsWith("<") && colorStr.endsWith(">") -> {
                    val partes = colorStr.removeSurrounding("<", ">").split(",")
                    val h = partes[0].trim().toFloat()
                    val s = partes[1].trim().toFloat() / 100f
                    val l = partes[2].trim().toFloat() / 100f

                    val v = l + s * Math.min(l, 1 - l)
                    val sHsv = if (v == 0f) 0f else 2 * (1 - l / v)
                    Color.HSVToColor(floatArrayOf(h, sHsv, v))
                }
                else -> default
            }
        } catch (e: Exception) {
            default
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