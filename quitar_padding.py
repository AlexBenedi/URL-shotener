import csv

# Abrir el archivo CSV original
with open('tests2.csv', mode='r', newline='') as infile:
    reader = csv.DictReader(infile)
    
    # Crear un nuevo archivo CSV para guardar solo las URLs
    with open('urls.csv', mode='w', newline='') as outfile:
        writer = csv.writer(outfile)
        
        # Escribir el encabezado
        writer.writerow(['url'])
        
        # Escribir las filas con solo la columna 'url'
        for row in reader:
            writer.writerow([row['url']])
