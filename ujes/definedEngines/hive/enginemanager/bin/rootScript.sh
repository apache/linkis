#!/usr/bin/expect -f

set user [lindex $argv 0]
set command [lindex $argv 1]
set timeout -1

spawn sudo su -
expect "~]# "
send "su - $user\r"
expect "~]* "
send "$command \r"
expect "~]* "
send "exit\r"
expect "~]# "
send "exit\r"
expect "~]$ "
