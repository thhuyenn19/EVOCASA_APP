import os
import csv
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from firebase_util import init_firebase, get_all_documents_as_texts

load_dotenv()
init_firebase()

# Lấy dữ liệu từ Firestore
collections = ["Product", "Review", "Voucher", "Order", "Customers", "Category"]
firestore_texts = get_all_documents_as_texts(collections)

if not firestore_texts:
    print(" Không có văn bản hợp lệ từ Firestore.")
    exit()

print(f"\n Đã lấy {len(firestore_texts)} văn bản từ Firestore.")
print(f"Firestore sample: {firestore_texts[:2]}")  

# Đọc dữ liệu từ file CSV (FAQ)
csv_texts = []
csv_file_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "faq.csv")
try:
    with open(csv_file_path, mode='r', encoding='utf-8') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Kiểm tra các trường bắt buộc
            if all(key in row for key in ["Question", "Topic", "Answer", "Keywords"]):
                text = f"Question: {row['Question']}\nTopic: {row['Topic']}\nAnswer: {row['Answer']}\nKeywords: {row['Keywords']}"
                csv_texts.append(text)
            else:
                print(f" Dòng thiếu dữ liệu: {row}")
except FileNotFoundError:
    print(f" File {csv_file_path} không tồn tại. Vui lòng kiểm tra đường dẫn.")
    exit()
except Exception as e:
    print(f" Lỗi khi đọc file CSV: {e}")
    exit()

print(f"\n Đã lấy {len(csv_texts)} văn bản từ CSV.")
print(f"CSV sample: {csv_texts[:2]}")  # Debug

# Gộp tất cả dữ liệu
all_texts = firestore_texts + csv_texts
if not all_texts:
    print(" Không có văn bản hợp lệ để tạo vector.")
    exit()

docs = [Document(page_content=t) for t in all_texts]

# Tạo vectorstore
embedding = GoogleGenerativeAIEmbeddings(model="models/embedding-001")
vectorstore = Chroma.from_documents(docs, embedding, persist_directory="chroma_store")

print("\n Đã lưu vector vào Chroma thành công!")
