package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.MealPlanItem
import com.example.data.model.ProgressLog
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateAndShareProgressReport(
        context: Context,
        profile: UserProfile,
        mealPlans: List<MealPlanItem>,
        routines: List<WorkoutRoutine>,
        exercises: List<WorkoutExercise>,
        progressLogs: List<ProgressLog>,
        shoppingItems: List<ShoppingItem>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points (72 dpi)
        val pageHeight = 842 // Standard A4 height in PostScript points

        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        drawPageOne(canvas1, pageWidth, pageHeight, profile, mealPlans)
        pdfDocument.finishPage(page1)

        // Page 2: Workouts, Progress Evolution and Monthly Grocery List
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        drawPageTwo(canvas2, pageWidth, pageHeight, routines, exercises, progressLogs, shoppingItems)
        pdfDocument.finishPage(page2)

        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(reportsDir, "Relatorio_Dieta_Treino_$timestamp.pdf")

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            sharePdfFile(context, pdfFile)
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            pdfDocument.close()
            return null
        }
    }

    private fun drawPageOne(
        canvas: Canvas,
        width: Int,
        height: Int,
        profile: UserProfile,
        mealPlans: List<MealPlanItem>
    ) {
        val paint = Paint()
        paint.isAntiAlias = true

        // Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Header Background Banner (Orange Accent)
        paint.color = Color.parseColor("#FF5500")
        canvas.drawRect(0f, 0f, width.toFloat(), 95f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DIETA & TREINO", 30f, 42f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("RELATÓRIO MENSAL DE DESEMPENHO E PROTOCOLO NUTRICIONAL", 30f, 62f, paint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date())
        paint.textSize = 10f
        paint.color = Color.parseColor("#FFF3E0")
        canvas.drawText("Gerado em: $dateStr", 30f, 80f, paint)

        var y = 125f

        // Section 1: Perfil do Atleta & Metas
        drawSectionHeader(canvas, "1. DIAGNÓSTICO METABÓLICO & METAS DE MACROS", y)
        y += 24f

        // Card Box
        paint.color = Color.parseColor("#F4F6F8")
        canvas.drawRoundRect(30f, y, width - 30f, y + 115f, 8f, 8f, paint)

        paint.color = Color.parseColor("#111827")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col1 = 45f
        val col2 = 310f

        val bmr = MacroCalculator.calculateBMR(profile.weightKg, profile.heightCm, profile.age, profile.gender)
        val tdee = MacroCalculator.calculateTDEE(bmr, profile.activityLevel)

        canvas.drawText("Nome: ${profile.name}", col1, y + 20f, paint)
        canvas.drawText("Objetivo: ${profile.goal.label}", col2, y + 20f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Idade / Altura: ${profile.age} anos | ${profile.heightCm.toInt()} cm", col1, y + 38f, paint)
        canvas.drawText("Peso Atual / Meta: ${profile.weightKg} kg  ->  ${profile.monthlyTargetWeightKg} kg", col2, y + 38f, paint)

        canvas.drawText("Taxa Metabólica Basal (TMB): $bmr kcal", col1, y + 56f, paint)
        canvas.drawText("Gasto Energético Total (GET): $tdee kcal", col2, y + 56f, paint)

        // Highlight Macro Targets
        paint.color = Color.parseColor("#FF5500")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("META DIÁRIA: ${profile.targetCalories} kcal", col1, y + 80f, paint)
        canvas.drawText("ÁGUA: ${profile.targetWaterMl} ml/dia", col2, y + 80f, paint)

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val pGkg = String.format(Locale.US, "%.1f", profile.targetProteinGrams / profile.weightKg)
        val cGkg = String.format(Locale.US, "%.1f", profile.targetCarbsGrams / profile.weightKg)
        val fGkg = String.format(Locale.US, "%.1f", profile.targetFatGrams / profile.weightKg)
        canvas.drawText("Proteínas: ${profile.targetProteinGrams}g (${pGkg}g/kg) | Carbos: ${profile.targetCarbsGrams}g (${cGkg}g/kg) | Gorduras: ${profile.targetFatGrams}g (${fGkg}g/kg)", col1, y + 98f, paint)

        y += 140f

        // Section 2: Dieta Mensal e Distribuição de Refeições
        drawSectionHeader(canvas, "2. ESTRUTURA DA DIETA MENSAL & REFEIÇÕES DIÁRIAS", y)
        y += 24f

        // Meals grouped by type
        val groupedMeals = mealPlans.groupBy { it.mealType }.toSortedMap(compareBy { it.orderIndex })

        var totalPlanCals = 0f
        var totalPlanProt = 0f
        var totalPlanCarbs = 0f
        var totalPlanFat = 0f

        if (groupedMeals.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 10f
            canvas.drawText("Nenhuma refeição configurada.", 45f, y + 20f, paint)
            y += 40f
        } else {
            groupedMeals.forEach { (mealType, items) ->
                if (y > height - 120f) return@forEach

                // Meal Category Header
                val mealCals = items.sumOf { it.calories.toDouble() }.toFloat()
                val mealProt = items.sumOf { it.protein.toDouble() }.toFloat()
                val mealCarbs = items.sumOf { it.carbs.toDouble() }.toFloat()
                val mealFat = items.sumOf { it.fat.toDouble() }.toFloat()

                totalPlanCals += mealCals
                totalPlanProt += mealProt
                totalPlanCarbs += mealCarbs
                totalPlanFat += mealFat

                paint.color = Color.parseColor("#E2E8F0")
                canvas.drawRect(30f, y, width - 30f, y + 18f, paint)

                paint.color = Color.parseColor("#0F172A")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9.5f
                canvas.drawText("${mealType.title.uppercase()} (${mealType.defaultTime})", 38f, y + 13f, paint)

                val macroSub = "${mealCals.toInt()} kcal | P: ${mealProt.toInt()}g | C: ${mealCarbs.toInt()}g | G: ${mealFat.toInt()}g"
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.parseColor("#475569")
                canvas.drawText(macroSub, width - 220f, y + 13f, paint)

                y += 22f

                // Meal items
                items.forEach { item ->
                    if (y > height - 60f) return@forEach
                    paint.color = Color.parseColor("#1E293B")
                    paint.textSize = 9f
                    val foodText = "• ${item.foodName} (${item.amount.toInt()} ${item.unit})"
                    canvas.drawText(foodText, 45f, y + 10f, paint)

                    val itemMacros = "${item.calories.toInt()} kcal (P:${item.protein.toInt()}g C:${item.carbs.toInt()}g G:${item.fat.toInt()}g)"
                    paint.color = Color.parseColor("#64748B")
                    canvas.drawText(itemMacros, width - 210f, y + 10f, paint)

                    y += 15f
                }
                y += 6f
            }
        }

        // Totals Box
        y = (y + 6f).coerceAtMost(height - 65f)
        paint.color = Color.parseColor("#FFF3E0")
        canvas.drawRoundRect(30f, y, width - 30f, y + 28f, 6f, 6f, paint)

        paint.color = Color.parseColor("#D84315")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL PLANEJADO DA DIETA:", 40f, y + 18f, paint)
        canvas.drawText("${totalPlanCals.toInt()} kcal  |  P: ${totalPlanProt.toInt()}g  |  C: ${totalPlanCarbs.toInt()}g  |  G: ${totalPlanFat.toInt()}g", 210f, y + 18f, paint)

        // Footer Page 1
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dieta & Treino • Página 1 de 2", 30f, height - 20f, paint)
    }

    private fun drawPageTwo(
        canvas: Canvas,
        width: Int,
        height: Int,
        routines: List<WorkoutRoutine>,
        exercises: List<WorkoutExercise>,
        progressLogs: List<ProgressLog>,
        shoppingItems: List<ShoppingItem>
    ) {
        val paint = Paint()
        paint.isAntiAlias = true

        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Header strip
        paint.color = Color.parseColor("#1E293B")
        canvas.drawRect(0f, 0f, width.toFloat(), 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PROTOCOLO DE TREINOS & LISTA DE COMPRAS", 30f, 26f, paint)

        var y = 60f

        // Section 3: Planejamento de Treinos
        drawSectionHeader(canvas, "3. DIVISÃO & FICHA DE TREINOS SEMANAL", y)
        y += 20f

        val exercisesByRoutine = exercises.groupBy { it.routineId }

        routines.take(3).forEach { routine ->
            if (y > 330f) return@forEach

            paint.color = Color.parseColor("#FF5500")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText("TREINO ${routine.letter}: ${routine.title} (${routine.dayOfWeekLabel})", 35f, y + 12f, paint)
            y += 18f

            val rExercises = exercisesByRoutine[routine.id] ?: emptyList()
            rExercises.take(5).forEach { ex ->
                paint.color = Color.parseColor("#1E293B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                val exStr = "• ${ex.exerciseName}: ${ex.sets} séries x ${ex.repsMin}-${ex.repsMax} reps (${ex.currentWeightKg.toInt()}kg) - Descanso: ${ex.restSeconds}s"
                canvas.drawText(exStr, 45f, y + 10f, paint)
                y += 14f
            }
            y += 6f
        }

        y += 10f

        // Section 4: Histórico de Evolução Antropométrica
        drawSectionHeader(canvas, "4. HISTÓRICO DE EVOLUÇÃO DE PESO & MEDIDAS", y)
        y += 20f

        // Table Header
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, y, width - 30f, y + 16f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Data", 38f, y + 12f, paint)
        canvas.drawText("Peso (kg)", 110f, y + 12f, paint)
        canvas.drawText("BF (%)", 175f, y + 12f, paint)
        canvas.drawText("Tórax", 230f, y + 12f, paint)
        canvas.drawText("Cintura", 285f, y + 12f, paint)
        canvas.drawText("Braço", 345f, y + 12f, paint)
        canvas.drawText("Observações", 410f, y + 12f, paint)

        y += 18f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val recentLogs = progressLogs.sortedByDescending { it.dateMillis }.take(5)
        recentLogs.forEach { log ->
            paint.color = Color.parseColor("#334155")
            paint.textSize = 8f
            canvas.drawText(log.dateDisplay, 38f, y + 10f, paint)
            canvas.drawText("${log.weightKg} kg", 110f, y + 10f, paint)
            canvas.drawText(if (log.bodyFatPercentage > 0) "${log.bodyFatPercentage}%" else "--", 175f, y + 10f, paint)
            canvas.drawText(if (log.chestCm > 0) "${log.chestCm} cm" else "--", 230f, y + 10f, paint)
            canvas.drawText(if (log.waistCm > 0) "${log.waistCm} cm" else "--", 285f, y + 10f, paint)
            canvas.drawText(if (log.armRightCm > 0) "${log.armRightCm} cm" else "--", 345f, y + 10f, paint)

            val noteTruncated = if (log.notes.length > 28) log.notes.substring(0, 25) + "..." else log.notes
            canvas.drawText(noteTruncated.ifEmpty { "Sem obs" }, 410f, y + 10f, paint)

            y += 14f
        }

        y += 12f

        // Section 5: Lista de Compras Automática Mensal
        drawSectionHeader(canvas, "5. ESTIMATIVA DE LISTA DE COMPRAS (MENSAL)", y)
        y += 20f

        val displayShopping = shoppingItems.take(8)
        if (displayShopping.isEmpty()) {
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            canvas.drawText("Gere sua lista de compras automática na aba de compras.", 45f, y + 10f, paint)
            y += 25f
        } else {
            // Draw 2 column list
            var itemIdx = 0
            displayShopping.forEach { item ->
                if (y > height - 70f) return@forEach
                val isLeft = itemIdx % 2 == 0
                val colX = if (isLeft) 40f else 300f
                val colY = y

                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val qtyStr = "[ ] ${item.name}: ${item.quantity} ${item.unit}"
                canvas.drawText(qtyStr, colX, colY + 10f, paint)

                if (!isLeft) {
                    y += 15f
                }
                itemIdx++
            }
            if (itemIdx % 2 != 0) y += 15f
        }

        // Footer Page 2
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dieta & Treino • Documento Oficial de Acompanhamento • Página 2 de 2", 30f, height - 20f, paint)
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float) {
        val paint = Paint()
        paint.isAntiAlias = true

        // Little orange rectangle pill
        paint.color = Color.parseColor("#FF5500")
        canvas.drawRoundRect(30f, y - 2f, 35f, y + 14f, 2f, 2f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, 42f, y + 10f, paint)
    }

    private fun sharePdfFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório de Dieta e Treino")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo meu relatório completo de macros, dieta, treinos e evolução.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Exportar / Compartilhar Relatório PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF gerado: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
