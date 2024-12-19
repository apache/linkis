import sys
import ast
import json

def extract_method_names(file_path):
    with open(file_path, 'r') as file:
        code = file.read()
    tree = ast.parse(code)
    method_names = set()
    
    for node in ast.walk(tree):
        if isinstance(node, ast.FunctionDef):
            method_names.add(node.name)
    
    return json.dumps(list(method_names), indent=4)

file_path = sys.argv[1]
print(extract_method_names(file_path))
