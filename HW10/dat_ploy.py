import serial
import matplotlib.pyplot as plt
import numpy as np

ser = serial.Serial('/dev/ttyACM0',9600)
ser.write('r')
raw = []
maf = []
iir = []
fir = []

for i in range(100):
    serial_line = ser.readline()
    vals = serial_line.split()
    raw.append(vals[1])
    maf.append(vals[2])
    iir.append(vals[3])
    fir.append(vals[4])

ser.close()


plt.plot(raw, label='raw')
plt.plot(maf, label='maf')
plt.plot(iir, label='iir')
plt.plot(fir, label='fir')
plt.legend()
plt.show()

# freq = 100
# rawf = np.fft.fft(raw)
# xf = np.linspace(0.0, 2*freq, 5)
