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

    // Lista canônica de exercícios da biblioteca (exibida no catálogo e nos filtros)
    private val catalogGuides = listOf(
        // === PEITORAL ===
        ExerciseVisualGuide(
            exerciseName = "Supino Reto com Barra",
            muscleGroup = "Peitoral Maior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-bench-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif",
            concentricPhase = "Empurre a barra para cima expirando o ar e contraindo o peitoral.",
            eccentricPhase = "Desça a barra até a linha do esterno de forma controlada em 2 a 3 segundos.",
            postureTip = "Escápulas aduzidas (retraídas) contra o banco e pés firmes no chão."
        ),
        ExerciseVisualGuide(
            exerciseName = "Supino Inclinado com Halteres",
            muscleGroup = "Peitoral Superior & Deltoide",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/incline-dumbbell-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif",
            concentricPhase = "Eleve os halteres em direção ao teto até quase se tocarem no topo.",
            eccentricPhase = "Desça com cotovelos em ângulo de 45 graus para focar na porção clavicular.",
            postureTip = "Ajuste o banco entre 30° e 45° para não sobrecarregar a articulação do ombro."
        ),
        ExerciseVisualGuide(
            exerciseName = "Crucifixo no Cross / Crossover",
            muscleGroup = "Peitoral Médio e Inferior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/cable-crossover/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Cable-Crossover.gif",
            concentricPhase = "Aproxime as polias no centro do peito cruzando levemente as mãos.",
            eccentricPhase = "Retorne abrindo os braços sentindo o peitoral alongar no limite seguro.",
            postureTip = "Mantenha o tronco levemente inclinado à frente e cotovelos semidobrados."
        ),
        ExerciseVisualGuide(
            exerciseName = "Crucifixo Reto com Halteres",
            muscleGroup = "Peitoral Maior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-fly/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Fly.gif",
            concentricPhase = "Aproxime os halteres no centro acima do peito com braços em arco.",
            eccentricPhase = "Abra os braços lateralmente até sentir máximo alongamento peitoral.",
            postureTip = "Mantenha cotovelos levemente flexionados durante todo o movimento."
        ),
        ExerciseVisualGuide(
            exerciseName = "Voador / Peck Deck",
            muscleGroup = "Peitoral Maior & Isolamento",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/pec-deck-fly/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pec-Deck-Fly.gif",
            concentricPhase = "Feche as hastes da máquina à frente espremendo o peitoral por 1 segundo.",
            eccentricPhase = "Abra lentamente mantendo as escápulas coladas ao encosto.",
            postureTip = "Ajuste o assento para que as pegadas fiquem na linha do meio do peito."
        ),

        // === COSTAS & DORSAL ===
        ExerciseVisualGuide(
            exerciseName = "Puxada Alta no Pulley",
            muscleGroup = "Dorsal e Latíssimo",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/lat-pulldown/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif",
            concentricPhase = "Puxe a barra em direção ao peito superior abaixando as escápulas.",
            eccentricPhase = "Suba a barra devagar até alongar completamente a asa (dorsal).",
            postureTip = "Evite balançar o tronco para trás; foque em puxar com os cotovelos."
        ),
        ExerciseVisualGuide(
            exerciseName = "Remada Curvada com Barra",
            muscleGroup = "Dorsal, Miolo das Costas",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-bent-over-row/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif",
            concentricPhase = "Puxe a barra em direção ao umbigo espremendo as escápulas atrás.",
            eccentricPhase = "Alongue os braços completamente mantendo a coluna neutra.",
            postureTip = "Incline o tronco a aproximadamente 45° com joelhos levemente flexionados."
        ),
        ExerciseVisualGuide(
            exerciseName = "Remada Baixa no Cabo",
            muscleGroup = "Dorsal & Romboides",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/seated-cable-row/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Seated-Cable-Row.gif",
            concentricPhase = "Puxe o puxador triangular até a linha do abdômen retraindo as costas.",
            eccentricPhase = "Estenda os braços à frente permitindo alongamento controlado da dorsal.",
            postureTip = "Mantenha o peito aberto e coluna ereta sem hiperextender a lombar."
        ),
        ExerciseVisualGuide(
            exerciseName = "Remada Unilateral com Halter (Serrote)",
            muscleGroup = "Latíssimo & Miolo das Costas",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-row/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Row.gif",
            concentricPhase = "Puxe o halter em direção ao quadril levando o cotovelo para o alto.",
            eccentricPhase = "Desça o halter controladamente até o braço ficar estendido.",
            postureTip = "Apoie o joelho e a mão contrária no banco, mantendo a coluna alinhada."
        ),
        ExerciseVisualGuide(
            exerciseName = "Barra Fixa (Pull-up)",
            muscleGroup = "Dorsal, Bíceps & Core",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/pull-up/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pull-up.gif",
            concentricPhase = "Puxe o corpo para cima até o queixo passar da barra.",
            eccentricPhase = "Desça até extensão quase completa dos braços de forma controlada.",
            postureTip = "Acione o abdômen para não balançar o corpo ou dar impulso com as pernas."
        ),

        // === OMBROS & DELTOIDES ===
        ExerciseVisualGuide(
            exerciseName = "Desenvolvimento com Halteres",
            muscleGroup = "Deltoide Anterior e Lateral",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-shoulder-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif",
            concentricPhase = "Empurre os halteres para cima estendendo os braços verticalmente.",
            eccentricPhase = "Desça os halteres até a linha das orelhas sob estrito controle.",
            postureTip = "Mantenha a coluna ereta, abdômen travado e não jogue a cabeça para a frente."
        ),
        ExerciseVisualGuide(
            exerciseName = "Elevação Lateral com Halteres",
            muscleGroup = "Deltoide Lateral",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-lateral-raise/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Lateral-Raise.gif",
            concentricPhase = "Eleve os braços para os lados até a altura dos ombros.",
            eccentricPhase = "Desça devagar controlando a gravidade sem balançar o corpo.",
            postureTip = "Cotovelos levemente flexionados e polegares apontando discretamente para baixo."
        ),
        ExerciseVisualGuide(
            exerciseName = "Crucifixo Inverso com Halteres",
            muscleGroup = "Deltoide Posterior & Trapézio",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-reverse-fly/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Reverse-Fly.gif",
            concentricPhase = "Abra os braços lateralmente puxando com a porção de trás dos ombros.",
            eccentricPhase = "Aproxime os halteres lentamente mantendo o tronco inclinado.",
            postureTip = "Incline o tronco paralelo ao chão e foque em espremer a parte de trás do ombro."
        ),
        ExerciseVisualGuide(
            exerciseName = "Face Pull na Polia",
            muscleGroup = "Deltoide Posterior, Manguito & Trapézio",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/face-pull/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Face-Pull.gif",
            concentricPhase = "Puxe a corda em direção aos olhos separando as mãos e rodando os ombros externamente.",
            eccentricPhase = "Retorne os braços à frente sob tensão contínua.",
            postureTip = "Excelente para saúde articular dos ombros e postura correta."
        ),

        // === BÍCEPS ===
        ExerciseVisualGuide(
            exerciseName = "Rosca Direta com Barra",
            muscleGroup = "Bíceps Braquial",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Curl.gif",
            concentricPhase = "Flexione os cotovelos elevando a barra em direção aos ombros.",
            eccentricPhase = "Desça a barra estendendo o cotovelo totalmente em 2 a 3 segundos.",
            postureTip = "Mantenha os cotovelos colados ao lado do tronco sem roubar com o quadril."
        ),
        ExerciseVisualGuide(
            exerciseName = "Rosca Martelo com Halteres",
            muscleGroup = "Braquiorradial & Braquial",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/hammer-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Hammer-Curl.gif",
            concentricPhase = "Suba os halteres com a pegada neutra (palmas voltadas uma para a outra).",
            eccentricPhase = "Desça controlando o peso sem balançar o corpo.",
            postureTip = "Excelente para ganho de espessura de braço e antebraço."
        ),
        ExerciseVisualGuide(
            exerciseName = "Rosca Scott com Halter",
            muscleGroup = "Bíceps Isolado (Cabeça Curta)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/preacher-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Preacher-Curl.gif",
            concentricPhase = "Flexione o braço erguendo o halter até contração total do bíceps.",
            eccentricPhase = "Desça lentamente sem hiperextender perigosamente a articulação.",
            postureTip = "Mantenha as axilas e tríceps completamente apoiados na almofada inclinada."
        ),
        ExerciseVisualGuide(
            exerciseName = "Rosca Concentrada com Halter",
            muscleGroup = "Pico do Bíceps",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/concentration-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Concentration-Curl.gif",
            concentricPhase = "Suba o halter em direção ao ombro contraindo fortemente o pico do bíceps.",
            eccentricPhase = "Desça devagar até extensão do braço.",
            postureTip = "Apoie o cotovelo na parte interna da coxa para total isolamento muscular."
        ),
        ExerciseVisualGuide(
            exerciseName = "Rosca Inclinada com Halteres",
            muscleGroup = "Bíceps (Cabeça Longa)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/seated-incline-dumbbell-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Seated-Incline-Dumbbell-Curl.gif",
            concentricPhase = "Suba os halteres supinando os punhos no topo do movimento.",
            eccentricPhase = "Desça completamente sentindo o alongamento máximo do bíceps.",
            postureTip = "Ajuste o banco em 45° a 60° mantendo os ombros para trás."
        ),

        // === TRÍCEPS (TODOS MAPEADOS COM GIFS 100% FUNCIONAIS) ===
        ExerciseVisualGuide(
            exerciseName = "Tríceps Pulley Corda",
            muscleGroup = "Tríceps (Cabeça Lateral e Longa)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/rope-pushdown/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif",
            concentricPhase = "Empurre a corda para baixo abrindo as pontas no final da extensão.",
            eccentricPhase = "Suba as mãos até a linha do peito mantendo cotovelos parados.",
            postureTip = "Trave os cotovelos ao lado do corpo; apenas os antebraços se movimentam."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps Francês",
            muscleGroup = "Tríceps (Cabeça Longa)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/seated-dumbbell-triceps-extension/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/06/Seated-Dumbbell-Triceps-Extension.gif",
            concentricPhase = "Estenda os cotovelos erguendo o halter acima da cabeça.",
            eccentricPhase = "Desça o halter por trás da nuca sentindo o tríceps alongar.",
            postureTip = "Mantenha os cotovelos fechados apontando para a frente e coluna ereta."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps Testa com Barra",
            muscleGroup = "Tríceps (Cabeça Longa & Medial)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-lying-triceps-extension/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Triceps-Extension.gif",
            concentricPhase = "Estenda os cotovelos empurrando a barra para cima até a vertical.",
            eccentricPhase = "Flexione os cotovelos descendo a barra controladamente em direção à testa.",
            postureTip = "Mantenha os braços fixos e verticais; apenas os antebraços realizam a flexão."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps Coice com Halter",
            muscleGroup = "Tríceps (Cabeça Lateral)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-tricep-kickback/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Kickback.gif",
            concentricPhase = "Estenda o braço para trás até ficar paralelo ao tronco e contraia por 1s.",
            eccentricPhase = "Retorne o antebraço a 90 graus sem deixar o cotovelo cair.",
            postureTip = "Mantenha o cotovelo alto e colado à lateral do corpo o tempo todo."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps Mergulho no Banco",
            muscleGroup = "Tríceps & Deltoide Anterior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/bench-dips/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Bench-Dips.gif",
            concentricPhase = "Empurre as palmas contra o banco estendendo os braços.",
            eccentricPhase = "Desça o quadril próximo ao banco flexionando os cotovelos até 90 graus.",
            postureTip = "Mantenha as costas rentes à borda do banco e ombros longe das orelhas."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps nas Paralelas",
            muscleGroup = "Tríceps & Peitoral Inferior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/triceps-dips/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Triceps-Dips.gif",
            concentricPhase = "Empurre as barras para cima até estender completamente os braços.",
            eccentricPhase = "Desça o tronco de forma controlada mantendo o corpo mais vertical.",
            postureTip = "Para focar no tríceps, mantenha o tronco ereto e cotovelos próximos às costelas."
        ),
        ExerciseVisualGuide(
            exerciseName = "Tríceps Supinado (Pegada Fechada)",
            muscleGroup = "Tríceps & Peitoral Maior",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/close-grip-bench-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Close-Grip-Bench-Press.gif",
            concentricPhase = "Empurre a barra para cima com pegada na largura dos ombros.",
            eccentricPhase = "Desça a barra até a linha do peito com cotovelos colados ao tronco.",
            postureTip = "Não feche demais a pegada para proteger a articulação dos punhos."
        ),

        // === PERNAS, GLÚTEOS & PANTURRILHA ===
        ExerciseVisualGuide(
            exerciseName = "Agachamento Livre com Barra",
            muscleGroup = "Quadríceps, Glúteos & Core",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-squat/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif",
            concentricPhase = "Empurre o chão com os calcanhares e suba mantendo o peito erguido.",
            eccentricPhase = "Flexione joelhos e quadril descendo abaixo do ângulo de 90°.",
            postureTip = "Trave a respiração diafragmática (Valsalva) e joelhos acompanham a ponta dos pés."
        ),
        ExerciseVisualGuide(
            exerciseName = "Leg Press 45°",
            muscleGroup = "Quadríceps & Glúteos",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/leg-press/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2015/11/Leg-Press.gif",
            concentricPhase = "Empurre a plataforma sem travar (hiperextender) os joelhos no topo.",
            eccentricPhase = "Desça a plataforma até formar 90° mantendo o quadril colado no encosto.",
            postureTip = "Nunca descole a região lombar do encosto para evitar lesão na coluna."
        ),
        ExerciseVisualGuide(
            exerciseName = "Cadeira Extensora",
            muscleGroup = "Quadríceps Isolado",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/leg-extension/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/LEG-EXTENSION.gif",
            concentricPhase = "Estenda as pernas completamente até o topo segurando 1 segundo.",
            eccentricPhase = "Retorne lentamente à posição inicial sob constante tensão muscular.",
            postureTip = "Ajuste o rolo do tornozelo exatamente acima dos pés e joelhos alinhados ao eixo."
        ),
        ExerciseVisualGuide(
            exerciseName = "Mesa Flexora",
            muscleGroup = "Isquiotibiais (Posterior de Coxa)",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/leg-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Curl.gif",
            concentricPhase = "Flexione as pernas puxando o rolo em direção aos glúteos.",
            eccentricPhase = "Estenda as pernas lentamente mantendo a tensão sem bater os pesos.",
            postureTip = "Mantenha o quadril pressionado contra o banco para não sobrecarregar a lombar."
        ),
        ExerciseVisualGuide(
            exerciseName = "Cadeira Flexora",
            muscleGroup = "Isquiotibiais Sentado",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/seated-leg-curl/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/08/Seated-Leg-Curl.gif",
            concentricPhase = "Puxe a almofada para baixo flexionando os joelhos ao máximo.",
            eccentricPhase = "Suba devagar até extensão quase completa das pernas.",
            postureTip = "Ajuste o encosto de modo que os joelhos fiquem exatamente no eixo da máquina."
        ),
        ExerciseVisualGuide(
            exerciseName = "Stiff com Barra / RDL",
            muscleGroup = "Isquiotibiais & Glúteos",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-romanian-deadlift/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Romanian-Deadlift.gif",
            concentricPhase = "Empurre o quadril para a frente usando os glúteos para ficar em pé.",
            eccentricPhase = "Projete o quadril para trás com a barra rente às pernas até sentir o posterior alongar.",
            postureTip = "Coluna perfeitamente ereta do tórax à lombar; joelhos destravados mas não dobrados."
        ),
        ExerciseVisualGuide(
            exerciseName = "Elevação Pélvica com Barra",
            muscleGroup = "Glúteo Máximo",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/barbell-hip-thrust/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Hip-Thrust.gif",
            concentricPhase = "Empurre o chão com os calcanhares elevando o quadril até alinhar com o tronco.",
            eccentricPhase = "Desça o quadril controladamente mantendo o queixo recolhido.",
            postureTip = "No topo do movimento, contraia os glúteos por 1 a 2 segundos com tíbias verticais."
        ),
        ExerciseVisualGuide(
            exerciseName = "Afundo / Passada com Halteres",
            muscleGroup = "Quadríceps, Glúteos & Equilíbrio",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/dumbbell-lunge/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Lunge.gif",
            concentricPhase = "Empurre com a perna da frente voltando à posição ereta.",
            eccentricPhase = "Dê um passo à frente flexionando ambos os joelhos até 90 graus.",
            postureTip = "O joelho da frente não deve ultrapassar excessivamente a ponta do pé."
        ),
        ExerciseVisualGuide(
            exerciseName = "Panturrilha em Pé",
            muscleGroup = "Gastrocnêmio & Sóleo",
            fitnessProgrammerUrl = "https://fitnessprogramer.com/exercise/standing-calf-raise/",
            gifUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/06/Standing-Calf-Raise.gif",
            concentricPhase = "Suba na ponta dos pés o máximo possível contraindo a panturrilha.",
            eccentricPhase = "Desça os calcanhares abaixo da linha do degrau alongando a musculatura.",
            postureTip = "Mantenha os joelhos estendidos e corpo alinhado sem dobrar o quadril."
        )
    )

    // Mapa com variações, sinônimos e nomes comuns para busca direta
    private val directMap: Map<String, ExerciseVisualGuide> by lazy {
        val map = mutableMapOf<String, ExerciseVisualGuide>()
        catalogGuides.forEach { guide ->
            map[guide.exerciseName] = guide
            map[guide.exerciseName.lowercase()] = guide
        }

        // Aliases para Tríceps
        val tricepsCorda = catalogGuides.first { it.exerciseName == "Tríceps Pulley Corda" }
        map["Tríceps Corda"] = tricepsCorda
        map["Tríceps no Pulley"] = tricepsCorda
        map["Tríceps Pulley"] = tricepsCorda
        map["Tríceps Polia"] = tricepsCorda
        map["Tríceps Pulley Barra"] = tricepsCorda

        val tricepsFrances = catalogGuides.first { it.exerciseName == "Tríceps Francês" }
        map["Tríceps Francês com Halter"] = tricepsFrances
        map["Tríceps Frances"] = tricepsFrances
        map["Tríceps Frances com Halter"] = tricepsFrances
        map["Frances Triceps"] = tricepsFrances

        val tricepsTesta = catalogGuides.first { it.exerciseName == "Tríceps Testa com Barra" }
        map["Tríceps Testa"] = tricepsTesta
        map["Tríceps Testa com Halteres"] = tricepsTesta
        map["Tríceps Testa Barra W"] = tricepsTesta
        map["Tríceps Testa com Barra W"] = tricepsTesta
        map["Skullcrusher"] = tricepsTesta
        map["Skull Crusher"] = tricepsTesta

        val tricepsCoice = catalogGuides.first { it.exerciseName == "Tríceps Coice com Halter" }
        map["Tríceps Coice"] = tricepsCoice
        map["Tríceps Kickback"] = tricepsCoice
        map["Coice Tríceps"] = tricepsCoice

        val tricepsMergulho = catalogGuides.first { it.exerciseName == "Tríceps Mergulho no Banco" }
        map["Tríceps Mergulho"] = tricepsMergulho
        map["Tríceps Banco"] = tricepsMergulho
        map["Mergulho no Banco"] = tricepsMergulho
        map["Mergulho Tríceps"] = tricepsMergulho

        val tricepsParalelas = catalogGuides.first { it.exerciseName == "Tríceps nas Paralelas" }
        map["Tríceps Paralelas"] = tricepsParalelas
        map["Paralelas"] = tricepsParalelas
        map["Triceps Dips"] = tricepsParalelas

        val tricepsSupinado = catalogGuides.first { it.exerciseName == "Tríceps Supinado (Pegada Fechada)" }
        map["Tríceps Supinado"] = tricepsSupinado
        map["Supino Fechado"] = tricepsSupinado
        map["Supino Pegada Fechada"] = tricepsSupinado

        // Aliases para Peitoral
        val supinoReto = catalogGuides.first { it.exerciseName == "Supino Reto com Barra" }
        map["Supino Reto"] = supinoReto
        map["Supino Reto com Halteres"] = supinoReto
        map["Supino Reto Barra"] = supinoReto

        val supinoInclinado = catalogGuides.first { it.exerciseName == "Supino Inclinado com Halteres" }
        map["Supino Inclinado"] = supinoInclinado
        map["Supino Inclinado com Barra"] = supinoInclinado
        map["Supino Inclinado com Halteres (30°)"] = supinoInclinado

        val crossover = catalogGuides.first { it.exerciseName == "Crucifixo no Cross / Crossover" }
        map["Crossover"] = crossover
        map["Cross Over"] = crossover
        map["Crucifixo no Cross"] = crossover

        val voador = catalogGuides.first { it.exerciseName == "Voador / Peck Deck" }
        map["Voador"] = voador
        map["Peck Deck"] = voador
        map["Peck Deck Fly"] = voador

        // Aliases para Costas
        val puxadaAlta = catalogGuides.first { it.exerciseName == "Puxada Alta no Pulley" }
        map["Puxada Alta"] = puxadaAlta
        map["Puxada Alta Aberta no Pulley"] = puxadaAlta
        map["Puxada Frontal"] = puxadaAlta
        map["Puxador Alto"] = puxadaAlta

        val remadaCurvada = catalogGuides.first { it.exerciseName == "Remada Curvada com Barra" }
        map["Remada Curvada"] = remadaCurvada

        val remadaBaixa = catalogGuides.first { it.exerciseName == "Remada Baixa no Cabo" }
        map["Remada Baixa"] = remadaBaixa
        map["Remada Sentada"] = remadaBaixa

        val remadaSerrote = catalogGuides.first { it.exerciseName == "Remada Unilateral com Halter (Serrote)" }
        map["Serrote"] = remadaSerrote
        map["Remada Serrote"] = remadaSerrote
        map["Remada Unilateral"] = remadaSerrote

        // Aliases para Bíceps
        val roscaDireta = catalogGuides.first { it.exerciseName == "Rosca Direta com Barra" }
        map["Rosca Direta"] = roscaDireta
        map["Rosca Direta com Barra W"] = roscaDireta

        val roscaMartelo = catalogGuides.first { it.exerciseName == "Rosca Martelo com Halteres" }
        map["Rosca Martelo"] = roscaMartelo

        val roscaScott = catalogGuides.first { it.exerciseName == "Rosca Scott com Halter" }
        map["Rosca Scott"] = roscaScott
        map["Rosca Scott na Máquina"] = roscaScott

        val roscaConcentrada = catalogGuides.first { it.exerciseName == "Rosca Concentrada com Halter" }
        map["Rosca Concentrada"] = roscaConcentrada

        // Aliases para Pernas
        val agachamento = catalogGuides.first { it.exerciseName == "Agachamento Livre com Barra" }
        map["Agachamento Livre"] = agachamento
        map["Agachamento"] = agachamento

        val legPress = catalogGuides.first { it.exerciseName == "Leg Press 45°" }
        map["Leg Press"] = legPress
        map["Leg Press 45"] = legPress

        val extensora = catalogGuides.first { it.exerciseName == "Cadeira Extensora" }
        map["Extensora"] = extensora

        val flexoraMesa = catalogGuides.first { it.exerciseName == "Mesa Flexora" }
        map["Flexora"] = flexoraMesa

        val flexoraCadeira = catalogGuides.first { it.exerciseName == "Cadeira Flexora" }
        map["Flexora Sentada"] = flexoraCadeira

        val panturrilha = catalogGuides.first { it.exerciseName == "Panturrilha em Pé" }
        map["Panturrilha no Smith em Pé"] = panturrilha
        map["Panturrilha"] = panturrilha
        map["Panturrilha em Pé no Smith"] = panturrilha

        map
    }

    fun getAllAvailableExercises(): List<ExerciseVisualGuide> {
        return catalogGuides
    }

    fun getGuideForExercise(exerciseName: String): ExerciseVisualGuide {
        // 1. Busca exata ou por alias direto
        directMap[exerciseName]?.let { return it }
        directMap[exerciseName.lowercase()]?.let { return it }

        val clean = exerciseName.lowercase().trim()

        // 2. Resolução inteligente específica para Tríceps
        val isTriceps = clean.contains("tríceps") || clean.contains("triceps") || clean.contains("tricep")
        if (isTriceps) {
            when {
                clean.contains("corda") || clean.contains("polia") || clean.contains("pulley") ->
                    return directMap["Tríceps Pulley Corda"]!!
                clean.contains("franc") -> // francês / frances
                    return directMap["Tríceps Francês"]!!
                clean.contains("testa") || clean.contains("skull") ->
                    return directMap["Tríceps Testa com Barra"]!!
                clean.contains("coice") || clean.contains("kickback") ->
                    return directMap["Tríceps Coice com Halter"]!!
                clean.contains("mergulho") || clean.contains("banco") ->
                    return directMap["Tríceps Mergulho no Banco"]!!
                clean.contains("paralela") || clean.contains("dip") ->
                    return directMap["Tríceps nas Paralelas"]!!
                clean.contains("supinado") || clean.contains("fechado") ->
                    return directMap["Tríceps Supinado (Pegada Fechada)"]!!
                else ->
                    return directMap["Tríceps Pulley Corda"]!!
            }
        }

        // Resolução para exercícios de tríceps sem a palavra explícita "tríceps"
        if (clean.contains("francês") || clean.contains("frances")) return directMap["Tríceps Francês"]!!
        if (clean.contains("testa")) return directMap["Tríceps Testa com Barra"]!!
        if (clean.contains("coice")) return directMap["Tríceps Coice com Halter"]!!
        if (clean.contains("mergulho")) return directMap["Tríceps Mergulho no Banco"]!!
        if (clean.contains("paralela")) return directMap["Tríceps nas Paralelas"]!!

        // 3. Resolução inteligente para outros grupamentos
        if (clean.contains("supino")) {
            return if (clean.contains("inclinado")) directMap["Supino Inclinado com Halteres"]!!
            else directMap["Supino Reto com Barra"]!!
        }
        if (clean.contains("cross") || clean.contains("crossover")) return directMap["Crucifixo no Cross / Crossover"]!!
        if (clean.contains("voador") || clean.contains("peck")) return directMap["Voador / Peck Deck"]!!
        if (clean.contains("crucifixo")) {
            return if (clean.contains("inverso")) directMap["Crucifixo Inverso com Halteres"]!!
            else directMap["Crucifixo Reto com Halteres"]!!
        }

        if (clean.contains("puxada") || clean.contains("puxador")) return directMap["Puxada Alta no Pulley"]!!
        if (clean.contains("serrote")) return directMap["Remada Unilateral com Halter (Serrote)"]!!
        if (clean.contains("remada")) {
            return when {
                clean.contains("baixa") || clean.contains("cabo") -> directMap["Remada Baixa no Cabo"]!!
                clean.contains("unilateral") -> directMap["Remada Unilateral com Halter (Serrote)"]!!
                else -> directMap["Remada Curvada com Barra"]!!
            }
        }
        if (clean.contains("barra fixa") || clean.contains("pull up") || clean.contains("pull-up")) return directMap["Barra Fixa (Pull-up)"]!!

        if (clean.contains("desenvolvimento")) return directMap["Desenvolvimento com Halteres"]!!
        if (clean.contains("elevação lateral") || clean.contains("elevacao lateral") || clean.contains("lateral")) return directMap["Elevação Lateral com Halteres"]!!
        if (clean.contains("face pull") || clean.contains("facepull")) return directMap["Face Pull na Polia"]!!

        if (clean.contains("martelo")) return directMap["Rosca Martelo com Halteres"]!!
        if (clean.contains("scott")) return directMap["Rosca Scott com Halter"]!!
        if (clean.contains("concentrad")) return directMap["Rosca Concentrada com Halter"]!!
        if (clean.contains("rosca") || clean.contains("bíceps") || clean.contains("biceps")) return directMap["Rosca Direta com Barra"]!!

        if (clean.contains("agachamento") || clean.contains("squat")) return directMap["Agachamento Livre com Barra"]!!
        if (clean.contains("leg press") || clean.contains("leg")) return directMap["Leg Press 45°"]!!
        if (clean.contains("extensora")) return directMap["Cadeira Extensora"]!!
        if (clean.contains("flexora")) {
            return if (clean.contains("cadeira") || clean.contains("sentad")) directMap["Cadeira Flexora"]!!
            else directMap["Mesa Flexora"]!!
        }
        if (clean.contains("stiff") || clean.contains("rdl")) return directMap["Stiff com Barra / RDL"]!!
        if (clean.contains("pélvica") || clean.contains("pelvica") || clean.contains("hip thrust")) return directMap["Elevação Pélvica com Barra"]!!
        if (clean.contains("afundo") || clean.contains("passada") || clean.contains("lunge")) return directMap["Afundo / Passada com Halteres"]!!
        if (clean.contains("panturrilha") || clean.contains("gemeos") || clean.contains("gêmeos")) return directMap["Panturrilha em Pé"]!!

        // 4. Substring matching geral contra o catálogo
        catalogGuides.firstOrNull { guide ->
            val guideLower = guide.exerciseName.lowercase()
            clean.contains(guideLower) || guideLower.contains(clean)
        }?.let { return it }

        // 5. Fallback seguro e elegante
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
}
