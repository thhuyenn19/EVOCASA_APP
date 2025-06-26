import os
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_community.vectorstores import Chroma  # Sử dụng tạm, cần cập nhật sau
from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ConversationBufferMemory
from langchain.prompts import PromptTemplate
from firebase_util import init_firebase, get_all_documents_as_texts
from firebase_admin import firestore

load_dotenv()
init_firebase()

# Khởi tạo Firestore client
db = firestore.client()

# Tạo vectorstore (cảnh báo về Chroma, cần cài langchain-chroma)
embedding = GoogleGenerativeAIEmbeddings(model="models/embedding-001")
vectorstore = Chroma(persist_directory="chroma_store", embedding_function=embedding)
retriever = vectorstore.as_retriever(search_type="similarity", search_kwargs={"k": 5})

# Lấy tất cả danh mục từ Firestore, bao gồm ParentCategory
def get_all_categories():
    categories = {}
    try:
        docs = db.collection("Category").stream()
        for doc in docs:
            data = doc.to_dict()
            if data and "Name" in data:
                categories[data["Name"]] = data.get("ParentCategory", "None")
    except Exception as e:
        print(f"❌ Lỗi khi lấy danh mục: {e}")
    # Lọc danh mục cha (ParentCategory là None hoặc "None")
    parent_categories = [name for name, parent in categories.items() if parent is None or parent == "None"]
    return sorted(parent_categories)  # Sắp xếp danh sách

# Cấu hình LLM và chain
try:
    llm = ChatGoogleGenerativeAI(model="models/gemini-2.5-pro", temperature=0.2)
except ValueError as e:
    print(f"⚠️ Mô hình 'gemini-2.5-pro' không khả dụng, dùng 'gemini-1.5-pro' thay thế. Lỗi: {e}")
    llm = ChatGoogleGenerativeAI(model="models/gemini-1.5-pro", temperature=0.2)

memory = ConversationBufferMemory(
    memory_key="chat_history",
    return_messages=True,
    output_key="answer",
    input_key="question"  # Chỉ định khóa đầu vào chính
)

qa_chain = ConversationalRetrievalChain.from_llm(
    llm=llm,
    retriever=retriever,
    memory=memory,
    chain_type="stuff",
    return_source_documents=True,
    output_key="answer",
    combine_docs_chain_kwargs={"prompt": PromptTemplate(
        input_variables=["context", "question", "all_categories"],
        template="""
You are an intelligent sales assistant for EvoCasa. Based on the information from the vectorstore: {context} and all categories: {all_categories}, please answer the question: {question} in English, concisely and accurately.

- If the question is 'list category name', 'all category name', or 'give me list category name', list ALL unique category names from {all_categories} in a single list, ignoring the context.
- If the question is 'list parent category', list ONLY the category names from {all_categories}, which are the parent categories (those with no parent category), in a single list, ignoring the context.
- If the question matches any FAQ questions (Question) from the CSV, provide the corresponding Answer in English and use only the most relevant FAQ document.
- If no answer is found, suggest: 'Please contact support via email support@evocasa.com or hotline 1800-XXX-XXX.'
- Use the format:
  - Info 1
  - Info 2
  - ...
"""
    )}
)

print("💬 Enter your question (type 'exit' to quit)")
while True:
    query = input("\nYou: ")
    if query.strip().lower() == "exit":
        break

    # Lấy tất cả danh mục trước khi invoke
    all_categories = get_all_categories()
    if not all_categories:
        print("⚠️ Không tìm thấy danh mục nào từ Firestore.")
    # Truyền question làm input chính, all_categories làm tham số bổ sung
    result = qa_chain.invoke({"question": query, "all_categories": "\n- ".join(all_categories) if all_categories else "No categories found"})
    print(f"\n🤖 Gemini answers:\n{result['answer']}")
    print(f"\n📚 Reference documents: {result['source_documents']}")