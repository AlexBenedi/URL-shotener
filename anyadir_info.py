import re

def extract_name(url):
    match = re.search(r'www\.(.*?)\.', url)
    if match:
        return match.group(1)
    return "unknown"

with open('tests.csv', 'r') as file:
    lines = file.readlines()

modified_lines = []
header = lines[0].strip() + ",generateQRCode,brandedName,isBranded,name\n"
modified_lines.append(header)

for line in lines[1:]:
    url = line.strip()
    nombre_arbitrario = extract_name(url)
    modified_line = f"{url},on,{nombre_arbitrario},true,{nombre_arbitrario}\n"
    modified_lines.append(modified_line)

with open('tests.csv', 'w') as file:
    file.writelines(modified_lines)
