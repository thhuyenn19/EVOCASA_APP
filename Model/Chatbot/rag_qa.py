import os
import re
import time
import asyncio
from concurrent.futures import ThreadPoolExecutor
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ConversationBufferMemory
from langchain.prompts import PromptTemplate
from firebase_util import init_firebase, get_all_documents_as_texts
from firebase_admin import firestore

load_dotenv()
init_firebase()

# Khởi tạo Firestore client
db = firestore.client()

# Cache categories để tránh gọi Firestore mỗi lần
CATEGORIES_CACHE = {}
CACHE_TIMESTAMP = 0
CACHE_DURATION = 300  # 5 phút

# Enhanced common responses - thân thiện và chi tiết hơn
COMMON_RESPONSES = {
    "hello": "Hello! Welcome to EvoCasa! 😊 I'm your furniture consultant, ready to help you find the perfect pieces for your home. Whether you're looking for specific products, pricing information, or need advice on furniture selection, I'm here to assist you. What can I help you with today?",
    "hi": "Hi there! Great to have you at EvoCasa! 🏠 I'm here to help you explore our premium furniture collection. I can provide product details, pricing, availability, design advice, or help you compare different options. How may I assist you?",
    "hey": "Hey! Welcome to EvoCasa - where comfort meets style! 👋 I'm your personal furniture assistant, ready to help you discover amazing pieces for your space. What would you like to know about our collection today?",
    "good morning": "Good morning! What a wonderful day to explore beautiful furniture! ☀️ I'm here to help you find exactly what you need for your home. Whether it's a specific item or general browsing, I'm at your service. How can I assist you this morning?",
    "good afternoon": "Good afternoon! I hope you're having a great day! 🌤️ I'm here to help you with all your EvoCasa furniture needs - from product information to design suggestions. What can I help you discover today?",
    "good evening": "Good evening! Perfect time to plan your home improvements! 🌙 I'm here to help you explore our furniture collection and find pieces that will make your space even more beautiful. How may I assist you tonight?",
    "thanks": "You're absolutely welcome! 😊 I'm so glad I could help you today. If you have any other questions about our products, need more details, or want to explore other options, please don't hesitate to ask. I'm here whenever you need assistance!",
    "thank you": "It's my pleasure to help you! 🌟 That's what I'm here for. If you need any additional information, want to see similar products, or have questions about delivery and services, feel free to ask anytime. I'm always ready to assist!",
    "bye": "Thank you so much for visiting EvoCasa today! 👋 It was wonderful helping you explore our furniture collection. Don't forget to check back for new arrivals and special offers. Have a fantastic day, and see you again soon!",
    "goodbye": "Goodbye and thank you for choosing EvoCasa! 🌟 I hope you found everything you were looking for. Remember, I'm always here if you need more information or assistance with your furniture needs. Take care and have a wonderful day!",
    "how are you": "I'm doing fantastic, thank you for asking! 😊 I'm excited and ready to help you discover amazing furniture pieces today. EvoCasa has such a wonderful collection, and I love helping customers find exactly what they need for their homes. How are you doing, and what brings you to EvoCasa today?",
    "what's up": "Hello there! I'm here and ready to help you with anything EvoCasa related! 🛋️ Whether you're looking for specific furniture pieces, comparing options, checking prices, or just browsing for inspiration, I'm your go-to assistant. What's on your furniture wishlist today?"
}

# Function to check for common responses - vẫn giữ exact matching
def check_common_response(query):
    query_lower = query.lower().strip()
    
    # Chỉ check exact matches để tránh nhầm lẫn
    if query_lower in COMMON_RESPONSES:
        return COMMON_RESPONSES[query_lower]
    
    # Chỉ match các pattern rất cụ thể
    exact_patterns = {
        r'^(hello|hi|hey)$': "hello",
        r'^(thank you|thanks)$': "thanks", 
        r'^(bye|goodbye)$': "bye",
        r'^good (morning|afternoon|evening)$': query_lower,
        r'^how are you\??$': "how are you",
        r'^what\'?s up\??$': "what's up"
    }
    
    for pattern, response_key in exact_patterns.items():
        if re.match(pattern, query_lower):
            return COMMON_RESPONSES.get(response_key, COMMON_RESPONSES.get("hello"))
    
    return None

# Cache-enabled function để lấy categories
def get_all_categories_cached():
    global CATEGORIES_CACHE, CACHE_TIMESTAMP
    current_time = time.time()
    
    # Kiểm tra cache
    if CATEGORIES_CACHE and (current_time - CACHE_TIMESTAMP) < CACHE_DURATION:
        return CATEGORIES_CACHE
    
    categories = {}
    try:
        docs = db.collection("Category").stream()
        for doc in docs:
            data = doc.to_dict()
            if data and "Name" in data:
                categories[data["Name"]] = data.get("ParentCategory", "None")
        
        # Lọc danh mục cha
        parent_categories = [name for name, parent in categories.items() if parent is None or parent == "None"]
        
        # Update cache
        CATEGORIES_CACHE = sorted(parent_categories)
        CACHE_TIMESTAMP = current_time
        
        return CATEGORIES_CACHE
        
    except Exception as e:
        print(f"❌ Lỗi khi lấy danh mục: {e}")
        return CATEGORIES_CACHE if CATEGORIES_CACHE else []

# Khởi tạo vectorstore và embedding model
print("🚀 Initializing EvoCasa Assistant...")
embedding = GoogleGenerativeAIEmbeddings(model="models/embedding-001")
vectorstore = Chroma(persist_directory="chroma_store", embedding_function=embedding)

# Cân bằng retriever - đủ thông tin nhưng không quá nhiều
retriever = vectorstore.as_retriever(
    search_type="similarity", 
    search_kwargs={"k": 4}  # Cân bằng: đủ context nhưng không quá chi tiết
)

# Hybrid LLM Configuration - Intelligent model selection
def get_llm_for_query(query):
    """Chọn model phù hợp dựa trên độ phức tạp của câu hỏi"""
    query_lower = query.lower()
    
    # Simple queries - use Flash for speed but with better temperature
    simple_patterns = [
        'list', 'show', 'what is', 'how much', 'price', 'cost',
        'available', 'in stock', 'color', 'size', 'dimension'
    ]
    
    # Complex queries - use Pro for better understanding
    complex_patterns = [
        'recommend', 'suggest', 'compare', 'difference', 'better',
        'why', 'how to', 'explain', 'advice', 'opinion', 'return', 'warranty'
    ]
    
    # Check for complex patterns first
    if any(pattern in query_lower for pattern in complex_patterns):
        try:
            return ChatGoogleGenerativeAI(
                model="models/gemini-1.5-pro",
                temperature=0.4,  # Tăng để tự nhiên hơn, ít formal
                timeout=20
            ), "Pro"
        except:
            pass
    
    # Check for simple patterns - vẫn dùng Flash nhưng với setting tốt hơn
    if any(pattern in query_lower for pattern in simple_patterns):
        try:
            return ChatGoogleGenerativeAI(
                model="models/gemini-1.5-flash",
                temperature=0.2, 
                timeout=12
            ), "Flash"
        except:
            pass
    
    # Default to Pro for unknown queries
    try:
        return ChatGoogleGenerativeAI(
            model="models/gemini-1.5-pro",
            temperature=0.3,
            timeout=18
        ), "Pro"
    except ValueError as e:
        print(f"Fallback to Flash due to error: {e}")
        return ChatGoogleGenerativeAI(
            model="models/gemini-1.5-flash",
            temperature=0.2,
            timeout=12
        ), "Flash"

# Initialize default LLM
llm, model_name = get_llm_for_query("default")
print(f"Hybrid LLM system initialized (Default: {model_name})")

# Cải thiện memory buffer - tăng token limit
memory = ConversationBufferMemory(
    memory_key="chat_history",
    return_messages=True,
    output_key="answer",
    input_key="question",
    max_token_limit=2000  # Tăng từ 1000 lên 2000 để giữ được context tốt hơn
)

# Balanced prompt template - thân thiện nhưng ngắn gọn
improved_prompt = PromptTemplate(
    input_variables=["context", "question", "all_categories", "chat_history"],
    template="""You are a friendly EvoCasa furniture consultant. Be helpful, warm, but concise and natural - like talking to a friend who works at a furniture store.

Context: {context}
Categories: {all_categories}
Previous chat: {chat_history}
Question: {question}

RESPONSE STYLE:
- Be conversational and friendly, not formal or robotic
- Give key information clearly without overwhelming details
- Answer the main question first, then add 1-2 helpful extras if relevant
- Keep responses natural length - not too short, not essay-long

FORMATS:
- Prices: "[Product] costs [price]. It's [brief description - style/material]. [One key feature]. Need more details?"
- Returns: "Sure! You have [timeframe] to return items in original condition with receipt. Contact our team at [contact] - they'll walk you through it. What product are you looking to return?"
- General: Answer directly, add context if helpful, offer next steps naturally

If no info available: "I don't have those details right now, but our team at support@evocasa.com or 1800-XXX-XXX can help you out! Anything else I can check for you?"

Keep it natural and helpful - like a good salesperson, not a manual!

Answer:"""
)

# Dynamic chain creation function
def create_qa_chain(llm_instance):
    """Tạo chain với LLM được chỉ định"""
    return ConversationalRetrievalChain.from_llm(
        llm=llm_instance,
        retriever=retriever,
        memory=memory,
        chain_type="stuff",
        return_source_documents=False,
        output_key="answer",
        combine_docs_chain_kwargs={"prompt": improved_prompt}
    )

# Initialize default chain
qa_chain = create_qa_chain(llm)

# Pre-load categories khi khởi động
print("📋 Pre-loading categories...")
categories_preload = get_all_categories_cached()
categories_text = "\n• ".join(categories_preload) if categories_preload else "No categories found"

# Welcome message
print("=" * 70)
print("🏠 EVOCASA ASSISTANT - BALANCED & FRIENDLY 😊")
print("=" * 60)
print("Perfect balance features:")
print("• 💬 Natural, conversational responses")
print("• 🎯 Key information without overwhelming details") 
print("• 🤖 Smart model selection (Flash/Pro)")
print("• ⚡ Fast response with quality answers")
print("• 📚 Right amount of context (4 documents)")
print("• 🧠 Good conversation memory")
print("-" * 70)
print("💬 Ask me anything about EvoCasa furniture! (type 'exit' to quit)")
print("-" * 70)

# Performance tracking
query_count = 0
total_response_time = 0

while True:
    query = input("\n🙋 You: ").strip()
    
    if query.lower() == "exit":
        print("\n🤖 EvoCasa Assistant: Thank you so much for visiting EvoCasa today! 🌟")
        print("It was wonderful helping you explore our furniture collection.")
        print("Remember, I'm always here whenever you need assistance with your home furnishing needs.")
        if query_count > 0:
            avg_response_time = total_response_time / query_count
            print(f"📊 Session Summary: {query_count} questions answered, average response time: {avg_response_time:.2f}s")
        print("Have a fantastic day! 👋")
        break
    
    if not query:
        print("\n🤖 EvoCasa Assistant: I'm here and ready to help! 😊 Please feel free to ask me anything about our furniture collection, pricing, policies, or let me know how I can assist you today!")
        continue
    
    start_time = time.time()
    query_count += 1
    
    # Check for common responses first
    common_response = check_common_response(query)
    if common_response:
        response_time = time.time() - start_time
        total_response_time += response_time
        print(f"\n🤖 EvoCasa Assistant: {common_response}")
        continue
    
    # Show loading message
    print("\n🤖 Analyzing your question and gathering information... ⏳", end="", flush=True)
    
    try:
        # Intelligent model selection based on query complexity
        selected_llm, model_type = get_llm_for_query(query)
        
        # Create appropriate chain for this query
        current_chain = create_qa_chain(selected_llm)
        
        # Update loading message with model info
        print(f"\r🤖 Processing with {model_type} model... ⏳", end="", flush=True)
        
        # Execute query
        result = current_chain.invoke({
            "question": query, 
            "all_categories": categories_text
        })
        
        response_time = time.time() - start_time
        total_response_time += response_time
        
        # Clear loading message and show result
        print(f"\r🤖 EvoCasa Assistant:")
        print(f"{result['answer']}")
        
        # Performance indicator (chỉ hiển thị nếu chậm)
        if response_time > 10:
            print(f"⏱️ Response time: {response_time:.1f}s")
            
    except Exception as e:
        response_time = time.time() - start_time
        total_response_time += response_time
        print(f"\r🤖 EvoCasa Assistant: I apologize, but I'm experiencing some technical difficulties right now. 😔")
        print("For immediate assistance, please contact our customer service team:")
        print("📧 Email: support@evocasa.com")
        print("📞 Phone: 1800-XXX-XXX")
        print("I'll be back to full functionality shortly. Thank you for your patience!")
        
        if "timeout" in str(e).lower():
            print("⏱️ The request took longer than expected - our team can provide faster assistance via phone or email.")

print("\n🏠 Thank you for choosing EvoCasa - where your home dreams come true! 🌟")