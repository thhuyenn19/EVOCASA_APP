import os
from dotenv import load_dotenv
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from firebase_util import init_firebase, get_all_documents_as_texts

load_dotenv()
init_firebase()

collections = ["Product", "Review", "Voucher", "Order", "Customers", "Category"]

texts = get_all_documents_as_texts(collections)

if not texts:
    print("⚠️ Không có văn bản hợp lệ để tạo vector.")
    exit()

print(f"\n✅ Đã lấy {len(texts)} văn bản từ Firestore.")
print(f"Texts sample: {texts[:5]}")  # Debug để kiểm tra vài bản ghi đầu

docs = [Document(page_content=t) for t in texts]

embedding = GoogleGenerativeAIEmbeddings(model="models/embedding-001")
vectorstore = Chroma.from_documents(docs, embedding, persist_directory="chroma_store")

print("\n✅ Đã lưu vector vào Chroma thành công!")