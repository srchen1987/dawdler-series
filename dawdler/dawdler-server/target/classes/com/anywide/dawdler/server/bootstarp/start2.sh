export DAWDLER_BIN_PATH=$(cd `dirname $0`; pwd)
cd ..
export DAWDLER_BASE_PATH=$(cd `dirname $0`; pwd)
cd bin
# java -Xms1024m -Xmx1024m -Xmn512m  -cp .;./* com.roiland.dawdler.bootstarp.DawdlerServer
# java -Xms1g -Xmx1g -XX:ParallelGCThreads=8 -Xss256k -XX:-DisableExplicitGC  -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -cp .;./*
vm_arguments="-Xms1g -Xmx1g  -Xmn512m";
if [ "$1" = "start" ]
then
java $vm_arguments -cp .:./*  com.anywide.dawdler.bootstarp.Bootstrap start >../logs/console.log 2>&1 &
else
java $vm_arguments -cp .:./*  com.anywide.dawdler.bootstarp.Bootstrap
fi
#java $vm_arguments -cp .:./*  com.anywide.dawdler.bootstarp.Bootstrap
#java -Xms1g -Xmx1g  -Xmn512m -cp .:./*  com.anywide.dawdler.bootstarp.Bootstrap
echo "start server\t$vm_arguments\t$DAWDLER_BASE_PATH"
