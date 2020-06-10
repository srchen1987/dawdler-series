@echo off
set vm_arguments=-Xms1g -Xmx1g -Xmn512m
set _RUNJAVA=java %vm_arguments% -cp .;./*  com.anywide.dawdler.server.bootstarp.Bootstrap
set "DAWDLER_BIN_PATH=%cd%"
cd ..
set "DAWDLER_BASE_PATH=%cd%"
cd bin
if ""%1"" == """" goto doHelp
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
if ""%1"" == ""stopnow"" goto doStopNow
:doStart
if "%TITLE%" == "" set TITLE=dawdler
set _EXECJAVA=start "%TITLE%" %_RUNJAVA% start
goto execCmd
:doRun
set _EXECJAVA= %_RUNJAVA% run
goto execCmd
:doStop
set _EXECJAVA= %_RUNJAVA% stop
goto execCmd
:doStopNow
set _EXECJAVA= %_RUNJAVA% stopnow
goto execCmd
:execCmd
%_EXECJAVA%
goto end
:doHelp
echo Usage:  dawdler ( commands ... )
echo commands:
echo   run               Start dawdler in the current window
echo   start             Start dawdler in a separate window
echo   stop              Stop dawdler
echo   stopnow           Stop dawdler immediately
goto end
:end
