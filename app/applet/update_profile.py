
path = 'app/src/main/java/com/example/ui/screens/profile/UserProfileScreen.kt'
with open(path, 'r') as f:
    content = f.read()

if 'import com.example.ui.components.MuscleHeatmapView' not in content:
    import_target = 'package com.example.ui.screens.profile'
    content = content.replace(import_target, import_target + '\n\nimport com.example.ui.components.MuscleHeatmapView')

if 'heatmapDays' not in content:
    lazy_column_target = 'LazyColumn('
    state_addition = '''
    var heatmapDays by remember { mutableStateOf(30) }
    val muscleVolume = viewModel.getMuscleVolume(heatmapDays)

    LazyColumn('''
    content = content.replace(lazy_column_target, state_addition, 1)

if 'MuscleHeatmapView(' not in content:
    first_item_target = 'item {'
    heatmap_ui = '''item {
                MuscleHeatmapView(
                    muscleVolumes = muscleVolume,
                    selectedTimeframeDays = heatmapDays,
                    onTimeframeSelected = { heatmapDays = it }
                )
            }
            item {'''
    content = content.replace(first_item_target, heatmap_ui, 1)

with open(path, 'w') as f:
    f.write(content)

print("Successfully updated UserProfileScreen.kt")
