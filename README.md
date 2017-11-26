# HargassnerLogger
Logging RS232 output of Hargassner Pellets Classic




# Related links
- https://www.mikrocontroller.net/topic/134331


# Developing notes

## Setup com port
```
stty -F /dev/ttyUSB0 19200 cs8 -cstopb -parenb
```

## Output of Hargassner

```
pm 0 18.3 73 67 2.1 4.7 31.1 140.0 33.8 0.0 52 125 0 0 100.0 100.0 0 0.0 0.0 0.0 0 8.3 0 64 24 140 140 140.0 140.0 0.0 0.0 100.0 100.0 140 140 140.0 140.0 0.0 0.0 100.0 100.0 140 0.00 0.00 0.0 0.00 0.00 0.00 140.00 0 0 0 0 10 0 0 1 7 0 0 1 6 200 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 48560 5179 1300 10 10 0 8b 2222 900 900 0
tm 2017-04-23 22:33:48
z 22:33:49 Kessel Start 
```

pm ... data \n\rtm <timestamp> \r


Recording of raw data can be done via the following command
```
(stty raw; cat > received.txt) < /dev/ttyUSB0
```

Two sample recordings can be found under
```
src/test/resources/raw_ttyUSB0.txt
src/test/resources/raw_ttyUSB0_withText.txt
```


|Element|Format|Bezeichnung|Einheit|
|---|---|---|---|
|1|Int|Primärluftgebläse %|0 bis 100|
|2|Int|Saugzuggebläse %|0 bis 100|
|3|Float|O2 im Rauchgas %|0 bis 21|
|4|Int|Kesseltemperatur|°C|
|5|Int|Rauchgastemperatur|°C|
|6|Float|Außentemperatur aktuell|°C|
|7|Float|Außentemperatur gemittelt|°C|
|8|Int|Vorlauftemperatur Heizkreis 1|°C|
|9|Int|Vorlauftemperatur Heizkreis 2|°C|
|10|Int|Vorlauftemperatur Heizkreis 1 Soll|°C|
|11|Int|Vorlauftemperatur Heizkreis 2 Soll|°C|
|12|Int|Rücklauf Boiler 2 Puffertemperatur|°C|
|13|Int|Boilertemperatur 1|°C|
|14|Int|Fördermenge %|0 bis 100|
|15|Int|Kesselsolltemperatur|°C|
|16|Float|Unterdruck aktuell|mbav|
|17|Float|Unterdruck gemittelt|mbav|
|18|Float|Unterdruck soll|mbav|
|19|Int|Vorlauftemperatur Heizkreis 3|°C|
|20|Int|Vorlauftemperatur Heizkreis 4|°C|
|21|Int|Vorlauftemperatur Heizkreis 3 Soll|°C|
|22|Int|Vorlauftemperatur Heizkreis 4 Soll|°C|
|23|Int|Boilertemperatur 2 SM|°C|
|24|Int|HK1 FR25 *10|°C|
|25|Int|HK2 FR25 *10|°C|
|26|Int|HK3 FR25 SM *10|°C|
|27|Int|HK4 FR25 SM *10|°C|
|28|Int|Kesselzustand||
|29|Float|Strom Motor Einschubschnecke|A|
|30|Float|Strom Motor Aschenaustragung|A|
|31|Float|Strom Motor Raumaustragung|A|
|32|Hex|Digitalwerk 1||
|33|Hex|Digitalwerk 2||
|34|Hex|Digitalwerk 3||
|35|Hex|Digitalwerk 4||
|36|Hex|Digitalwerk 5||
|37|Hex|Digitalwerk 6||
|38|Hex|Digitalwerk 7||
|39|Hex|Digitalwerk 8||

|Bit|Digitalwerk 1|
|---|---|
|0|Stocker-Lauf|
|1|Stocker-Rückwärts|
|2|RA-Lauf|
|3|RA-Rückwärts|
|4|AA-Lauf|
|5|AA-Rückwärts|

|Bit|Digitalwerk 2|
|---|---|
|0|Motor-Brandschutzklappe|
|1|Zündung-Gebläse|
|2|Zündung-Heizung|
|3|Pumpe Fernleitung 1|
|4|Boilerpumpe 1|
|5|Pumpe-HK1|
|6|Mischer-HK1 auf|
|7|Mischer-HK1 zu|
|8|Pumpe-HK2|
|9|Mischer-HK2 auf|
|10|Mischer-HK2 zu|
|11|Störlampe (Fernleitung 2)|

|Bit|Digitalwerk 3|
|---|---|
|3|ext. Heizkreispumpe SM|
|4|Boilerpumpe 2 SM|
|5|Pumpe-HK3 SM|
|6|Mischer-HK3 auf SM|
|7|Mischer-HK3 zu SM|
|8|Pumpe-HK4 SM|
|9|Mischer-HK4 auf SM|
|10|Mischer-HK4 zu SM|

|Bit|Digitalwerk 4|
|---|---|
|4|STB|
|5|Endschalter Deckel|
|6|Vergaserschalter|
|7|Rost|
|8|Brandschutzklappe|
|9|ext. Heizkreis Anforderung|

|Bit|Digitalwerk 5|
|---|---|
|9|ext. Heizkreis Anforderung SM|

|Bit|Digitalwerk 6|
|---|---|
|0|HK1 FR25 AUTO|
|1|HK1 FR25 NACHT|
|2|HK1 FR25 TAG|
|3|HK2 FR25 AUTO|
|4|HK2 FR25 NACHT|
|5|HK2 FR25 TAG|
|6|HK3 FR25 AUTO SM|
|7|HK3 FR25 NACHT SM|
|8|HK3 FR25 TAG SM|
|9|HK4 FR25 AUTO SM|
|10|HK4 FR25 NACHT SM|
|11|HK4 FR25 TAG SM|

## Remote TTY

When developing on a different machine than the one that is connected to the serial port you 
can rewire the tty over ethernet to your development machine.

Make sure socat is installed
```bash
sudo apt install socat
```

On device (raspberrypi) with rs-232 attached to /dev/ttyUSB0
```
socat tcp-l:54321,reuseaddr,fork file:/dev/ttyUSB0,nonblock,waitlock=/var/run/tty0.lock
```

On your development machine:
```
socat pty,link=$HOME/ttyUSB0,waitslave tcp:raspberrypi:54321
```

On your development machine use the new tty $HOME/ttyUSB0
```
cat $HOME/ttyUSB0
```

## Use RxTx from IDE

Add this parameter to your VM options:
```
-Djava.library.path=/usr/lib/jni
```