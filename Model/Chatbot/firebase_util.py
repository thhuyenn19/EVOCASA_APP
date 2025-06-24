import firebase_admin
from firebase_admin import credentials, db

def init_firebase():
    cred = credentials.Certificate("firebase_service.json")
    firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://your-project-id.firebaseio.com'
    })

def get_text_data(path="/shop_data"):
    ref = db.reference(path)
    raw_data = ref.get()
    texts = [item["description"] for item in raw_data.values()]
    return texts
