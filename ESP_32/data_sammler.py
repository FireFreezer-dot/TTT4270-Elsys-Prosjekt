# var for å visualisere data for signalstyrke (dB) og avstand (m) samlet inn fra ESP32-enheten.

import numpy as np
import matplotlib.pyplot as plt

dB = np.array([])
avstand = np.array([])

dB_temp = input("Enter signal strength (dB) or press Enter to finish:")

lengder = np.array([1, 5, 10, 15, 20])

snitt_db = np.zeros(len(lengder))
k = 0

print(f"Lengden til dB: {len(dB)} \nlengden til avstand: {len(avstand)}")

while dB_temp != "":
    dB = np.append(dB, float(dB_temp))
    avstand_temp = float(input("Enter distance (m):"))

    while avstand_temp == 0:
        print("Distance cannot be zero. Please enter a valid distance.")
        avstand_temp = float(input("Enter distance (m):"))

    avstand = np.append(avstand, avstand_temp)

    dB_temp = input("Enter signal strength (dB) or press Enter to finish:")


for i in range(len(lengder)):
    for j in range(len(avstand)):
        if avstand[j] == lengder[i]:
            print(f"Lengden til dB: {len(dB)} \nlengden til avstand: {len(avstand)}")
            snitt_db[i] += dB[j]
            k += 1
    snitt_db[i] /= k
    snitt_db[i] *= -1
    k = 0

plt.bar(lengder, snitt_db)
plt.xlabel('Avstand (m)')
plt.ylabel('Signalstyrke (dB)')
plt.title('Signalstyrke vs Avstand')
plt.show()