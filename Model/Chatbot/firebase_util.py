import os
import firebase_admin
from firebase_admin import credentials, firestore

def init_firebase():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = os.path.join(base_dir, "firebase_service.json")
    cred = credentials.Certificate(json_path)
    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred)

def get_all_documents_as_texts(collections: list[str]) -> list[str]:
    db = firestore.client()
    all_texts = []

    # Tạo map từ ID → Tên Category
    category_map = {}
    try:
        for doc in db.collection("Category").stream():
            data = doc.to_dict()
            if data and "Name" in data:
                category_map[doc.id] = data["Name"]
        print(f"Category map: {category_map}")  # Debug để kiểm tra ánh xạ
    except Exception as e:
        print(f"❌ Lỗi khi đọc collection 'Category': {e}")

    for collection_name in collections:
        print(f"\n📥 Đọc collection '{collection_name}'")
        try:
            docs = db.collection(collection_name).stream()
            for doc in docs:
                data = doc.to_dict()
                if not data:
                    continue

                # Gộp ParentCategory từ ID → Tên nếu có
                if "ParentCategory" in data:
                    parent = data["ParentCategory"]
                    if isinstance(parent, dict) and "$oid" in parent:
                        oid = parent["$oid"]
                        data["ParentCategory"] = category_map.get(oid, f"Unknown ID: {oid}")
                    elif parent is None or parent == "null":  # Xử lý cả None và "null"
                        data["ParentCategory"] = "None"

                # Gộp toàn bộ field thành văn bản rõ ràng
                text = "\n".join(f"{k}: {str(v)}" for k, v in data.items() if k != "_id")  # Loại bỏ _id để gọn
                all_texts.append(text)

        except Exception as e:
            print(f"❌ Lỗi khi đọc collection '{collection_name}': {e}")

    return list(dict.fromkeys(all_texts))  # Loại bỏ trùng lặp