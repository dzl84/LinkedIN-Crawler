#!/bin/bash
ROOT=$(dirname $(readlink -f $(which "$0")))/..

# Close all existing Xvfb instances
pids=$(pidof /usr/bin/gvncviewer)
if [ -n "$pids" ]; then
  echo "Killing gvncviewer processes " $pids
  kill -9 $pids
fi
sleep 1s

pids=$(pidof /usr/bin/Xvfb)
if [ -n "$pids" ]; then
  echo "Killing Xvfb processes " $pids
  kill -9 $pids
fi

pids=$(ps -ax | grep selenium | grep -v grep| awk -F ' ' '{print $1}')
if [ -n "$pids" ]; then
  echo "Killing selenium	 processes " $pids
  kill -9 $pids
fi

echo "Starting new Xvfb processes"
VWINDOWS=1
hostname=`hstname`
for i in `seq 1 $VWINDOWS`;
do
  echo "start xvfb"
  # Initialize Xvfb
  Xvfb :$i -screen 0 1024x768x24 &
  sleep 5s

  echo "start x11vnc"
  # Initialize vncserver. 
  # To view the content of this window, please use a vnc client
  x11vnc -ncache 10 -display :$i -nopw &
  sleep 5s

  echo "start selenium-server-standalone"
  port=$((i + 9134))
  DISPLAY=:$i java -Dwebdriver.chrome.driver=/usr/lib/chromedriver -jar $ROOT/lib/selenium-server-standalone-2.53.1.jar -port $port &
done