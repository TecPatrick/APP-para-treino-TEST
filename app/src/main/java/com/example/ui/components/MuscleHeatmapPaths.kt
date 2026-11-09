package com.example.ui.components

import android.graphics.RectF
import android.graphics.Region
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.PathParser

internal const val HEATMAP_VIEWPORT_WIDTH = 200f
internal const val HEATMAP_VIEWPORT_HEIGHT = 440f
internal const val HEATMAP_CENTER_X = 100f

internal fun resolveMuscleVolume(names: List<String>, volumes: Map<String, Float>): Float {
    if (names.isEmpty()) return 0f
    // Direct match first
    names.firstNotNullOfOrNull { name ->
        volumes.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
    }?.let { return it }
    // Partial match fallback
    names.firstNotNullOfOrNull { name ->
        volumes.entries.firstOrNull {
            it.key.contains(name, ignoreCase = true) || name.contains(it.key, ignoreCase = true)
        }?.value
    }?.let { return it }
    return 0f
}

/**
 * Representa uma região anatômica espelhada no espaço 200 x 440 (centro X = 100f).
 * Inclui o caminho principal do músculo (`outlinePath`) e feixes de fibras internas (`fiberPaths`)
 * para conferir estética anatômica estilo Hevy App.
 */
@Immutable
internal class AnatomicalMuscleRegion(
    val primaryName: String,
    val aliases: List<String>,
    outlineSvg: String,
    fiberSvgs: List<String> = emptyList()
) {
    val outlinePath: Path by lazy { PathParser().parsePathString(outlineSvg).toPath() }
    val fiberPaths: List<Path> by lazy { fiberSvgs.map { PathParser().parsePathString(it).toPath() } }
    private val hitRegion: Region by lazy {
        val androidPath = outlinePath.asAndroidPath()
        val bounds = RectF()
        androidPath.computeBounds(bounds, true)
        Region().apply {
            setPath(
                androidPath,
                Region(
                    bounds.left.toInt() - 2,
                    bounds.top.toInt() - 2,
                    bounds.right.toInt() + 2,
                    bounds.bottom.toInt() + 2
                )
            )
        }
    }

    fun containsPoint(x: Float, y: Float): Boolean {
        val canonicalX = if (x > HEATMAP_CENTER_X) HEATMAP_VIEWPORT_WIDTH - x else x
        return hitRegion.contains(canonicalX.toInt(), y.toInt())
    }
}

private val CommonHeadAndNeckSilhouette = listOf(
    "M100 10 C86 10 79 20 80 33 C81 43 85 51 91 56 L90 66 L100 69 L100 10 Z",
    "M80 32 C77 32 77 39 81 41 Z"
)

private val BaseLimbAndTorsoSilhouette = listOf(
    "M100 66 L89 65 L74 71 C58 73 48 81 44 96 L38 126 L29 164 L22 196 " +
        "C17 203 15 212 19 218 C22 222 27 219 30 212 L33 198 L45 166 L54 132 " +
        "L58 114 L61 142 L65 172 L62 198 C55 224 54 256 59 286 L62 308 " +
        "C57 328 57 352 63 376 L65 398 L56 412 C53 418 57 423 67 423 L78 421 " +
        "L80 398 L81 374 C86 350 86 328 83 308 L85 286 L94 234 L100 226 Z"
)

internal val AnteriorSilhouette: List<Path> by lazy {
    (CommonHeadAndNeckSilhouette + BaseLimbAndTorsoSilhouette).map {
        PathParser().parsePathString(it).toPath()
    }
}

internal val PosteriorSilhouette: List<Path> by lazy {
    (CommonHeadAndNeckSilhouette + BaseLimbAndTorsoSilhouette).map {
        PathParser().parsePathString(it).toPath()
    }
}

internal val AnteriorJointAndBoneLines: List<Path> by lazy {
    listOf(
        "M98 73 Q82 72 64 76",
        "M100 76 L100 198",
        "M66 290 Q73 286 80 290 Q81 301 73 305 Q65 301 66 290",
        "M23 202 L20 214",
        "M26 203 L24 215",
        "M61 416 Q70 414 77 417"
    ).map { PathParser().parsePathString(it).toPath() }
}

internal val PosteriorJointAndBoneLines: List<Path> by lazy {
    listOf(
        "M100 66 L100 202",
        "M64 296 Q73 301 82 296",
        "M70 374 L71 406",
        "M75 374 L74 406"
    ).map { PathParser().parsePathString(it).toPath() }
}

internal val AnteriorMuscles = listOf(
    AnatomicalMuscleRegion(
        primaryName = "Trapézio",
        aliases = listOf("Trapézio", "Costas"),
        outlineSvg = "M89 58 L71 71 Q81 73 91 71 L91 61 Z",
        fiberSvgs = listOf("M88 63 L77 70")
    ),
    AnatomicalMuscleRegion(
        primaryName = "Ombros",
        aliases = listOf("Ombros", "Deltoides", "Deltoide"),
        outlineSvg = "M70 72 C56 73 46 82 43 97 C42 105 45 113 51 116 C56 111 61 101 64 91 L72 77 Z",
        fiberSvgs = listOf(
            "M64 76 C54 83 49 94 48 108",
            "M68 79 C59 87 55 97 53 111"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Peitoral",
        aliases = listOf("Peitoral", "Peito"),
        outlineSvg = "M72 75 Q85 73 98 76 L98 115 C88 121 72 120 62 111 C59 103 61 91 66 83 Z",
        fiberSvgs = listOf(
            "M65 88 Q81 86 97 87",
            "M62 98 Q80 99 97 97",
            "M64 107 Q81 111 97 106"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Bíceps",
        aliases = listOf("Bíceps", "Braços"),
        outlineSvg = "M44 102 C49 106 55 104 58 99 L55 129 C52 138 43 139 39 131 C37 121 39 110 44 102 Z",
        fiberSvgs = listOf(
            "M49 105 C47 115 46 125 46 134",
            "M44 109 C42 117 42 124 43 131"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Antebraços",
        aliases = listOf("Antebraços", "Braços"),
        outlineSvg = "M38 134 C44 139 51 138 53 133 C51 151 45 172 34 195 L25 193 C28 171 31 151 38 134 Z",
        fiberSvgs = listOf(
            "M45 138 C42 155 37 173 31 191",
            "M39 142 C36 157 32 173 28 189"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Abdominais",
        aliases = listOf("Abdominais", "Core", "Abdômen"),
        outlineSvg = "M76 120 L98 119 L98 196 C88 194 81 182 78 168 L75 138 Z",
        fiberSvgs = listOf(
            "M77 137 Q88 139 98 136",
            "M77 154 Q88 156 98 153",
            "M79 171 Q89 173 98 170"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Abdominais",
        aliases = listOf("Abdominais", "Core", "Abdômen"),
        outlineSvg = "M62 114 C67 119 72 121 74 122 L76 168 L83 192 C73 187 66 175 64 159 L61 132 Z",
        fiberSvgs = listOf(
            "M63 125 L73 133",
            "M63 138 L74 147",
            "M65 152 L75 162"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Quadríceps",
        aliases = listOf("Quadríceps", "Pernas", "Coxas"),
        outlineSvg = "M64 186 C74 194 85 201 97 204 L94 235 L85 283 C79 289 67 288 60 281 C54 250 55 215 64 186 Z",
        fiberSvgs = listOf(
            "M76 196 C74 224 73 253 72 282",
            "M66 202 C61 228 61 255 65 278",
            "M89 208 C88 232 85 256 79 280",
            "M83 254 Q78 268 83 281"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Panturrilhas",
        aliases = listOf("Panturrilhas", "Pernas"),
        outlineSvg = "M62 309 C69 306 77 306 82 310 C86 332 85 356 80 384 L66 384 C58 358 57 332 62 309 Z",
        fiberSvgs = listOf(
            "M72 311 C71 335 72 360 73 382",
            "M65 318 C62 338 63 358 67 378"
        )
    )
)

internal val PosteriorMuscles = listOf(
    AnatomicalMuscleRegion(
        primaryName = "Trapézio",
        aliases = listOf("Trapézio", "Costas", "Dorsais"),
        outlineSvg = "M90 56 L70 71 C74 84 83 102 99 125 L99 56 Z",
        fiberSvgs = listOf(
            "M75 74 L98 78",
            "M80 88 L98 94",
            "M87 103 L98 110"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Ombros",
        aliases = listOf("Ombros", "Deltoides", "Deltoide"),
        outlineSvg = "M69 72 C56 73 46 82 43 96 C43 104 47 110 55 112 C62 104 68 92 72 80 Z",
        fiberSvgs = listOf(
            "M66 78 C58 85 51 94 48 105",
            "M69 84 C62 91 56 99 53 108"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Costas",
        aliases = listOf("Costas", "Dorsais", "Grande Dorsal"),
        outlineSvg = "M71 84 C65 96 60 106 58 115 L62 146 C67 160 77 169 87 174 L98 152 L98 126 C84 108 76 95 71 84 Z",
        fiberSvgs = listOf(
            "M61 108 C71 116 83 125 97 135",
            "M62 125 C72 133 84 141 97 148",
            "M65 142 C74 150 84 157 93 163"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Lombar",
        aliases = listOf("Lombar", "Costas", "Dorsais"),
        outlineSvg = "M87 156 L98 153 L98 188 L76 178 C79 169 83 162 87 156 Z",
        fiberSvgs = listOf(
            "M91 159 L90 182",
            "M84 166 L83 180"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Tríceps",
        aliases = listOf("Tríceps", "Braços"),
        outlineSvg = "M44 101 C50 106 56 105 58 100 L55 130 C51 138 43 139 39 131 C37 121 39 110 44 101 Z",
        fiberSvgs = listOf(
            "M43 124 Q48 112 53 125",
            "M48 105 L48 118"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Antebraços",
        aliases = listOf("Antebraços", "Braços"),
        outlineSvg = "M38 134 C44 139 51 138 53 133 C51 151 45 172 34 195 L25 193 C28 171 31 151 38 134 Z",
        fiberSvgs = listOf(
            "M45 138 C42 155 37 173 31 191",
            "M39 142 C36 157 32 173 28 189"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Glúteos",
        aliases = listOf("Glúteos", "Pernas"),
        outlineSvg = "M65 174 C75 176 87 182 98 190 L98 228 C84 238 66 234 59 220 C56 204 58 187 65 174 Z",
        fiberSvgs = listOf(
            "M66 184 Q81 189 96 199",
            "M61 198 Q78 205 96 213",
            "M62 213 Q78 221 95 224"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Isquiotibiais",
        aliases = listOf("Isquiotibiais", "Posterior", "Posterior de Coxa", "Pernas"),
        outlineSvg = "M59 224 C70 236 84 239 95 231 L85 288 C78 294 67 294 61 288 C55 266 55 244 59 224 Z",
        fiberSvgs = listOf(
            "M76 235 C75 253 74 272 73 291",
            "M65 236 C63 254 63 271 65 286",
            "M86 237 C84 254 82 271 79 286"
        )
    ),
    AnatomicalMuscleRegion(
        primaryName = "Panturrilhas",
        aliases = listOf("Panturrilhas", "Pernas"),
        outlineSvg = "M62 304 C69 299 77 299 83 305 C87 325 86 347 80 374 L66 374 C57 348 56 325 62 304 Z",
        fiberSvgs = listOf(
            "M73 303 L73 345",
            "M61 338 Q67 349 73 342 Q79 349 84 338"
        )
    )
)
