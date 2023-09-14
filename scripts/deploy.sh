#!/bin/bash

REPOSITORY=/home/ubuntu/app
cd $REPOSITORY
mkdir test

APP_NAME=java
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=build/libs

CURRENT_PID=$(pgrep -f $APP_NAME)

if [ -z $CURRENT_PID ] # CURRENT_PID 값이 없는지 여부 판
then # $CURRENT_PID가 비어 있을 때 실행되는 부분
  echo "> 종료할것 없음."
else # $CURRENT_PID가 비어 있지 않을 때 실행되는 부분
  echo "> kill -9 $CURRENT_PID" 
  kill -9 $CURRENT_PID # 해당 프로세스 강제 종료
  sleep 5 # 5초 대기
fi

echo "> $JAR_PATH 배포"
# nohup을 사용하여 백그라운드에서 Java JAR 파일 실행
mkdir test1
cd $JAR_PATH
nohup java -jar $JAR_PATH > /dev/null 2> /dev/null < /del/null &
