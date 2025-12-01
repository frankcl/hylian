#!/bin/bash

cd `dirname $0`

mkdir log

pid_file="../pid"

if [ -f ${pid_file} ]; then
  prev_pid=`cat ${pid_file}`
  echo "Kill process ${prev_pid}"
  kill -9 ${prev_pid}
fi

exec java -Xlog:gc=info:file=./log/gc.log:uptimemillis,pid:filecount=5,filesize=1m -XX:+UseG1GC \
    -Xms100m -Xmx300m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./log/heap.dump \
    -cp "../lib/*" -Dlog4j.configuration="file:../conf/log4j.properties" \
    xin.manong.hylian.server.Application > stderr.out 2>&1 & disown

echo $! > ${pid_file}