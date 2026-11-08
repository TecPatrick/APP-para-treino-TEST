package com.example.util

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader

data class ExerciseVisualGuide(
    val exerciseName: String,
    val muscleGroup: String,
    val fitnessProgrammerUrl: String,
    val gifUrl: String,
    val concentricPhase: String,
    val eccentricPhase: String,
    val postureTip: String
)

/** Centralized primary-muscle classification used by every workout catalog/filter. */
object ExerciseMuscleClassifier {
    fun matches(guide: ExerciseVisualGuide, filter: String): Boolean {
        if (filter == "Todos" || filter == "⭐ Populares") return true
        val name = guide.exerciseName.lowercase()
        val muscle = guide.muscleGroup.lowercase()
        fun hasAny(text: String, vararg terms: String) = terms.any { text.contains(it) }
        return when (filter) {
            "Tríceps" -> muscle.startsWith("tríceps") || muscle.startsWith("triceps") || hasAny(name, "tríceps", "triceps", "pushdown", "skull crusher")
            "Bíceps" -> muscle.startsWith("bíceps") || muscle.startsWith("biceps") || hasAny(name, "rosca", "curl")
            "Peitoral" -> (muscle.startsWith("peitoral") || muscle.startsWith("chest") || hasAny(name, "supino", "bench press", "crossover", "pec deck", "push-up", "flexão", "pullover")) && !hasAny(name, "inverso", "reverse fly", "rear delt")
            "Dorsal" -> muscle.startsWith("dorsal") || muscle.startsWith("costas") || muscle.startsWith("latíssimo") || hasAny(name, "puxada", "pulldown", "remada", " row", "barra fixa", "pull-up", "pull up")
            "Deltoide" -> muscle.startsWith("deltoide") || muscle.startsWith("ombro") || muscle.startsWith("shoulder") || hasAny(name, "desenvolvimento", "elevação lateral", "elevação frontal", "lateral raise", "front raise", "face pull", "overhead press", "military press", "shoulder press", "arnold press", "rear delt")
            "Quadríceps" -> muscle.startsWith("quadríceps") || muscle.startsWith("quadriceps") || hasAny(name, "agachamento", "squat", "leg press", "extensora", "leg extension", "hack squat", "afundo", "lunge")
            "Posterior / Glúteos" -> muscle.startsWith("isquiotibiais") || muscle.startsWith("posterior") || muscle.startsWith("glúteo") || muscle.startsWith("glute") || muscle.startsWith("cadeia posterior") || hasAny(name, "flexora", "leg curl", "stiff", "rdl", "romanian deadlift", "hip thrust", "elevação pélvica", "good morning")
            "Panturrilha" -> muscle.startsWith("panturrilha") || muscle.startsWith("gastrocnêmio") || muscle.startsWith("calf") || hasAny(name, "panturrilha", "calf raise")
            "Antebraço" -> muscle.startsWith("antebraço") || muscle.startsWith("forearm") || hasAny(name, "wrist", "punho", "rosca inversa")
            "Trapézio" -> muscle.startsWith("trapézio") || muscle.startsWith("trapezius") || hasAny(name, "encolhimento", "shrug")
            "Abdômen" -> muscle.startsWith("abdominal") || muscle.startsWith("abdômen") || muscle.startsWith("core") || hasAny(name, "abdominal", "crunch", "plank", "prancha")
            else -> false
        }
    }

    fun primaryFilter(guide: ExerciseVisualGuide): String =
        listOf("Tríceps", "Bíceps", "Peitoral", "Dorsal", "Deltoide", "Quadríceps", "Posterior / Glúteos", "Panturrilha", "Antebraço", "Trapézio", "Abdômen")
            .firstOrNull { matches(guide, it) } ?: "Todos"
}

object ExerciseGifMapper {

    private var applicationContext: Context? = null
    private var cachedCatalogGuides: List<ExerciseVisualGuide>? = null
    private var cachedFullLibrary: List<ExerciseVisualGuide>? = null
    private var cachedLibraryByName: Map<String, ExerciseVisualGuide>? = null
    private var cachedDirectMap: Map<String, ExerciseVisualGuide>? = null

    fun init(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
            cachedCatalogGuides = null
            cachedDirectMap = null
            loadLibraryFromAssets()
        }
    }

    private fun openAssetOrFallback(fileName: String): InputStream? {
        applicationContext?.let { ctx ->
            runCatching { return ctx.assets.open(fileName) }
        }
        val candidates = listOf(
            File("src/main/assets/$fileName"),
            File("app/src/main/assets/$fileName")
        )
        for (file in candidates) {
            if (file.exists()) {
                return FileInputStream(file)
            }
        }
        return ExerciseGifMapper::class.java.classLoader?.getResourceAsStream("assets/$fileName")
    }

    private val catalogGuides: List<ExerciseVisualGuide>
        get() {
            cachedCatalogGuides?.let { if (it.isNotEmpty()) return it }
            val loaded = mutableListOf<ExerciseVisualGuide>()
            try {
                val stream = openAssetOrFallback("canonical_exercise_guides.json")
                if (stream != null) {
                    val jsonString = BufferedReader(InputStreamReader(stream)).use { it.readText() }
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        loaded.add(
                            ExerciseVisualGuide(
                                exerciseName = obj.optString("exerciseName", ""),
                                muscleGroup = obj.optString("muscleGroup", ""),
                                fitnessProgrammerUrl = obj.optString("fitnessProgrammerUrl", ""),
                                gifUrl = obj.optString("gifUrl", ""),
                                concentricPhase = obj.optString("concentricPhase", ""),
                                eccentricPhase = obj.optString("eccentricPhase", ""),
                                postureTip = obj.optString("postureTip", "")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (loaded.isNotEmpty()) {
                cachedCatalogGuides = loaded
            }
            return loaded
        }

    private fun loadLibraryFromAssets(): List<ExerciseVisualGuide> {
        cachedFullLibrary?.let { return it }
        val baseCatalog = catalogGuides

        try {
            val inputStream = openAssetOrFallback("fitnessprogramer_exercises.json") ?: return baseCatalog
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            val loadedList = mutableListOf<ExerciseVisualGuide>()
            val loadedMap = mutableMapOf<String, ExerciseVisualGuide>()

            for (guide in baseCatalog) {
                loadedList.add(guide)
                loadedMap[guide.exerciseName.lowercase().trim()] = guide
            }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val enName = obj.optString("exerciseName", "").trim()
                if (enName.isEmpty()) continue

                val lowerEn = enName.lowercase()
                if (loadedMap.containsKey(lowerEn)) continue

                val muscleGroup = obj.optString("muscleGroup", "Geral")
                val pageUrl = obj.optString("fitnessProgrammerUrl", "")
                val gifUrl = obj.optString("gifUrl", "")

                val guide = ExerciseVisualGuide(
                    exerciseName = enName,
                    muscleGroup = muscleGroup,
                    fitnessProgrammerUrl = pageUrl,
                    gifUrl = gifUrl,
                    concentricPhase = "Fase Concêntrica: Realize a contração vencendo a resistência de forma controlada.",
                    eccentricPhase = "Fase Excêntrica: Controle a volta do movimento (descida da carga) em 2 a 3 segundos.",
                    postureTip = "Mantenha a postura alinhada, core ativado e amplitudes seguras para as articulações."
                )

                loadedList.add(guide)
                loadedMap[lowerEn] = guide
            }

            cachedFullLibrary = loadedList
            cachedLibraryByName = loadedMap
            return loadedList
        } catch (e: Exception) {
            e.printStackTrace()
            return baseCatalog
        }
    }

    private val directMap: Map<String, ExerciseVisualGuide>
        get() {
            cachedDirectMap?.let { if (it.isNotEmpty()) return it }
            val guides = catalogGuides
            if (guides.isEmpty()) return emptyMap()

            val map = mutableMapOf<String, ExerciseVisualGuide>()
            guides.forEach { guide ->
                map[guide.exerciseName] = guide
                map[guide.exerciseName.lowercase()] = guide
            }

            fun registerAliases(canonicalName: String, vararg aliases: String) {
                val guide = guides.firstOrNull { it.exerciseName == canonicalName } ?: return
                aliases.forEach { alias ->
                    map[alias] = guide
                    map[alias.lowercase()] = guide
                }
            }

            // Aliases para Tríceps
            registerAliases(
                "Tríceps Pulley Corda",
                "Tríceps Corda", "Tríceps no Pulley", "Tríceps Pulley", "Tríceps Polia",
                "Tríceps Pulley Barra", "Tríceps Pulley Barra W", "Triceps pulley barra w"
            )
            registerAliases(
                "Tríceps Francês",
                "Tríceps Francês com Halter", "Tríceps Frances", "Tríceps Frances com Halter",
                "Frances Triceps", "Tríceps Francês na Polia Barra W", "triceps frances na polia barra W"
            )
            registerAliases(
                "Tríceps Testa com Barra",
                "Tríceps Testa", "Tríceps Testa com Halteres", "Tríceps Testa Barra W",
                "Tríceps Testa com Barra W", "Skullcrusher", "Skull Crusher",
                "Tríceps Testa na Polia", "Triceps testa na polia"
            )
            registerAliases(
                "Tríceps Coice com Halter",
                "Tríceps Coice", "Tríceps Kickback", "Coice Tríceps"
            )
            registerAliases(
                "Tríceps Mergulho no Banco",
                "Tríceps Mergulho", "Tríceps Banco", "Mergulho no Banco", "Mergulho Tríceps"
            )
            registerAliases(
                "Tríceps nas Paralelas",
                "Tríceps Paralelas", "Paralelas", "Triceps Dips"
            )
            registerAliases(
                "Tríceps Supinado (Pegada Fechada)",
                "Tríceps Supinado", "Supino Fechado", "Supino Pegada Fechada"
            )

            // Aliases para Peitoral
            registerAliases(
                "Supino Reto com Barra",
                "Supino Reto", "Supino Reto com Halteres", "Supino Reto Barra"
            )
            registerAliases(
                "Supino Inclinado com Halteres",
                "Supino Inclinado", "Supino Inclinado com Barra", "Supino Inclinado com Halteres (30°)",
                "Supino Inclinado 30°", "Supino Inclinado 30"
            )
            registerAliases(
                "Crucifixo no Cross / Crossover",
                "Crossover", "Cross Over", "Crucifixo no Cross", "Crucifixo Polia Alta", "Cruxifixo polia alta"
            )
            registerAliases(
                "Voador / Peck Deck",
                "Voador", "Peck Deck", "Peck Deck Fly", "Crucifixo Máquina", "Cruxufixo maquina"
            )

            // Aliases para Costas
            registerAliases(
                "Puxada Alta no Pulley",
                "Puxada Alta", "Puxada Alta Aberta no Pulley", "Puxada Frontal", "Puxador Alto",
                "Puxada Alta Pronada na Polia", "puxada alta pronada na polia", "Puxada Triângulo", "puxada triangulo"
            )
            registerAliases("Remada Curvada com Barra", "Remada Curvada")
            registerAliases("Remada Baixa no Cabo", "Remada Baixa", "Remada Sentada")
            registerAliases(
                "Remada Unilateral com Halter (Serrote)",
                "Serrote", "Remada Serrote", "Remada Unilateral"
            )

            // Aliases para Bíceps
            registerAliases(
                "Rosca Direta com Barra",
                "Rosca Direta", "Rosca Direta com Barra W", "rosca direta com barra W"
            )
            registerAliases(
                "Rosca Martelo com Halteres",
                "Rosca Martelo", "Rosca Martelo Alternada", "Rosca martelo Alternada"
            )
            registerAliases("Rosca Scott com Halter", "Rosca Scott", "Rosca Scott na Máquina")
            registerAliases(
                "Rosca Concentrada com Halter",
                "Rosca Concentrada", "Rosca Polia Alta Unilateral", "Rosca polia alta unilateral"
            )
            registerAliases("Rosca Inversa com Barra", "Rosca Inversa Barra W", "Rosca Inversa barra W")

            // Aliases para Pernas
            registerAliases("Agachamento Livre com Barra", "Agachamento Livre", "Agachamento")
            registerAliases("Leg Press 45°", "Leg Press", "Leg Press 45", "legpress 45")
            registerAliases("Cadeira Extensora", "Extensora", "cadeira extensora")
            registerAliases("Mesa Flexora", "Flexora")
            registerAliases("Cadeira Flexora", "Flexora Sentada", "cadeira flexora")
            registerAliases(
                "Panturrilha em Pé",
                "Panturrilha no Smith em Pé", "Panturrilha", "Panturrilha em Pé no Smith"
            )
            registerAliases("Cadeira Abdutora", "Abdutora")
            registerAliases("Cadeira Adutora", "adutora")
            registerAliases("Extensora em Pé", "extensora em pe")
            registerAliases("Elevação Pélvica com Barra", "Elevação Pélvica", "elevação pélvica")
            registerAliases("Agachamento Hack", "Agachamento Hack 45°", "agachemento hack 45")

            cachedDirectMap = map
            return map
        }

    fun getAllAvailableExercises(): List<ExerciseVisualGuide> {
        val loaded = cachedFullLibrary ?: loadLibraryFromAssets()
        return if (loaded.isNotEmpty()) loaded else catalogGuides
    }

    fun getGuideForExercise(exerciseName: String): ExerciseVisualGuide {
        val map = directMap
        map[exerciseName]?.let { return it }
        map[exerciseName.lowercase()]?.let { return it }

        val clean = exerciseName.lowercase().trim()
        cachedLibraryByName?.get(clean)?.let { return it }

        val isTriceps = clean.contains("tríceps") || clean.contains("triceps") || clean.contains("tricep")
        if (isTriceps) {
            val targetKey = when {
                clean.contains("corda") || clean.contains("polia") || clean.contains("pulley") -> "Tríceps Pulley Corda"
                clean.contains("franc") -> "Tríceps Francês"
                clean.contains("testa") || clean.contains("skull") -> "Tríceps Testa com Barra"
                clean.contains("coice") || clean.contains("kickback") -> "Tríceps Coice com Halter"
                clean.contains("mergulho") || clean.contains("banco") -> "Tríceps Mergulho no Banco"
                clean.contains("paralela") || clean.contains("dip") -> "Tríceps nas Paralelas"
                clean.contains("supinado") || clean.contains("fechado") -> "Tríceps Supinado (Pegada Fechada)"
                else -> "Tríceps Pulley Corda"
            }
            map[targetKey]?.let { return it }
        }

        if (clean.contains("francês") || clean.contains("frances")) map["Tríceps Francês"]?.let { return it }
        if (clean.contains("testa")) map["Tríceps Testa com Barra"]?.let { return it }
        if (clean.contains("coice")) map["Tríceps Coice com Halter"]?.let { return it }
        if (clean.contains("mergulho")) map["Tríceps Mergulho no Banco"]?.let { return it }
        if (clean.contains("paralela")) map["Tríceps nas Paralelas"]?.let { return it }

        if (clean.contains("supino")) {
            val key = if (clean.contains("inclinado")) "Supino Inclinado com Halteres" else "Supino Reto com Barra"
            map[key]?.let { return it }
        }
        if (clean.contains("cross") || clean.contains("crossover")) map["Crucifixo no Cross / Crossover"]?.let { return it }
        if (clean.contains("voador") || clean.contains("peck")) map["Voador / Peck Deck"]?.let { return it }
        if (clean.contains("crucifixo")) {
            val key = if (clean.contains("inverso")) "Crucifixo Inverso com Halteres" else "Crucifixo Reto com Halteres"
            map[key]?.let { return it }
        }

        if (clean.contains("puxada") || clean.contains("puxador")) map["Puxada Alta no Pulley"]?.let { return it }
        if (clean.contains("serrote")) map["Remada Unilateral com Halter (Serrote)"]?.let { return it }
        if (clean.contains("remada")) {
            val key = when {
                clean.contains("baixa") || clean.contains("cabo") -> "Remada Baixa no Cabo"
                clean.contains("unilateral") -> "Remada Unilateral com Halter (Serrote)"
                else -> "Remada Curvada com Barra"
            }
            map[key]?.let { return it }
        }
        if (clean.contains("barra fixa") || clean.contains("pull up") || clean.contains("pull-up")) map["Barra Fixa (Pull-up)"]?.let { return it }

        if (clean.contains("desenvolvimento")) map["Desenvolvimento com Halteres"]?.let { return it }
        if (clean.contains("elevação lateral") || clean.contains("elevacao lateral") || clean.contains("lateral")) map["Elevação Lateral com Halteres"]?.let { return it }
        if (clean.contains("face pull") || clean.contains("facepull")) map["Face Pull na Polia"]?.let { return it }

        if (clean.contains("martelo")) map["Rosca Martelo com Halteres"]?.let { return it }
        if (clean.contains("scott")) map["Rosca Scott com Halter"]?.let { return it }
        if (clean.contains("concentrad")) map["Rosca Concentrada com Halter"]?.let { return it }
        if (clean.contains("rosca") || clean.contains("bíceps") || clean.contains("biceps")) map["Rosca Direta com Barra"]?.let { return it }

        if (clean.contains("agachamento") || clean.contains("squat")) map["Agachamento Livre com Barra"]?.let { return it }
        if (clean.contains("leg press") || clean.contains("leg")) map["Leg Press 45°"]?.let { return it }
        if (clean.contains("extensora")) map["Cadeira Extensora"]?.let { return it }
        if (clean.contains("flexora")) {
            val key = if (clean.contains("cadeira") || clean.contains("sentad")) "Cadeira Flexora" else "Mesa Flexora"
            map[key]?.let { return it }
        }
        if (clean.contains("stiff") || clean.contains("rdl")) map["Stiff com Barra / RDL"]?.let { return it }
        if (clean.contains("pélvica") || clean.contains("pelvica") || clean.contains("hip thrust")) map["Elevação Pélvica com Barra"]?.let { return it }
        if (clean.contains("afundo") || clean.contains("passada") || clean.contains("lunge")) map["Afundo / Passada com Halteres"]?.let { return it }
        if (clean.contains("panturrilha") || clean.contains("gemeos") || clean.contains("gêmeos")) map["Panturrilha em Pé"]?.let { return it }

        catalogGuides.firstOrNull { guide ->
            val guideLower = guide.exerciseName.lowercase()
            clean.contains(guideLower) || guideLower.contains(clean)
        }?.let { return it }

        getAllAvailableExercises().firstOrNull { guide ->
            val guideLower = guide.exerciseName.lowercase()
            clean.contains(guideLower) || guideLower.contains(clean)
        }?.let { return it }

        val slug = clean
            .replace("ã", "a").replace("á", "a").replace("â", "a")
            .replace("é", "e").replace("ê", "e")
            .replace("í", "i").replace("ó", "o").replace("ô", "o")
            .replace("ú", "u").replace("ç", "c")
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
            .replace(Regex("\\s+"), "-")

        return ExerciseVisualGuide(
            exerciseName = exerciseName,
            muscleGroup = "Hipertrofia & Definição",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/$slug/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif",
            concentricPhase = "Fase Concêntrica: Puxe/empurre a carga com máxima intenção e foco no músculo alvo.",
            eccentricPhase = "Fase Excêntrica: Controle a descida em 2 a 3 segundos para maximizar microlesões celulares.",
            postureTip = "Mantenha a postura anatômica correta, articulações protegidas e estabilização de core."
        )
    }

    fun getFavoriteAndTopExercises(): List<ExerciseVisualGuide> {
        return catalogGuides
    }
}
