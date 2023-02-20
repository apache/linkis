#The cleaning date must be passed in. And ecm will print the actual effective date log.
end_date=`date -d '-90 days'`
end_date_t=`date -d '-90 days' +%s`
echo "Clean up data before this date:$end_date"
#Directory read ecm engine cache variables
prefix=/data/bdp/linkis/
for userdir in `sudo ls $prefix -F | grep '/$'`
do
#EngineConPublicckDir cannot be cleaned up. Clean it up in the ecm stop script
  if [ "$userdir" != "engineConnPublickDir/" ];then
    for ecdir in `sudo ls $prefix$userdir -F | grep '/$'`
    do
       ecdirpath=$prefix$userdir$ecdir
        #Get the last modification time of the folder, and get the time of the latest modified file under the folder
       modify_time=`sudo find $ecdirpath  -printf "%TY-%Tm-%Td\n" | sort -nr | head -n 1`
       modify_time_t=`date -d "$modify_time" +%s`
       if [ $modify_time_t -lt $end_date_t ]; then
         echo "Delete directory：$ecdirpath Last update time$modify_time"
         sudo rm -rf $ecdirpath
       fi
    done
  fi
done
