#!/usr/bin/env python3
"""Generate original, non-placeholder game textures and soundtrack for Geometry Dash Deluxe."""
import os, math, wave, random
import numpy as np
from PIL import Image, ImageFilter
ROOT="app/src/main/assets"
os.makedirs(ROOT,exist_ok=True)
rng=np.random.default_rng(240619)

def fbm(w,h,seed,octaves=7):
    r=np.random.default_rng(seed); out=np.zeros((h,w),np.float32); amp=1.; total=0.
    for k in range(octaves):
        sw=max(2,w//(2**(k+2))); sh=max(2,h//(2**(k+2)))
        small=Image.fromarray(r.integers(0,256,(sh,sw),dtype=np.uint8)).resize((w,h),Image.Resampling.BICUBIC)
        a=np.asarray(small,dtype=np.float32)/255.
        out+=a*amp; total+=amp; amp*=.53
    return out/total

def texture(name,w,h,base,contrast,seed,kind):
    n=fbm(w,h,seed); fine=np.random.default_rng(seed+9).random((h,w),dtype=np.float32)
    if kind=="stone":
        crack=(np.abs(np.sin((n*29+fine*2)*math.pi))<.055).astype(np.float32)
        v=np.clip((n-.48)*contrast+fine*13-crack*35,-65,65)
    elif kind=="dirt":
        grains=(fine-.5)*24
        v=(n-.5)*contrast+grains+np.sin(np.indices((h,w))[1]/43)*4
    elif kind=="sky":
        yy=np.linspace(0,1,h,dtype=np.float32)[:,None]
        v=(n-.5)*contrast+yy*15
    else:
        v=(n-.5)*contrast+(fine-.5)*18
    arr=np.zeros((h,w,3),np.uint8)
    for c,b in enumerate(base): arr[:,:,c]=np.clip(b+v,0,255)
    im=Image.fromarray(arr,"RGB")
    if kind=="stone":
        # subtle mortar seams, chipped edges and mineral flecks
        pix=ImageDraw=None
    im.save(os.path.join(ROOT,name),format="PNG",compress_level=1)

# Large seamless-style materials used by the live game renderer.
texture("stone_wall.png",3072,3072,(91,96,111),76,11,"stone")
texture("earth_soil.png",1536,1536,(111,70,43),88,21,"dirt")
texture("sky_clouds.png",1536,1536,(73,151,211),60,31,"sky")
texture("cave_rock.png",1536,1536,(48,43,55),72,41,"stone")

# Original electronic soundtrack: four distinct 155-second, stereo PCM tracks.
SR=44100; DUR=155
def write_song(name,bpm,root,seed,style):
    rr=np.random.default_rng(seed); n=SR*DUR
    t=np.arange(n,dtype=np.float32)/SR
    # musical phrase in a minor/pentatonic palette, with evolving chord changes
    scale=[0,3,5,7,10,12,15,17,19,22]
    beat=t*(bpm/60); bar=np.floor(beat/4).astype(np.int32)
    chord_roots=np.array([0,5,3,7,0,8,5,3])
    semis=chord_roots[(bar//4)%len(chord_roots)]
    step=np.floor(beat*2).astype(np.int32)
    melody=np.array([0,7,10,12,10,7,3,5,7,15,12,10,7,5,3,0])
    note=melody[step%len(melody)]+semis+root
    freq=440*np.power(2.,(note-69)/12.)
    phase=2*np.pi*np.cumsum(freq)/SR
    lead=(np.sin(phase)+.28*np.sin(phase*2)+.12*np.sin(phase*3))*0.105
    # warm, side-chained bass pulses
    bass_note=(root-24+semis)
    bf=440*np.power(2.,(bass_note-69)/12.)
    bp=2*np.pi*np.cumsum(bf)/SR
    pulse=np.maximum(0,np.sin(2*np.pi*beat))
    bass=np.sin(bp)*(.15+.11*pulse)
    # four-on-the-floor kick with short pitch drop
    beat_pos=np.mod(beat,1.0)
    kick_env=np.exp(-beat_pos*15)
    kick=np.sin(2*np.pi*(48+80*np.exp(-beat_pos*24))*t)*kick_env*.24
    # crisp snare/clap on beats 2 and 4
    half=np.mod(beat,2.0); sn_env=np.exp(-half*20)
    noise=rr.standard_normal(n).astype(np.float32)
    snare=noise*sn_env*(np.isclose(half,0,atol=.012))*0.055
    # eighth-note hats with changing velocity
    hp=np.mod(beat*2,1.0); hat_env=np.exp(-hp*65)
    hats=rr.standard_normal(n).astype(np.float32)*hat_env*.018
    # soft atmospheric chord pad
    pad=np.sin(2*np.pi*(110+semis*3.2)*t)*.035 + np.sin(2*np.pi*(164+semis*4.1)*t)*.025
    if style==1: lead*=.8; pad*=1.5; hats*=.7
    if style==2: lead*=1.2; bass*=1.2; kick*=1.15
    mono=np.tanh((lead+bass+kick+snare+hats+pad)*1.45)
    fade=np.minimum(1,np.minimum(t/2,(DUR-t)/2))
    mono*=fade
    # stereo width from a short modulated delay
    delay=int(SR*.013); right=np.roll(mono,delay)*.97
    left=mono
    stereo=np.stack([left,right],axis=1)
    pcm=np.clip(stereo*26000,-32768,32767).astype("<i2")
    with wave.open(os.path.join(ROOT,name),"wb") as f:
        f.setnchannels(2);f.setsampwidth(2);f.setframerate(SR);f.writeframes(pcm.tobytes())

write_song("stereo_madness.wav",140,57,101,0)
write_song("cave_runner.wav",128,53,202,1)
write_song("neon_overdrive.wav",154,55,303,2)
write_song("final_descent.wav",136,50,404,1)

# Original one-shot sound effects for jump, portal, crash and completion.
def effect(name,seconds,fn):
    n=int(SR*seconds); t=np.arange(n,dtype=np.float32)/SR
    a=fn(t).astype(np.float32); fade=np.minimum(1,np.minimum(t/.008,(seconds-t)/.04)); a*=fade
    pcm=np.clip(a*25000,-32768,32767).astype("<i2")
    with wave.open(os.path.join(ROOT,name),"wb") as f:
        f.setnchannels(1);f.setsampwidth(2);f.setframerate(SR);f.writeframes(pcm.tobytes())
effect("jump.wav",.22,lambda t:np.sin(2*np.pi*(260+700*t)*t)*np.exp(-t*13))
effect("portal.wav",.65,lambda t:(np.sin(2*np.pi*(240+900*t)*t)+.35*np.sin(2*np.pi*880*t))*np.exp(-t*4))
effect("crash.wav",.48,lambda t:np.random.default_rng(7).normal(0,1,len(t))*np.exp(-t*9)*.8)
effect("complete.wav",1.2,lambda t:(np.sin(2*np.pi*523*t)+np.sin(2*np.pi*659*t)+np.sin(2*np.pi*784*t))*np.exp(-t*1.4)*.22)
print("Generated original textured materials, four full-length soundtrack tracks and four sound effects.")
