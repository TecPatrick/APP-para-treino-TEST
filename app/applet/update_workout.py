path = '/app/applet/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt'
with open(path, 'r') as f:
    code = f.read()

start_idx = code.find('// Division Filter Selector')
if start_idx == -1:
    start_idx = code.find('// Division Filter')

end_idx = code.find('// --- MODAL DIALOGS ---')

print('Start idx:', start_idx, 'End idx:', end_idx)

replacement = """            // ROUTINE CARDS LIST
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (allRoutines.isEmpty()) {
                item {
                    EmptyWorkoutGroupCard(
                        title = "Nenhuma Ficha Cadastrada",
                        subtitle = "Crie suas fichas de treino para visualizar os exercícios.",
                        onCreateClick = {
                            routineToEdit = null
                            presetLetterForNewRoutine = "A"
                            presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                            showRoutineEditorDialog = true
                        }
                    )
                }
            } else {
                items(allRoutines) { routine ->
                    val exercisesForRoutine = allExercises.filter { it.routineId == routine.id }
                    RoutineCardItem(
                        routine = routine,
                        exerciseCount = exercisesForRoutine.size,
                        onOpenRoutine = {
                            viewingRoutineDetail = routine
                        },
                        onStartClick = {
                            viewModel.startActiveWorkout(routine, exercisesForRoutine)
                            viewingRoutineDetail = routine
                        },
                        onEditClick = {
                            routineToEdit = routine
                            showRoutineEditorDialog = true
                        },
                        onShareClick = {
                            val text = viewModel.formatRoutineForSharing(routine)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Ficha"))
                        },
                        onDeleteClick = { routineToDelete = routine }
                    )
                }
            }
        }
    }

"""

if start_idx != -1 and end_idx != -1:
    code = code[:start_idx] + replacement + code[end_idx:]
    with open(path, 'w') as f:
        f.write(code)
    print('WorkoutScreen updated successfully.')
else:
    print('Error: indices not found')
