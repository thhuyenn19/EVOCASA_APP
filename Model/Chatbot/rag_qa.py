import os
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ConversationBufferMemory
from langchain.prompts import PromptTemplate
from firebase_util import init_firebase, get_all_documents_as_texts

load_dotenv()
init_firebase()

# Tạo vectorstore
embedding = GoogleGenerativeAIEmbeddings(model="models/embedding-001")
vectorstore = Chroma(persist_directory="chroma_store", embedding_function=embedding)
retriever = vectorstore.as_retriever(search_type="similarity", search_kwargs={"k": 10})  # Tăng k lên 10

# Cấu hình LLM và chain
llm = ChatGoogleGenerativeAI(model="models/gemini-1.5-pro", temperature=0.2)
memory = ConversationBufferMemory(
    memory_key="chat_history",
    return_messages=True,
    output_key="answer"
)

qa_chain = ConversationalRetrievalChain.from_llm(
    llm=llm,
    retriever=retriever,
    memory=memory,
    chain_type="stuff",
    return_source_documents=True,
    output_key="answer",
    combine_docs_chain_kwargs={"prompt": PromptTemplate(
        input_variables=["context", "question"],
        template="""
Bạn là trợ lý bán hàng thông minh. Dựa trên thông tin sau: {context}, hãy trả lời câu hỏi: {question} một cách ngắn gọn và chính xác.

- Nếu câu hỏi là 'list category', hãy liệt kê tất cả các danh mục (Name) từ dữ liệu.
- Nếu câu hỏi là 'list parent category', hãy liệt kê tất cả các danh mục có ParentCategory là 'None' (là danh mục cha), bỏ qua các danh mục con.
- Sử dụng định dạng:
  - Danh mục 1
  - Danh mục 2
  - ...
- Nếu dữ liệu không đầy đủ, hãy thông báo: 'Dữ liệu không đủ để liệt kê đầy đủ.'
"""
    )}
)

print("💬 Nhập câu hỏi (gõ 'exit' để thoát)")
while True:
    query = input("\nBạn: ")
    if query.strip().lower() == "exit":
        break

    result = qa_chain.invoke({"question": query})
    print(f"\n🤖 Gemini trả lời:\n{result['answer']}")
    print(f"\n📚 Tài liệu tham chiếu: {result['source_documents']}")