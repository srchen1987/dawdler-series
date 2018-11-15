export DAWDLER_BIN_PATH=$(cd `dirname $0`; pwd)
export MainClass=com.anywide.dawdler.server.bootstarp.Bootstrap
cd ..
export DAWDLER_BASE_PATH=$(cd `dirname $0`; pwd)
cd bin
vm_arguments="-Xms1g -Xmx1g  -Xmn512m";
if [ "$1" = "start" ]
then
#echo "start server\t$vm_arguments\t$DAWDLER_BASE_PATH"
java $vm_arguments -cp .:./*  com.anywide.dawdler.server.bootstarp.Bootstrap start >../logs/console.log 2>&1 &
elif [ "$1" = "run" ]
then
java $vm_arguments -cp .:./*  com.anywide.dawdler.server.bootstarp.Bootstrap run
elif [ "$1" = "stopnow" ]
then
echo "stop server\t$DAWDLER_BASE_PATH"
java $vm_arguments -cp .:./*  com.anywide.dawdler.server.bootstarp.Bootstrap stopnow
elif [ "$1" = "stop" ]
then
echo "stop server\t$DAWDLER_BASE_PATH"
java $vm_arguments -cp .:./*  com.anywide.dawdler.server.bootstarp.Bootstrap stop
else
echo "commands:"
echo   "run               Start dawdler in the current window"
echo   "start             Start dawdler in a separate window"
echo   "stop              Stop dawdler"
echo   "stopnow           Stop dawdler immediately"
fi

