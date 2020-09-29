import re
import subprocess
from pytube import YouTube
from moviepy.editor import *

f = open("urls.txt", "r")

for url in f:
    video = YouTube(url)
    
    for stream in video.streams.filter(file_extension='mp4', adaptive=True):
        file_name = ""                    
        if(stream.resolution == "720p" and stream.fps == 30):                        
            file_name = re.sub('[^a-zA-Z0-9\n\.]', '', stream.title) + stream.resolution
            stream.download(filename=file_name)
        elif(stream.resolution == "1080p" and stream.fps == 30):            
            file_name = re.sub('[^a-zA-Z0-9\n\.]', '', stream.title) + stream.resolution
            stream.download(filename=file_name)        
        
        if(file_name != ""):            
            clip = VideoFileClip(file_name + ".mp4").subclip(0, 60)            
            clip.write_videofile("CUT_" + file_name + ".mp4")            
            json_file = file_name + ".json"         
            siti_output = open(json_file, "x")
            cmdCommand = "siti -q CUT_" + file_name + ".mp4"   #specify your cmd command
            process = subprocess.Popen(cmdCommand.split(), stdout=siti_output)            
            output, error = process.communicate()            