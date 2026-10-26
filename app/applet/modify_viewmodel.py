
with open('/app/applet/app/src/main/java/com/example/ui/VirtusViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i == len(lines) - 1:
        new_lines.append("\n    fun getMuscleVolume(days: Int): Map<String, Float> {\n")
        new_lines.append("        val now = System.currentTimeMillis()\n")
        new_lines.append("        val limit = now - (days.toLong() * 24 * 60 * 60 * 1000)\n")
        new_lines.append("        \n")
        new_lines.append("        val logs = workoutLogs.value.filter { it.dateMillis >= limit }\n")
        new_lines.append("        val muscleVolume = mutableMapOf<String, Float>()\n")
        new_lines.append("        \n")
        new_lines.append("        logs.forEach { log ->\n")
        new_lines.append("            val routine = workoutRoutines.value.find { it.id == log.routineId }\n")
        new_lines.append("            if (routine != null) {\n")
        new_lines.append("                val muscles = routine.targetMuscleGroups.split(\",\").map { it.trim() }\n")
        new_lines.append("                val volumePerMuscle = log.totalVolumeKg / muscles.size\n")
        new_lines.append("                muscles.forEach { muscle ->\n")
        new_lines.append("                    muscleVolume[muscle] = muscleVolume.getOrDefault(muscle, 0f) + volumePerMuscle\n")
        new_lines.append("                }\n")
        new_lines.append("            }\n")
        new_lines.append("        }\n")
        new_lines.append("        return muscleVolume\n")
        new_lines.append("    }\n")
        new_lines.append(line)
    else:
        new_lines.append(line)

with open('/app/applet/app/src/main/java/com/example/ui/VirtusViewModel.kt', 'w') as f:
    f.writelines(new_lines)
