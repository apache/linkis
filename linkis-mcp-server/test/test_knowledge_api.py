import pytest
from knowledge import KnowledgeQA

@pytest.fixture(scope="module")
def qa():
    return KnowledgeQA()

def test_qa_add_and_ask(qa):
    qa.add_entry({
        "question": "测试问题",
        "answer": "测试答案",
        "tags": ["test"],
        "aliases": ["测试别名"]
    }, save=False)
    results = qa.ask("测试问题", top_k=1)
    assert results and results[0]["answer"] == "测试答案"

def test_qa_topics(qa):
    topics = qa.topics()
    assert isinstance(topics, list)
