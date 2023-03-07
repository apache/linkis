#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import os, sys, getopt, traceback, json, re
import resource

# set memory
#memoryLimit = long(sys.argv[3])
#resource.setrlimit(resource.RLIMIT_AS,(memoryLimit,memoryLimit))

zipPaths = sys.argv[2]
paths = zipPaths.split(':')
for i in range(len(paths)):
  sys.path.insert(0, paths[i])

from py4j.java_gateway import java_import, JavaGateway, GatewayClient
from py4j.protocol import Py4JJavaError, Py4JNetworkError
import ast
import traceback
import warnings
import signal
import base64
from py4j.java_gateway import JavaGateway
from io import BytesIO
try:
  from StringIO import StringIO
except ImportError:
  from io import StringIO

import time
import threading

# for back compatibility

class Logger(object):
  def __init__(self):
    pass

  def write(self, message):
    intp.appendOutput(message)

  def reset(self):
    pass

  def flush(self):
    pass

class ErrorLogger(object):
    def __init__(self):
        self.out = ""

    def write(self, message):
        intp.appendErrorOutput(message)

    def reset(self):
        self.out = ""

    def flush(self):
        pass

def handler_stop_signals(sig, frame):
  sys.exit("Got signal : " + str(sig))

signal.signal(signal.SIGINT, handler_stop_signals)

_pUserQueryNameSpace = {}
client = GatewayClient(port=int(sys.argv[1]))

#gateway = JavaGateway(client, auto_convert = True)
gateway = JavaGateway(client)

linkisOutput = Logger()
errorOutput = ErrorLogger()
sys.stdout = linkisOutput
sys.stderr = errorOutput
intp = gateway.entry_point

def show_matplotlib(p=None,fmt="png", width="auto", height="auto",
                    **kwargs):
  """Matplotlib show function
  """

  if p==None:
    try:
      import matplotlib
      matplotlib.use('Agg')
      import matplotlib.pyplot
      p=matplotlib.pyplot
    except Exception as e:
      print("Failed to import matplotlib")
      print(e)
      return

  if fmt == "png":
    img = BytesIO()
    p.savefig(img, format=fmt)
    img_str = b"data:image/png;base64,"
    img_str += base64.b64encode(img.getvalue().strip())
    img_tag = "<img src={img} style='width={width};height:{height}'>"
    # Decoding is necessary for Python 3 compability
    img_str = img_str.decode("utf-8")
    img_str = img_tag.format(img=img_str, width=width, height=height)
  elif fmt == "svg":
    img = StringIO()
    p.savefig(img, format=fmt)
    img_str = img.getvalue()
  else:
    raise ValueError("fmt must be 'png' or 'svg'")

  html = "<div style='width:{width};height:{height}'>{img}<div>"
  #print(html.format(width=width, height=height, img=img_str))
  intp.showHTML(html.format(width=width, height=height, img=img_str))
  img.close()

def printlog(obj):
    try:
        intp.printLog(obj)
    except Exception as e:
        print("send log failed")

class PythonContext(object):
  """ A context impl that uses Py4j to communicate to JVM
  """

  def __init__(self, py):
    self.py = py
    self.max_result = 5000
    self._setup_matplotlib()

  def show(self, p, **kwargs):
    if hasattr(p, '__name__') and p.__name__ == "matplotlib.pyplot":
      self.show_matplotlib(p, **kwargs)
    elif type(p).__name__ == "DataFrame": # does not play well with sub-classes
      # `isinstance(p, DataFrame)` would req `import pandas.core.frame.DataFrame`
      # and so a dependency on pandas
      self.show_dataframe(p, **kwargs)
    elif hasattr(p, '__call__'):
      p() #error reporting

  def show_dataframe(self, df, show_index=False, **kwargs):
    """Pretty prints DF using Table Display System
    """
    limit = len(df) > self.max_result
    dt=df.dtypes
    dh=df.columns
    data = gateway.jvm.java.util.ArrayList()
    headers = gateway.jvm.java.util.ArrayList()
    schemas = gateway.jvm.java.util.ArrayList()

    for i in dh:
        headers.append(i)
       # print(dt[i])
        schemas.append(str(dt[i]))
    #for i in dt:
     #   schemas.append(i)
    for i in range(0,len(df)):
        iterms = gateway.jvm.java.util.ArrayList()
        for iterm in df.iloc[i]:
            iterms.append(str(iterm))
        data.append(iterms)
    intp.showDF(data,schemas,headers)

  def show_matplotlib(self, p, fmt="png", width="auto", height="auto",
                      **kwargs):
    """Matplotlib show function
    """
    show_matplotlib(p, fmt,width, height, **kwargs)

  def configure_mpl(self, **kwargs):
    import mpl_config
    mpl_config.configure(**kwargs)

  def _setup_matplotlib(self):
    # If we don't have matplotlib installed don't bother continuing
    try:
      import matplotlib
      matplotlib.use('Agg')
    except ImportError:
      return

def setup_plt_show():
    """Override plt.show to show_matplotlib method
    """
    try:
      import matplotlib
      matplotlib.use('Agg')
      import matplotlib.pyplot
      matplotlib.pyplot.show=show_matplotlib
    except Exception as e:
      print(e)
      return

setup_plt_show()

show = __show__ = PythonContext(intp)
__show__._setup_matplotlib()

intp.onPythonScriptInitialized(os.getpid())

def java_watchdog_thread(sleep=10):
    while True :
        time.sleep(sleep)
        try:
            intp.getKind()
        except Exception as e:
            # Failed to detect java daemon, now exit python process
            #just exit thread see https://stackoverflow.com/questions/905189/why-does-sys-exit-not-exit-when-called-inside-a-thread-in-python
            os._exit(1)
watchdog_thread = threading.Thread(target=java_watchdog_thread)
watchdog_thread.daemon = True
watchdog_thread.start()

while True :
  try:
    req = intp.getStatements()
    if req == None:
      break

    stmts = req.statements().split("\n")
    final_code = None
#     ori_code = req.statements()
#     if ori_code:
#         linkisOutput.write(ori_code)

    for bdp_dwc_s in stmts:
      if bdp_dwc_s == None:
        continue

      # skip comment
      s_stripped = bdp_dwc_s.strip()
      if len(s_stripped) == 0 or s_stripped.startswith("#"):
        continue

      if final_code:
        final_code += "\n" + bdp_dwc_s
      else:
        final_code = bdp_dwc_s

    if final_code:
        compiledCode = compile(final_code, "<string>", "exec")
        eval(compiledCode)

    intp.setStatementsFinished("", False)
  except Py4JJavaError:
    excInnerError = traceback.format_exc() # format_tb() does not return the inner exception
    innerErrorStart = excInnerError.find("Py4JJavaError:")
    if innerErrorStart > -1:
       excInnerError = excInnerError[innerErrorStart:]
    intp.setStatementsFinished(excInnerError + str(sys.exc_info()), True)
  except Py4JNetworkError:
    # lost connection from gateway server. exit
    sys.exit(1)
  except:
    intp.setStatementsFinished(traceback.format_exc(), True)

  linkisOutput.reset()