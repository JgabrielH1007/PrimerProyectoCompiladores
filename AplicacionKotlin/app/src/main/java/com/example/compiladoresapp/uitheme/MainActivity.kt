package com.example.compiladoresapp.uitheme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicacioncompi1.uitheme.logic.GeneradorReportes
import com.example.compiladoresapp.R
import com.example.compiladoresapp.uitheme.logic.Analizador
import com.example.compiladoresapp.uitheme.viewModel.GeneradorDeFormularios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.webkit.WebView
import com.example.compiladoresapp.uitheme.logic.GeneradorFormulariosPKM
import com.example.compiladoresapp.uitheme.logic.NodoPKM
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader

class MainActivity : AppCompatActivity() {

    private lateinit var editorCodigo: EditText

    private lateinit var contenedorFormulario: LinearLayout
    private lateinit var abrirParaEditorLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>

    private var ultimoReporteHtml: String = ""
    private lateinit var guardarArchivoLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
    private var contenidoPendientePorGuardar: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        abrirParaEditorLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    leerArchivoParaEditor(uri)
                }
            }
        }

        guardarArchivoLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    escribirArchivoFisico(uri, contenidoPendientePorGuardar)
                }
            }
        }
        editorCodigo = findViewById(R.id.editorCodigo)
        contenedorFormulario = findViewById(R.id.contenedorFormulario)

        val btnAbrir = findViewById<Button>(R.id.btnAbrir)
        val btnAnalizarColorear = findViewById<Button>(R.id.btnAnalizarColorear)
        val btnGenerar = findViewById<Button>(R.id.btnGenerar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnReportes = findViewById<Button>(R.id.btnReportes)

        btnReportes.setOnClickListener {
            if (ultimoReporteHtml.isBlank()) {
                android.widget.Toast.makeText(this, "Todo está limpio. No hay errores recientes.", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val navegadorHtml = android.webkit.WebView(this).apply {
                loadDataWithBaseURL(null, ultimoReporteHtml, "text/html", "UTF-8", null)
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reporte de Análisis")
                .setView(navegadorHtml) // Incrustamos el WebView
                .setPositiveButton("Cerrar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        btnAbrir.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            abrirParaEditorLauncher.launch(intent)
        }


        btnAnalizarColorear.setOnClickListener {
            val codigo = editorCodigo.text.toString()
            if (codigo.isBlank()) return@setOnClickListener

            val esPKM = codigo.trim().startsWith("###") || codigo.contains("<section=")

            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val analizador = Analizador(codigo)

                    if (esPKM) {
                        analizador.analizarPKM()
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (analizador.reporteErrores.isEmpty()) {
                                android.widget.Toast.makeText(this@MainActivity, "✅ Análisis PKM exitoso", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(this@MainActivity, "❌ Hay errores en el PKM", android.widget.Toast.LENGTH_LONG).show()
                                ultimoReporteHtml = analizador.reporteErrores
                            }
                        }
                    } else {
                        analizador.analizarFormulario()
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (analizador.reporteErrores.isEmpty()) {
                                android.widget.Toast.makeText(this@MainActivity, "✅ Análisis .form exitoso", android.widget.Toast.LENGTH_SHORT).show()

                                aplicarColoresAlEditor()

                            } else {
                                android.widget.Toast.makeText(this@MainActivity, "❌ Hay errores en el .form", android.widget.Toast.LENGTH_LONG).show()
                                ultimoReporteHtml = analizador.reporteErrores
                            }
                        }
                    }
                } catch (e: Throwable) { 
                    e.printStackTrace()
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(this@MainActivity, "💥 Error atrapado: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }



        btnGenerar.setOnClickListener {
            val codigo = editorCodigo.text.toString()
            if (codigo.isBlank()) return@setOnClickListener

            val esPKM = codigo.trim().startsWith("###") || codigo.contains("<section=")

            lifecycleScope.launch(Dispatchers.IO) {
                val analizador = Analizador(codigo)

                withContext(Dispatchers.Main) {
                    contenedorFormulario.removeAllViews()
                }

                if (esPKM) {
                    analizador.analizarPKM()
                    withContext(Dispatchers.Main) {
                        if (analizador.reporteErrores.isEmpty()) {
                            val astPkm =
                                analizador.resultadoFinal?.value as? List<com.example.compiladoresapp.uitheme.logic.NodoPKM>
                            astPkm?.let {
                                com.example.compiladoresapp.uitheme.logic.GeneradorFormulariosPKM(
                                    this@MainActivity,
                                    contenedorFormulario
                                ).ejecutarAST(it)
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "❌ Errores en PKM. No se puede generar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    analizador.analizarFormulario()
                    withContext(Dispatchers.Main) {
                        if (analizador.reporteErrores.isEmpty()) {
                            analizador.astFormulario?.let {
                                com.example.compiladoresapp.uitheme.viewModel.GeneradorDeFormularios(
                                    this@MainActivity,
                                    contenedorFormulario
                                ).ejecutarAST(it)
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "❌ Errores en .form. No se puede generar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        btnGuardar.setOnClickListener {
            val codigo = editorCodigo.text.toString()
            if (codigo.isBlank()) return@setOnClickListener

            val inputNombre =
                EditText(this).apply { hint = "Nombre del archivo"; setPadding(50, 40, 50, 40) }
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Guardar Archivo PKM")
                .setView(inputNombre)
                .setPositiveButton("Guardar") { _, _ ->
                    val nombre = inputNombre.text.toString().trim()
                    if (nombre.isNotEmpty()) {
                        val nombreFinal = if (nombre.endsWith(".pkm")) nombre else "$nombre.pkm"
                        procesarYGuardar(nombreFinal, codigo)
                    }
                }.show()
        }
    }

    private fun aplicarColoresAlEditor() {
        val editable = editorCodigo.text
        if (editable == null || editable.isEmpty()) return

        val codigo = editable.toString()

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val colorVerde = android.graphics.Color.parseColor("#4CAF50")
            val colorNaranja = android.graphics.Color.parseColor("#FF9800")
            val colorCeleste = android.graphics.Color.parseColor("#00BCD4")
            val colorMorado = android.graphics.Color.parseColor("#9C27B0")
            val colorAzul = android.graphics.Color.parseColor("#2196F3")
            val colorAmarillo = android.graphics.Color.parseColor("#FFEB3B")
            val colorBlanco = android.graphics.Color.parseColor("#FFFFFF") // Cambiado para tu fondo blanco


            val zonasAColorear = mutableListOf<Triple<Int, Int, Int>>()

            val patrones = mapOf(
                colorNaranja to "\"([^\"]*)\"",
                colorCeleste to "\\b\\d+(\\.\\d+)?\\b",
                colorMorado to "\\b(special|string|number|boolean|SECTION|TABLE|TEXT|OPEN_QUESTION|MULTIPLE_QUESTION|SELECT_QUESTION|DROP_QUESTION|elements|content|label|options|IF|ELSE|FOR|in|draw)\\b",
                colorAzul to "[{}\\[\\]()]",
                colorVerde to "[+\\-*/]",
                colorAmarillo to "@\\[.*?\\]"
            )

            for ((color, regexStr) in patrones) {
                try {
                    val patron = java.util.regex.Pattern.compile(regexStr)
                    val matcher = patron.matcher(codigo)
                    while (matcher.find()) {
                        zonasAColorear.add(Triple(color, matcher.start(), matcher.end()))
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            withContext(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    val spansViejos = editable.getSpans(0, editable.length, android.text.style.ForegroundColorSpan::class.java)
                    for (span in spansViejos) {
                        editable.removeSpan(span)
                    }

                    editable.setSpan(
                        android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK),
                        0, editable.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    for ((color, inicio, fin) in zonasAColorear) {
                        if (inicio >= 0 && fin <= editable.length) {
                            editable.setSpan(
                                android.text.style.ForegroundColorSpan(color),
                                inicio, fin,
                                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun procesarYGuardar(nombreSugerido: String, codigoEntrada: String) {
        val esPKM = codigoEntrada.trim().startsWith("###") || codigoEntrada.contains("<section=")

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var textoAFisico = ""

                if (esPKM) {
                    val analizador = Analizador(codigoEntrada)
                    analizador.analizarPKM()

                    if (analizador.reporteErrores.isEmpty()) {
                        textoAFisico = codigoEntrada
                    } else {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            android.widget.Toast.makeText(this@MainActivity, "Hay errores léxicos o sintácticos.", android.widget.Toast.LENGTH_LONG).show()
                            ultimoReporteHtml = analizador.reporteErrores
                        }
                        return@launch
                    }
                } else {
                    val analizador = Analizador(codigoEntrada)
                    analizador.analizarFormulario()

                    if (analizador.reporteErrores.isEmpty()) {
                        analizador.astFormulario?.let { arbol ->
                            val generadorPkm = com.example.compiladoresapp.uitheme.logic.GeneradorPKM(mutableMapOf())
                            textoAFisico = generadorPkm.generarArchivoPKM(arbol)
                        }
                    } else {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            android.widget.Toast.makeText(this@MainActivity, "Hay errores léxicos o sintácticos.", android.widget.Toast.LENGTH_LONG).show()
                            ultimoReporteHtml = analizador.reporteErrores
                        }
                        return@launch
                    }
                }

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    contenidoPendientePorGuardar = textoAFisico
                    val intent = android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(android.content.Intent.CATEGORY_OPENABLE)
                        type = "application/octet-stream"
                        putExtra(android.content.Intent.EXTRA_TITLE, nombreSugerido)
                    }
                    guardarArchivoLauncher.launch(intent)
                }

            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(this@MainActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun leerArchivoParaEditor(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val texto = inputStream?.bufferedReader().use { it?.readText() } ?: ""
            editorCodigo.setText(texto)
            android.widget.Toast.makeText(this, "Archivo cargado", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error al leer archivo", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun escribirArchivoFisico(uri: android.net.Uri, contenido: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contenido.toByteArray())
            }
            android.widget.Toast.makeText(this, "✅ Guardado exitosamente", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error al escribir archivo", android.widget.Toast.LENGTH_SHORT).show()
        }
    }



}