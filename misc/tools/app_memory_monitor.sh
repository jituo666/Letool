echo "======monitor the memory usage state for process com.xjt.letool======"
while true; do  
adb shell dumpsys meminfo com.xjt.letool
sleep 1s 
done
exit 0
