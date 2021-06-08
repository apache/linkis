#Configuring front-end ports
dss_port="8088"

#URL of the backend linkis gateway
linkis_url="http://localhost:20401"

#dss ip address
dss_ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')
