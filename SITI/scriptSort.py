import glob
import json
import operator
import pprint

data = []
for f in glob.glob("*.json"):
    jsonFile = json.load(open(f))    
    data.append({
        'input_file': jsonFile['input_file'],
        'avg_si': jsonFile['avg_si'],
        'std_si': jsonFile['std_si'],
        'avg_ti': jsonFile['avg_ti'], 
        'std_ti': jsonFile['std_ti']
        })
    
data.sort(key = lambda k: (float(k['avg_si']), float(k['avg_ti'])))
#pprint.pprint(data)

f = open("SI_TI_sorted.txt", "w")
for d in data:
    f.write(d['input_file'] + " " + 
            str(d['avg_si']) + " " + 
            str(d['std_si']) + " " + 
            str(d['avg_ti']) + " " + 
            str(d['std_ti']) + "\n")    



