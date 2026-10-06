package com.example.util

data class ExerciseVisualGuide(
    val exerciseName: String,
    val muscleGroup: String,
    val fitnessProgrammerUrl: String,
    val gifUrl: String,
    val concentricPhase: String,
    val eccentricPhase: String,
    val postureTip: String
)

object ExerciseGifMapper {

    private val guidesMap = mapOf(
        "Supino Reto com Barra" to ExerciseVisualGuide(
            exerciseName = "Supino Reto com Barra",
            muscleGroup = "Peitoral Maior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-bench-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif",
            concentricPhase = "Empurre a barra para cima expirando o ar e contraindo o peitoral.",
            eccentricPhase = "Desça a barra até a linha do esterno de forma controlada em 2 a 3 segundos.",
            postureTip = "Escápulas aduzidas (retraídas) contra o banco e pés firmes no chão."
        ),
        "Supino Inclinado com Halteres (30°)" to ExerciseVisualGuide(
            exerciseName = "Supino Inclinado com Halteres (30°)",
            muscleGroup = "Peitoral Superior & Deltoide",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/incline-dumbbell-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif",
            concentricPhase = "Eleve os halteres em direção ao teto até quase se tocarem no topo.",
            eccentricPhase = "Desça com cotovelos em ângulo de 45 graus para focar na porção clavicular.",
            postureTip = "Ajuste o banco entre 30° e 45° para não sobrecarregar o ombro."
        ),
        "Supino Inclinado com Halteres" to ExerciseVisualGuide(
            exerciseName = "Supino Inclinado com Halteres",
            muscleGroup = "Peitoral Superior & Deltoide",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/incline-dumbbell-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif",
            concentricPhase = "Eleve os halteres em direção ao teto até quase se tocarem no topo.",
            eccentricPhase = "Desça com cotovelos em ângulo de 45 graus para focar na porção clavicular.",
            postureTip = "Ajuste o banco entre 30° e 45° para não sobrecarregar o ombro."
        ),
        "Crucifixo no Cross / Crossover" to ExerciseVisualGuide(
            exerciseName = "Crucifixo no Cross / Crossover",
            muscleGroup = "Peitoral Médio e Inferior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/cable-crossover/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Cable-Crossover.gif",
            concentricPhase = "Aproxime as polias no centro do peito cruzando levemente as mãos.",
            eccentricPhase = "Retorne abrindo os braços sentindo o peitoral alongar no limite seguro.",
            postureTip = "Mantenha o tronco levemente inclinado à frente e cotovelos semidobrados."
        ),
        "Desenvolvimento com Halteres" to ExerciseVisualGuide(
            exerciseName = "Desenvolvimento com Halteres",
            muscleGroup = "Deltoide Anterior e Lateral",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-shoulder-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif",
            concentricPhase = "Empurre os halteres para cima estendendo os braços verticalmente.",
            eccentricPhase = "Desça os halteres até a linha das orelhas sob estrito controle.",
            postureTip = "Mantenha a coluna ereta e não jogue a cabeça para a frente."
        ),
        "Puxada Alta Aberta no Pulley" to ExerciseVisualGuide(
            exerciseName = "Puxada Alta Aberta no Pulley",
            muscleGroup = "Dorsal e Latíssimo",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/lat-pulldown/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif",
            concentricPhase = "Puxe a barra em direção ao peito superior abaixando as escápulas.",
            eccentricPhase = "Suba a barra devagar até alongar completamente a asa (dorsal).",
            postureTip = "Evite balançar o tronco para trás; foque em puxar com os cotovelos."
        ),
        "Remada Curvada com Barra" to ExerciseVisualGuide(
            exerciseName = "Remada Curvada com Barra",
            muscleGroup = "Dorsal, Miolo das Costas",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-bent-over-row/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif",
            concentricPhase = "Puxe a barra em direção ao umbigo espremendo as escápulas atrás.",
            eccentricPhase = "Alongue os braços completamente mantendo a coluna neutra.",
            postureTip = "Incline o tronco a aproximadamente 45° com joelhos levemente flexionados."
        ),
        "Crucifixo Inverso no Voador" to ExerciseVisualGuide(
            exerciseName = "Crucifixo Inverso no Voador",
            muscleGroup = "Deltoide Posterior & Trapézio",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/rear-delt-fly/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Rear-Delt-Fly.gif",
            concentricPhase = "Afaste as pegadas para trás contraindo a porção posterior do ombro.",
            eccentricPhase = "Retorne aproximando as mãos à frente sem perder a tensão.",
            postureTip = "Mantenha os cotovelos na altura dos ombros e peito apoiado no encosto."
        ),
        "Agachamento Livre com Barra" to ExerciseVisualGuide(
            exerciseName = "Agachamento Livre com Barra",
            muscleGroup = "Quadríceps, Glúteos & Core",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-squat/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif",
            concentricPhase = "Empurre o chão com os calcanhares e suba mantendo o peito erguido.",
            eccentricPhase = "Flexione joelhos e quadril descendo abaixo do ângulo de 90°.",
            postureTip = "Trave a respiração diafragmática (Valsalva) e joelhos acompanham os pés."
        ),
        "Leg Press 45°" to ExerciseVisualGuide(
            exerciseName = "Leg Press 45°",
            muscleGroup = "Quadríceps & Glúteos",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/leg-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Press.gif",
            concentricPhase = "Empurre a plataforma sem travar (hiperextender) os joelhos no topo.",
            eccentricPhase = "Desça a plataforma até formar 90° mantendo o quadril colado no encosto.",
            postureTip = "Nunca descole a região lombar da plataforma para evitar lesão na coluna."
        ),
        "Cadeira Extensora" to ExerciseVisualGuide(
            exerciseName = "Cadeira Extensora",
            muscleGroup = "Quadríceps Isolado",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/leg-extension/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Extension.gif",
            concentricPhase = "Extenda as pernas completamente até o topo segurando 1 segundo.",
            eccentricPhase = "Retorne lentamente à posição inicial sob constante tensão.",
            postureTip = "Ajuste o rolo do tornozelo exatamente acima dos pés e joelhos alinhados ao eixo."
        ),
        "Desenvolvimento Militar com Halteres" to ExerciseVisualGuide(
            exerciseName = "Desenvolvimento Militar com Halteres",
            muscleGroup = "Deltoides (Ombros)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-shoulder-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif",
            concentricPhase = "Suba os halteres em linha reta acima da cabeça sem bater os pesos.",
            eccentricPhase = "Desça os halteres até a linha das orelhas mantendo o controle.",
            postureTip = "Mantenha o abdômen contraído e não incline o tronco excessivamente para trás."
        ),
        "Elevação Lateral com Halteres" to ExerciseVisualGuide(
            exerciseName = "Elevação Lateral com Halteres",
            muscleGroup = "Deltoide Lateral",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-lateral-raise/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Lateral-Raise.gif",
            concentricPhase = "Eleve os braços para os lados até a altura dos ombros.",
            eccentricPhase = "Desça devagar controlando a gravidade sem balançar o corpo.",
            postureTip = "Cotovelos levemente flexionados e polegares apontando discretamente para baixo."
        ),
        "Rosca Direta com Barra W" to ExerciseVisualGuide(
            exerciseName = "Rosca Direta com Barra W",
            muscleGroup = "Bíceps Braquial",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/ez-bar-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/EZ-Bar-Curl.gif",
            concentricPhase = "Flexione os cotovelos elevando a barra em direção aos ombros.",
            eccentricPhase = "Desça a barra estendendo o cotovelo totalmente em 2 a 3 segundos.",
            postureTip = "Mantenha os cotovelos colados ao lado do tronco sem roubar com o quadril."
        ),
        "Tríceps Testa com Halteres" to ExerciseVisualGuide(
            exerciseName = "Tríceps Testa com Halteres",
            muscleGroup = "Tríceps (Cabeça Longa & Medial)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-lying-triceps-extension/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Lying-Triceps-Extension.gif",
            concentricPhase = "Estenda os cotovelos empurrando os halteres para cima.",
            eccentricPhase = "Flexione os cotovelos descendo os halteres em direção às têmporas.",
            postureTip = "Mantenha os braços estáticos e paralelos; apenas os antebraços se movimentam."
        ),
        "Tríceps Pulley Corda" to ExerciseVisualGuide(
            exerciseName = "Tríceps Pulley Corda",
            muscleGroup = "Tríceps (Cabeça Lateral e Longa)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/cable-tricep-pushdown/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif",
            concentricPhase = "Empurre a corda para baixo abrindo as pontas no final da extensão.",
            eccentricPhase = "Suba as mãos até a linha do peito mantendo cotovelos parados.",
            postureTip = "Trave os cotovelos ao lado do corpo; apenas os antebraços se movimentam."
        ),
        "Stiff com Barra / RDL" to ExerciseVisualGuide(
            exerciseName = "Stiff com Barra / RDL",
            muscleGroup = "Isquiotibiais & Glúteos",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-romanian-deadlift/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Romanian-Deadlift.gif",
            concentricPhase = "Empurre o quadril para a frente usando os glúteos para ficar em pé.",
            eccentricPhase = "Projete o quadril para trás com a barra rente às pernas até sentir o posterior alongar.",
            postureTip = "Coluna perfeitamente ereta do tórax à lombar; joelhos destravados mas não dobrados."
        ),
        "Panturrilha no Smith em Pé" to ExerciseVisualGuide(
            exerciseName = "Panturrilha no Smith em Pé",
            muscleGroup = "Gastrocnêmio & Sóleo",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/standing-calf-raises/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Standing-Calf-Raise.gif",
            concentricPhase = "Suba na ponta dos pés o máximo possível contraindo a panturrilha.",
            eccentricPhase = "Desça os calcanhares abaixo da linha do degrau alongando a musculatura.",
            postureTip = "Mantenha os joelhos estendidos e corpo alinhado sem dobrar o quadril."
        )
    )

    fun getGuideForExercise(exerciseName: String): ExerciseVisualGuide {
        // Direct exact match
        guidesMap[exerciseName]?.let { return it }

        // Fuzzy search by substring or normalized key
        val cleanName = exerciseName.lowercase()
        guidesMap.entries.firstOrNull { (key, _) ->
            val cleanKey = key.lowercase()
            cleanName.contains(cleanKey) || cleanKey.contains(cleanName) ||
            (cleanName.contains("supino") && cleanName.contains("inclinado") && cleanKey.contains("inclinado")) ||
            (cleanName.contains("crossover") && cleanKey.contains("crossover")) ||
            (cleanName.contains("puxada") && cleanKey.contains("puxada")) ||
            (cleanName.contains("voador") && cleanKey.contains("voador")) ||
            (cleanName.contains("testa") && cleanKey.contains("testa")) ||
            (cleanName.contains("panturrilha") && cleanKey.contains("panturrilha"))
        }?.let { return it.value }

        // Dynamic fallback with custom slug
        val slug = exerciseName.lowercase()
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
}
