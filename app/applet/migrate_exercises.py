import firebase_admin
from firebase_admin import credentials, firestore

# Inicializar com as credenciais apropriadas
cred = credentials.ApplicationDefault()
firebase_admin.initialize_app(cred)
db = firestore.client()

def migrate_exercises():
    users = db.collection('users').stream()
    for user in users:
        userId = user.id
        old_exercises = db.collection('users').document(userId).collection('workoutExercises').stream()
        
        for doc in old_exercises:
            data = doc.to_dict()
            workoutId = str(data.get('routineId', 'free'))
            
            # Novo caminho
            new_ref = db.collection('users').document(userId).collection('workouts').document(workoutId).collection('exercises').document(doc.id)
            
            # Copiar e deletar
            new_ref.set(data)
            doc.reference.delete()
            print(f"Migrado: User {userId}, Exercise {doc.id} para Workouts/{workoutId}")

if __name__ == "__main__":
    migrate_exercises()
