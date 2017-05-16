---
layout: default
---

## RS232 Output analyzed

This output is printed to the serial interface every 500ms.
<pre><code>{% raw  %}
tm 2017-04-26 15:16:13pm 10 18.3 75 71 8.4 7.6 31.8 140.0 30.4 0.0 48 125 0 72 100.0 100.0 0 0.0 0.0 0.0 0 8.3 0 61 17 140 140 140.0 140.0 0.0 0.0 100.0 100.0 140 140 140.0 140.0 0.0 0.0 100.0 100.0 140 0.00 0.00 0.0 0.00 0.00 0.00 140.00 0 0 0 0 10 0 0 1 7 0 0 1 6 200 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 48680 5192 1287 10 10 0 89 2222 900 900 0 
{% endraw %}
</code></pre>

Same with separators:
<pre><code>{% raw  %}
\n\rtm {timestamp YYYY-DD-MM HH:mm:SS}\npm {space separated data} 
{% endraw %}
</code></pre>


From time to time there is also clear text output:
<pre><code>{% raw  %}
tm 2017-04-26 15:48:12z 15:48:13 Kessel Start 
z 15:48:13 Lambdaheizung ein 
pm 4 18.3 70 63 6.8 6.8 31.3 140.0 31.3 0.0 47 125 ......
tm 2017-04-26 15:48:13z 15:48:13 ES Foerdern 
z 15:48:13 Zündung Start 
z 15:48:13 Zündung Einschub 
{% endraw %}
</code></pre>



|Pos.|Value|Type|Description|Unit|
|---|---|---|---|---|
|1   |10|Int|?  |   |
|2   |18.3|Float|?  |   |
|3   |75|Int| Kessel | °C |
|4   |71|Int| Rauch | °C |
|5   |8.4|Float| Außentemperatur | °C |
|6   |7.6|Float| Außentemperatur gemittelt| °C |
|7   |31.8|Float| Heizkessel 1| °C |
|8   |140.0|Float|?| |
|9   |30.4|Float|Vorlauf Soll| °C |
|10   |0.0|Float|?|  |
|11   |48|Int|Boiler 1| °C |
|12   |125|Int|...| ... |
|83  |49588|Int|Betriebsminuten Einschubschnecke| minutes|
|84  |5289|Int|Pelletsverbrauch gesamt|kg|
|85  |1190|Int|Pelletbestand|kg|
