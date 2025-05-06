import base64

def image_to_base64(image_path, output_path):
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
        with open(output_path, "w") as out_file:
            out_file.write(encoded_string)

# Example usage
image_path = "E:/javaProjs/floracare-mvp/test.jpg"
output_path = "E:/javaProjs/floracare-mvp/test_base64.txt"
image_to_base64(image_path, output_path)
print(f"Base64 string written to {output_path}")
