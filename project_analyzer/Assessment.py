import json
import pandas as pd

with open("method_summaries.json", "r", encoding="utf-8") as f:
    data = json.load(f)

rows = []
for item in data:
    rows.append({
        "类名": item.get("className", ""),
        "方法名": item.get("methodName", ""),
        "摘要": item.get("summary", ""),
        "准确性": "",
        "简洁性": "",
        "覆盖性": "",
        "总分": ""
    })

df = pd.DataFrame(rows)

df.to_excel("评估.xlsx", index=False)

print(df.head())
