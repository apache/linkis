# 清理日期必须传参进去。并且ecm要打印实际生效的日期日志。
end_date=`date -d '-90 days'`
end_date_t=`date -d '-90 days' +%s`
echo "清理该日期前的数据$end_date"
# 目录读取ecm 引擎缓存变量 
prefix=/data/bdp/linkis/
for userdir in `sudo ls $prefix -F | grep '/$'`
do
  #engineConnPublickDir不能清理，在ecm stop脚本中进行清理
  if [ "$userdir" != "engineConnPublickDir/" ];then
    for ecdir in `sudo ls $prefix$userdir -F | grep '/$'`
    do
       ecdirpath=$prefix$userdir$ecdir
       #获得文件夹最后修改时间，取文件夹下面最晚修改的文件的时间
       modify_time=`sudo find $ecdirpath  -printf "%TY-%Tm-%Td\n" | sort -nr | head -n 1`
       modify_time_t=`date -d "$modify_time" +%s`
       if [ $modify_time_t -lt $end_date_t ]; then
         echo "删除目录：$ecdirpath 最后更新时间$modify_time"
         sudo rm -rf $ecdirpath
       fi
    done
  fi
done
