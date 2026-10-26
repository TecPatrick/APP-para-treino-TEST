
path = '/app/applet/app/src/main/java/com/example/ui/VirtusViewModel.kt'
with open(path, 'r') as f:
    content = f.read()

method_code = '''
    fun getMuscleVolume(days: Int): Map<String, Float> {
        val now = System.currentTimeMillis()
        val limit = if (days <= 0) 0L else now - (days.toLong() * 24 * 60 * 60 * 1000)
        
        val logs = workoutLogs.value.filter { it.dateMillis >= limit }
        val muscleVolume = mutableMapOf<String, Float>()
        
        logs.forEach { log ->
            val routine = workoutRoutines.value.find { it.id == log.routineId }
            if (routine != null) {
                val muscles = routine.targetMuscleGroups.split(",").map { it.trim() }
                if (muscles.isNotEmpty()) {
                    val volumePerMuscle = log.totalVolumeKg / muscles.size
                    muscles.forEach { muscle ->
                        if (muscle.isNotBlank()) {
                            muscleVolume[muscle] = muscleVolume.getOrDefault(muscle, 0f) + volumePerMuscle
                        }
                    }
                }
            }
        }
        return muscleVolume
    }
'''

# Insert right before the last closing brace
if 'fun getMuscleVolume' not in content:
    last_brace_idx = content.rfind('}')
    if last_brace_idx != -1:
        new_content = content[:last_brace_idx] + method_code + "\n}\n"
        with open(path, 'w') as f:
            f.write(new_content)
        print("Successfully added getMuscleVolume to VirtusViewModel")
    else:
        print("Could not find last brace")
else:
    print("Method already exists")
