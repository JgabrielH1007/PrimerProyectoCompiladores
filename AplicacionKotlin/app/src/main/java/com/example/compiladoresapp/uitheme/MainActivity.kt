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
    private lateinit var btnAnalizar: Button
    private lateinit var btnReportes: Button
    private lateinit var contenedorFormulario: LinearLayout
    private lateinit var abrirParaEditorLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>

    private lateinit var abrirParaRenderLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
    private lateinit var btnContestar: Button

    private var ultimoReporteHtml: String = ""
    private lateinit var guardarArchivoLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
    private var contenidoPendientePorGuardar: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editorCodigo = findViewById(R.id.editorCodigo)
        btnAnalizar = findViewById(R.id.btnAnalizar)
        btnReportes = findViewById(R.id.btnReportes)
        btnContestar = findViewById(R.id.btnContestar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        abrirParaEditorLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    leerArchivoParaEditor(uri)
                }
            }
        }

        abrirParaRenderLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    leerArchivoParaRenderizarPKM(uri)
                }
            }
        }

        val btnAbrirEnEditor = findViewById<Button>(R.id.btnAbrirEnEditor)
        val btnAbrirYGenerarPKM = findViewById<Button>(R.id.btnAbrirYGenerarPKM)

        btnAbrirEnEditor.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            abrirParaEditorLauncher.launch(intent)
        }

        btnAbrirYGenerarPKM.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            android.widget.Toast.makeText(this, "Selecciona un archivo .pkm", android.widget.Toast.LENGTH_LONG).show()
            abrirParaRenderLauncher.launch(intent)
        }

        btnGuardar.setOnClickListener {
            val codigoEntrada = editorCodigo.text.toString()

            if (codigoEntrada.isBlank()) {
                Toast.makeText(this, "El editor está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inputNombre = EditText(this).apply {
                hint = "Nombre del archivo (sin extensión)"
                setPadding(50, 40, 50, 40)
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Guardar Formulario")
                .setMessage("Ingresa el nombre para tu archivo .pkm:")
                .setView(inputNombre)
                .setPositiveButton("Guardar") { _, _ ->
                    val nombreIngresado = inputNombre.text.toString().trim()

                    if (nombreIngresado.isNotEmpty()) {
                        val nombreFinal = if (nombreIngresado.endsWith(".pkm")) {
                            nombreIngresado
                        } else {
                            "$nombreIngresado.pkm"
                        }

                        procesarYGuardar(nombreFinal, codigoEntrada)
                    } else {
                        Toast.makeText(this, "Debes ingresar un nombre válido", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        guardarArchivoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    escribirContenidoEnUri(uri, contenidoPendientePorGuardar)
                }
            }
        }

        btnAnalizar.setOnClickListener {
            val codigoEntrada = editorCodigo.text.toString()

            if (codigoEntrada.isBlank()) {
                Toast.makeText(this, "El editor está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Analizando...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {

                val analizador = Analizador(codigoEntrada)

                analizador.analizarFormulario()

                withContext(Dispatchers.Main) {
                    val contenedor = findViewById<LinearLayout>(R.id.contenedorFormulario)

                    if (analizador.reporteErrores.isEmpty()) {

                        analizador.astFormulario?.let { arbol ->
                            Toast.makeText(this@MainActivity, "Listo con ${arbol.size} instrucciones", Toast.LENGTH_SHORT).show()

                            try {
                                contenedor.removeAllViews()

                                val generador = GeneradorDeFormularios(this@MainActivity, contenedor)
                                generador.ejecutarAST(arbol)

                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Error al dibujar: ${e.message}", Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            }

                        } ?: run {
                            Toast.makeText(this@MainActivity, " El análisis fue exitoso, pero el árbol llegó vacío (Null)", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(this@MainActivity, "Hay errores léxicos o sintácticos.", Toast.LENGTH_LONG).show()
                        contenedor.removeAllViews()
                        ultimoReporteHtml = analizador.reporteErrores
                    }
                }
            }
        }
        btnReportes.setOnClickListener {
            if (ultimoReporteHtml.isNotEmpty()) {

                val webView = WebView(this)
                webView.loadDataWithBaseURL(null, ultimoReporteHtml, "text/html", "UTF-8", null)

                android.app.AlertDialog.Builder(this)
                    .setTitle("Reporte de Análisis")
                    .setView(webView)
                    .setPositiveButton("Cerrar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            } else {
                Toast.makeText(this, "Primero debes analizar un código para ver el reporte o no hay errores.", Toast.LENGTH_LONG).show()
            }
        }
        btnContestar.setOnClickListener {

            Toast.makeText(this, "Recolectando respuestas...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escribirContenidoEnUri(uri: Uri, contenido: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(contenido.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Archivo guardado exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun leerArchivoParaEditor(uri: android.net.Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val texto = inputStream?.bufferedReader().use { it?.readText() } ?: ""

                withContext(Dispatchers.Main) {
                    editorCodigo.setText(texto)
                    Toast.makeText(this@MainActivity, "Archivo cargado en el editor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun leerArchivoParaRenderizarPKM(uri: android.net.Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val contenidoPkm = inputStream?.bufferedReader().use { it?.readText() } ?: ""

                withContext(Dispatchers.Main) {
                    if (!contenidoPkm.contains("<section=") && !contenidoPkm.contains("###")) {
                        android.widget.Toast.makeText(this@MainActivity, "El archivo seleccionado no parece ser un .pkm válido.", android.widget.Toast.LENGTH_LONG).show()
                        return@withContext
                    }

                    android.widget.Toast.makeText(this@MainActivity, "Analizando PKM...", android.widget.Toast.LENGTH_SHORT).show()

                    try {

                        val stringReader = java.io.StringReader(contenidoPkm)

                        val lexerPkm = com.example.compiladoresapp.uitheme.logic.LexerPKM(stringReader)
                        val parserPkm = com.example.compiladoresapp.uitheme.logic.ParserPKM(lexerPkm)

                        val resultadoParseo = parserPkm.parse()


                        val erroresLexicos = lexerPkm.errores
                        val erroresSintacticos = parserPkm.erroresSintacticos

                        val archivoManipulado = erroresLexicos.isNotEmpty() || erroresSintacticos.isNotEmpty()

                        if (archivoManipulado) {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                " Error: El archivo .pkm ha sido manipulado a mano y es inválido.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                " PKM verificado intacto. Generando interfaz...",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            contenedorFormulario.removeAllViews()

                            val astPkm = resultadoParseo.value as? List<NodoPKM>

                            if (astPkm != null) {
                                val generadorPkmVisual = GeneradorFormulariosPKM(this@MainActivity, contenedorFormulario)
                                generadorPkmVisual.ejecutarAST(astPkm)
                            } else {
                                android.widget.Toast.makeText(this@MainActivity, "El archivo está vacío.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Error sintáctico crítico: Archivo PKM corrupto.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Error crítico al intentar leer el archivo desde el teléfono",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun procesarYGuardar(nombreSugerido: String, codigoEntrada: String) {
        val esPKM = codigoEntrada.trim().startsWith("###") || codigoEntrada.contains("<section=")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var textoAFisico = ""

                if (esPKM) {
                    val analizador = Analizador(codigoEntrada)
                    analizador.analizarPKM()

                    if (analizador.reporteErrores.isEmpty()) {
                        textoAFisico = codigoEntrada
                    } else {
                        Toast.makeText(this@MainActivity, "Hay errores léxicos o sintácticos.", Toast.LENGTH_LONG).show()
                        ultimoReporteHtml = analizador.reporteErrores
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
                        Toast.makeText(this@MainActivity, "Hay errores léxicos o sintácticos.", Toast.LENGTH_LONG).show()
                        ultimoReporteHtml = analizador.reporteErrores
                        return@launch
                    }
                }

                withContext(Dispatchers.Main) {
                    contenidoPendientePorGuardar = textoAFisico
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/octet-stream" // O "text/plain"
                        putExtra(Intent.EXTRA_TITLE, nombreSugerido)
                    }
                    guardarArchivoLauncher.launch(intent)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}