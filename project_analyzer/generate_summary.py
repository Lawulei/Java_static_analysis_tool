import json
import re
import requests

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = "qwen3:4b"

# 向模型发送数据
def model_ollama(prompt):
    payload = {
        "model": MODEL_NAME,
        "prompt": prompt,
        "stream": False
    }
    response = requests.post(OLLAMA_URL, json=payload)
    response.raise_for_status()
    return response.json()['response'].strip()

# 去掉qwen3思考的部分（qwen3会返回思考的内容）
def strip_think(text):
    return re.sub(r"<think>.*?</think>\s*", "", text, flags=re.DOTALL).strip()

def main():
    input_path = "selected_methods.json"
    output_path = "method_summaries.json"

    # 加载json数据
    with open(input_path, "r", encoding="utf-8") as f:
        methods = json.load(f)

    results = []
    for method in methods:
        prompt = (
            f"请用一句话总结以下 Java 方法的功能：\n\n"
            f"{method['code']}"
        )
        print(f"正在总结 {method['className']}.{method['methodName']} ...")
        summary_raw = model_ollama(prompt) # 调用大模型生成摘要
        summary = strip_think(summary_raw) # 去掉摘要中的思考部分
        method['summary'] = summary
        results.append(method)

    # 将所有结果写进json文件
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    print(f"\n所有方法的功能摘要已保存到 {output_path}")

if __name__ == "__main__":
    main()
