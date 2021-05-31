Scriptis is a data analysis tool based on Linkis. Before deploying Scriptis, you need to deploy Linkis first. For the Linkis deploy document, see: [Linkis DeployDoc](https://github.com/WeBankFinTech/Linkis/blob/master/docs/en_US/ch1/deploy.md)

## 1 Preparation

1. Select the corresponding installation package to download.

2. Unzip the downloaded installation package in the installation directory: unzip wedatasphere-scriptis-0.7.0-dist.zip.

## 2 Deploy

​	There are two deployment methods, automated and manual deployment.

### 2.1 Automated deployment

Go to the frontend directory ```wedatasphere-scriptis``` and edit ```vi config.sh ``` to change the interface address of the frontend and backend port. backend port interface address is the gateway address of linkis.

### (3) Modify and save the configuration file created above

```
# Configuring front-end ports
scriptis_port="8088"

# URL of the backend linkis gateway
linkis_url="http://localhost:20401"

# Scriptis ip address
scriptis_ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')
```

After the modification, run the following command in the directory: ```sudo sh install.sh > install.log 2>&1```

Next, you can access ```http://scriptis_ipaddr:scriptis_port``` directly via Chrome, scriptis_port is the port configured in config.sh and scriptis_ipaddr is the IP of the machine that used for installation.

If encounter access failure,  please check install.log and find out the errors.

### 2.2 Manual deployment

1. Install Nginx: ```sudo yum install nginx -y```

2. Modify the configuration file:```sudo vi /etc/nginx/conf.d/scriptis.conf```

   Add the following:

```
server {
            listen       8080;# Access Port
            server_name  localhost;
            #charset koi8-r;
            #access_log  /var/log/nginx/host.access.log  main;
            location / {
            root   /appcom/Install/scriptis/ROOT; # directory where package decompressed
            #in the fronted
            index  index.html index.html;
            }
            location /ws {#webSocket configure spport
            proxy_pass http://192.168.xxx.xxx:9001;#IP port of the linkis gateway service
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            }
            location /api {
            proxy_pass http://192.168.xxx.xxx:9001;#IP port of the linkis gateway service
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header x_real_ipP $remote_addr;
            proxy_set_header remote_addr $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_http_version 1.1;
            proxy_connect_timeout 4s;
            proxy_read_timeout 600s;
            proxy_send_timeout 12s;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection upgrade;
            }
            #error_page  404              /404.html;
            # redirect server error pages to the static page /50x.html
            #
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
            root   /usr/share/nginx/html;
            }
        }
```

3. Copy the frontend package to the corresponding directory: ```/appcom/Install/scriptis/ROOT; # directory where package decompressed in the frontend```
4. Start service: ```sudo systemctl restart nginx```
5. You can directly access ```http://nginx_ip:nginx_port``` via Chrome after execution.

## 3 FAQs

(1) limitations on the size of files that being uploaded

```
sudo vi /etc/nginx/nginx.conf
```

Change the uploading size:

```
client_max_body_size 200m
```

(2) Interface timeout

```
sudo vi /etc/nginx/conf.d/scriptis.conf
```

Change the interface timeout:

```
proxy_read_timeout 600s
```

