from fastapi import FastAPI
from pydantic import BaseModel
from rag_qa import ask_question

app = FastAPI()

class Question(BaseModel):
    question: str

@app.post("/ask")
def ask(q: Question):
    answer = ask_question(q.question)
    return {"answer": answer}
