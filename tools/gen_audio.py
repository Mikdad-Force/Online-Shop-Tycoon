"""Generator efek suara & BGM sederhana (WAV mono 22050Hz) tanpa asset eksternal.
Jalankan: python3 tools/gen_audio.py
"""
import math, os, random, struct, wave

SR = 22050
OUT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'assets', 'audio')
os.makedirs(OUT, exist_ok=True)
random.seed(42)


def save(name, samples):
    path = os.path.join(OUT, name)
    with wave.open(path, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b''.join(struct.pack('<h', max(-32767, min(32767, int(s * 32767)))) for s in samples))
    print(name, len(samples) / SR, 'detik')


def tone(freq, dur, vol=0.35, kind='sine'):
    n = int(SR * dur)
    out = []
    for i in range(n):
        t = i / SR
        env = min(1.0, t / 0.01) * max(0.0, 1.0 - t / dur) ** 1.5
        if kind == 'square':
            s = 1.0 if math.sin(2 * math.pi * freq * t) >= 0 else -1.0
        elif kind == 'saw':
            s = 2.0 * ((freq * t) % 1.0) - 1.0
        else:
            s = math.sin(2 * math.pi * freq * t)
        out.append(s * env * vol)
    return out


def sweep(f1, f2, dur, vol=0.3):
    n = int(SR * dur)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / SR
        f = f1 + (f2 - f1) * (t / dur)
        phase += 2 * math.pi * f / SR
        out.append(math.sin(phase) * (1 - t / dur) * vol)
    return out


def noise(dur, vol=0.25, decay=1.0, lowpass=0.0):
    n = int(SR * dur)
    out, prev = [], 0.0
    for i in range(n):
        v = random.uniform(-1, 1)
        if lowpass:
            prev = prev * lowpass + v * (1 - lowpass)
            v = prev
        out.append(v * max(0.0, 1 - i / n) ** decay * vol)
    return out


def mix(*parts):
    n = max(len(p) for p in parts)
    out = [0.0] * n
    for p in parts:
        for i, v in enumerate(p):
            out[i] += v
    return out


save('beep.wav', tone(880, .09, .3, 'square') + [0.0] * 800 + tone(1180, .07, .25, 'square'))
save('click.wav', tone(1400, .035, .18, 'square'))
save('tape.wav', noise(.22, .3, .4, .35))
save('box.wav', noise(.18, .35, 1.2, .08))
save('printer.wav', mix(noise(.5, .12, .2, .2), tone(180, .5, .1, 'saw')))
save('step.wav', noise(.12, .22, 2.0, .05))
save('coin.wav', tone(784, .1, .3) + [0.0] * 400 + sweep(988, 1568, .3, .3))
save('error.wav', sweep(440, 160, .3, .28))
save('success.wav', tone(523, .12, .25) + tone(659, .12, .25) + tone(784, .3, .28))

# Engine & rain: loopable-ish
save('truck.wav', [0.18 * math.sin(2 * math.pi * 55 * i / SR) + 0.08 * random.uniform(-1, 1) for i in range(SR * 2)])
save('rain.wav', noise(2.0, .12, .05, .55))

# BGM: 8 detik, progresi C - Am - F - G, arpeggio lembut + bass
melody = [261.63, 329.63, 392.00, 523.25, 440.00, 329.63, 261.63, 392.00,
          349.23, 440.00, 523.25, 698.46, 392.00, 329.63, 293.66, 349.23]
bgm = []
for k, f in enumerate(melody):
    bgm += tone(f, .45, .13)
    if k % 4 == 0:
        bass = [f / 2] * int(SR * .45)
        bgm = mix(bgm, tone(f / 2, .45, .09))
save('bgm.wav', bgm[:SR * 8])
print('Selesai. Output:', os.path.abspath(OUT))
