# Compilation

## Getting Started

### Prerequisites

Install Node.js on your computer. Download Link:  [http://nodejs.cn/download/](http://nodejs.cn/download/). Recommend using the latest stable version.

**Only do this step at the first time.**

### Installation

Run the following commands in terminal:

```
git clone https://github.com/WeBankFinTech/Scriptis.git
cd DataSphereStudio/web
npm install
```

 Commands explanation:

1. Pull remote repository to local:	`git clone https://github.com/WeBankFinTech/Scriptis.git`

2. Change to the root directory of the project:	`cd DataSphereStudio/web`

3. Install all dependencies required for the project:	`npm install`

**Only do this step at the first time.**

### Configuration

You need to make some configurations in your code, such as port address of backend server and socket address of backend server in .env.development file in root directory.

```
// Port address of backend server
VUE_APP_MN_CONFIG_PREFIX=http://yourIp:yourPort/yourPath
// Socket address
VUE_APP_MN_CONFIG_SOCKET=/yourSocketPath
```

You can refer to the official documentation of vue-cli for detailed explanation. [Modes and environment variables](https://cli.vuejs.org/guide/mode-and-env.html#modes)

### Building project

You can run the following command in terminal to build the project:

```
npm run build
```

A folder named "dist" would appear in your project's root directory if the command has run successfully and you can directly put "dist" to your static server.

### How to run

You would need to run the following command in terminal if you want to run project on your local browser and see corresponding effects after making changes to the code.

```
npm run serve
```

Access the application in browser (Chrome recommended) via link: [http://localhost:8080/](http://localhost:8080/) .

Changes you make to the code would dynamically reflect on the
effects shown on browser when using the method described above to run project.

**Notes:  Since frontend and backend are developed separately, when running on local browser you need to allow cross domain access in order to access the port of backend server.**

e.g. Chrome browser:

Configuration in Windows:

1. Close all browser windows.

2. Create a shortcut of chrome,  right-click to choose "properties" , then go to "Shortcut" tab find "Target" and add`--args --disable-web-security --user-data-dir=C:\MyChromeDevUserData`  to it .
3. Use shortcut to open the browser.

Configuration in MacOS:

Run the following command. (You need to replace "yourname" in the path. If it's not working, check the path of MyChromeDevUserData on your machine and copy its path to the place right after "--user-data-dir=")

```
open -n /Applications/Google\ Chrome.app/ --args --disable-web-security --user-data-dir=/Users/yourname/MyChromeDevUserData/
```

### FAQ

#### Failed installation when running npm install

Try to use Taobao npm mirror:

```
npm install -g cnpm --registry=https://registry.npm.taobao.org
```

Next,  run the following command instead of npm install:

```
cnpm install
```

Note that you can still use `npm run serve` and `npm run build` to run and build project.