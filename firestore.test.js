const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// ============================================================================
// 1. AUTHENTICATION & CROSS-USER ISOLATION
// ============================================================================

test("Unauthenticated user: cannot read or write user profile or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("workoutLogs")
      .get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
    })
  );
});

test("Authenticated user: cannot read or write another user's profile or subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob",
      age: 30,
    });
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("workoutLogs")
      .doc("log_1")
      .set({
        userId: BOB_UID,
        dateMillis: 1700000000000,
        routineName: "Treino B",
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("workoutLogs")
      .doc("log_1")
      .get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("workoutLogs")
      .doc("log_2")
      .set({
        userId: BOB_UID,
        dateMillis: 1700000000000,
      })
  );
});

// ============================================================================
// 2. CANONICAL COLLECTIONS & STRICT VALIDATORS (S4)
// ============================================================================

test("Owner: can create and update valid user profile, rejects ghost fields and out-of-range metrics", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      name: "Alice Virtus",
      email: "alice@example.com",
      age: 28,
      weightKg: 68.5,
      heightCm: 170,
      targetCalories: 2200,
      isDarkMode: true,
      isBiometricLockEnabled: false,
      hasCompletedOnboarding: true,
    })
  );

  // Reject ghost field (hasOnly enforcement)
  await assertFails(
    profileRef.update({
      isAdmin: true,
    })
  );

  // Reject out-of-bounds age or weight
  await assertFails(
    profileRef.update({
      age: 250,
    })
  );
  await assertFails(
    profileRef.update({
      weightKg: 5,
    })
  );
});

test("Owner: can write valid workoutLogs, cardioLogs, shoppingItems, and workoutRoutines/{id}/exercises", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const userRef = aliceDb.collection("users").doc(ALICE_UID);

  // workoutLogs
  await assertSucceeds(
    userRef.collection("workoutLogs").doc("wlog_1").set({
      userId: ALICE_UID,
      routineId: 1,
      routineName: "Treino A - Peito",
      dateMillis: 1700000000000,
      durationMinutes: 55,
      totalVolumeKg: 4200.5,
      totalSetsCompleted: 16,
      perceivedExertion: 8,
      notes: "Ótimo treino",
    })
  );

  // cardioLogs
  await assertSucceeds(
    userRef.collection("cardioLogs").doc("clog_1").set({
      userId: ALICE_UID,
      type: "RUNNING",
      durationMinutes: 30,
      caloriesBurned: 320,
      distanceKm: 5.2,
      dateMillis: 1700000000000,
      notes: "Corrida leve",
    })
  );

  // shoppingItems
  await assertSucceeds(
    userRef.collection("shoppingItems").doc("item_1").set({
      userId: ALICE_UID,
      name: "Peito de Frango",
      category: "PROTEIN",
      totalQuantityGrams: 2000,
      unit: "kg",
      estimatedPrice: 44.0,
      isBought: false,
    })
  );

  // workoutRoutines & nested exercises
  await assertSucceeds(
    userRef.collection("workoutRoutines").doc("routine_1").set({
      userId: ALICE_UID,
      splitType: "ABC",
      letter: "A",
      title: "Push",
      dayOfWeekLabel: "Segunda",
      targetMuscleGroups: "Peito, Ombro, Tríceps",
      estimatedDurationMinutes: 60,
    })
  );

  await assertSucceeds(
    userRef
      .collection("workoutRoutines")
      .doc("routine_1")
      .collection("exercises")
      .doc("ex_1")
      .set({
        userId: ALICE_UID,
        workoutId: "routine_1",
        routineId: 1,
        exerciseName: "Supino Reto com Barra",
        muscleGroup: "Peitoral",
        sets: 4,
        repsMin: 8,
        repsMax: 10,
        currentWeightKg: 80.0,
        restSeconds: 90,
        orderIndex: 0,
      })
  );

  // Reject ghost field in nested exercise
  await assertFails(
    userRef
      .collection("workoutRoutines")
      .doc("routine_1")
      .collection("exercises")
      .doc("ex_2")
      .set({
        userId: ALICE_UID,
        workoutId: "routine_1",
        exerciseName: "Crucifixo",
        unexpectedField: "hack",
      })
  );
});

test("gpsRouteLogs: enforces routePoints list limit (<= 5000) and routePointsJson size limit", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const gpsCollection = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("gpsRouteLogs");

  // Valid GPS route with small point list
  await assertSucceeds(
    gpsCollection.doc("gps_valid").set({
      userId: ALICE_UID,
      routeName: "Corrida Matinal",
      activityType: "Corrida",
      dateMillis: 1700000000000,
      durationSeconds: 1800,
      distanceMeters: 5000,
      routePoints: ["-23.55,-46.63", "-23.56,-46.64"],
      routePointsJson: "-23.55,-46.63;-23.56,-46.64",
    })
  );

  // Reject GPS route with > 5000 points in routePoints list
  const oversizedPoints = Array.from({ length: 5001 }, (_, i) => `${i}`);
  await assertFails(
    gpsCollection.doc("gps_too_many_points").set({
      userId: ALICE_UID,
      dateMillis: 1700000000000,
      routePoints: oversizedPoints,
    })
  );

  // Reject GPS route with routePointsJson > 45 KB
  const oversizedJson = "x".repeat(46000);
  await assertFails(
    gpsCollection.doc("gps_oversized_json").set({
      userId: ALICE_UID,
      dateMillis: 1700000000000,
      routePointsJson: oversizedJson,
    })
  );
});

// ============================================================================
// 3. LEGACY COLLECTIONS RESTRICTION (S5: read & delete only, no create/update)
// ============================================================================

test("Legacy collections: allow read and delete for migration cleanup, but reject create and update", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const userDoc = context.firestore().collection("users").doc(ALICE_UID);
    await userDoc.collection("workout_logs").doc("legacy_1").set({
      userId: ALICE_UID,
      dateMillis: 1700000000000,
    });
    await userDoc.collection("cardio_logs").doc("legacy_1").set({
      userId: ALICE_UID,
      dateMillis: 1700000000000,
    });
    await userDoc.collection("shoppingList").doc("legacy_1").set({
      userId: ALICE_UID,
      name: "Arroz",
    });
    await userDoc.collection("workoutExercises").doc("legacy_1").set({
      userId: ALICE_UID,
      exerciseName: "Agachamento",
    });
    await userDoc
      .collection("workoutRoutines")
      .doc("1")
      .collection("workoutExercises")
      .doc("legacy_1")
      .set({
        userId: ALICE_UID,
        exerciseName: "Supino",
      });
    await userDoc
      .collection("workouts")
      .doc("1")
      .collection("exercises")
      .doc("legacy_1")
      .set({
        userId: ALICE_UID,
        exerciseName: "Remada",
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const userDoc = aliceDb.collection("users").doc(ALICE_UID);

  // Create/update on legacy collections MUST fail
  await assertFails(
    userDoc.collection("workout_logs").doc("new_doc").set({
      userId: ALICE_UID,
      dateMillis: 1700000000000,
    })
  );
  await assertFails(
    userDoc.collection("cardio_logs").doc("new_doc").set({
      userId: ALICE_UID,
      type: "RUN",
      dateMillis: 1700000000000,
    })
  );
  await assertFails(
    userDoc.collection("shoppingList").doc("new_doc").set({
      userId: ALICE_UID,
      name: "Aveia",
    })
  );
  await assertFails(
    userDoc.collection("workoutExercises").doc("new_doc").set({
      userId: ALICE_UID,
      workoutId: "1",
      exerciseName: "Agachamento",
    })
  );
  await assertFails(
    userDoc
      .collection("workoutRoutines")
      .doc("1")
      .collection("workoutExercises")
      .doc("new_doc")
      .set({
        userId: ALICE_UID,
        workoutId: "1",
        exerciseName: "Supino",
      })
  );
  await assertFails(
    userDoc
      .collection("workouts")
      .doc("1")
      .collection("exercises")
      .doc("new_doc")
      .set({
        userId: ALICE_UID,
        workoutId: "1",
        exerciseName: "Remada",
      })
  );

  // Read and delete on legacy collections MUST succeed for the owner
  await assertSucceeds(userDoc.collection("workout_logs").doc("legacy_1").get());
  await assertSucceeds(userDoc.collection("workout_logs").doc("legacy_1").delete());

  await assertSucceeds(userDoc.collection("cardio_logs").doc("legacy_1").get());
  await assertSucceeds(userDoc.collection("cardio_logs").doc("legacy_1").delete());

  await assertSucceeds(userDoc.collection("shoppingList").doc("legacy_1").get());
  await assertSucceeds(userDoc.collection("shoppingList").doc("legacy_1").delete());

  await assertSucceeds(userDoc.collection("workoutExercises").doc("legacy_1").get());
  await assertSucceeds(userDoc.collection("workoutExercises").doc("legacy_1").delete());

  await assertSucceeds(
    userDoc
      .collection("workoutRoutines")
      .doc("1")
      .collection("workoutExercises")
      .doc("legacy_1")
      .get()
  );
  await assertSucceeds(
    userDoc
      .collection("workoutRoutines")
      .doc("1")
      .collection("workoutExercises")
      .doc("legacy_1")
      .delete()
  );

  await assertSucceeds(
    userDoc
      .collection("workouts")
      .doc("1")
      .collection("exercises")
      .doc("legacy_1")
      .get()
  );
  await assertSucceeds(
    userDoc
      .collection("workouts")
      .doc("1")
      .collection("exercises")
      .doc("legacy_1")
      .delete()
  );
});
