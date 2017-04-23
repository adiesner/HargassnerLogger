# HargassnerLogger
Logging RS232 output of Hargassner Pellets Classic




# Developing notes

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


