/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.linkis.ujes.client.response.image

import java.util
import java.util.Base64
import javax.swing.{ImageIcon, JFrame}
import scala.collection.mutable.ArrayBuffer

object ShowImage {

  def showImage(fileContents: Object, width: Int = 1000, height: Int = 1000): Unit = {
    new ShowImages(fileContents).showImage(width, height)
  }

  private class ShowImages(fileContents: Object) {

    private def checkIsValidData: Boolean = fileContents.isInstanceOf[util.ArrayList[Object]]

    private def getArrayList: util.ArrayList[AnyRef] = fileContents.asInstanceOf[util.ArrayList[AnyRef]]

    private def getBase64Data(o: Object): String = {
      val imgData: String = o.toString
      if (imgData.contains(CONTAINS_DATA)) {
        return imgData.split(CONTAINS_DATA)(1).replace(REPLACE_STR, "").split(" ")(0)
      }
      null
    }

    private def putBytes(o: Object, images: ArrayBuffer[Array[Byte]]): Unit = {
      val base64Data = getBase64Data(o)
      if (null != base64Data) {
        val bytes = Base64.getDecoder.decode(base64Data)
        images += bytes
      }
    }

    private def getImageData: ArrayBuffer[Array[Byte]] = {
      val images = new ArrayBuffer[Array[Byte]]()
      if (!checkIsValidData) {
        return images
      }

      for (o <- getArrayList.toArray()) {
        putBytes(o, images)
      }
      images
    }

    def showImage(width: Int, height: Int): Unit = {
      for (img <- getImageData) {
        Image.showImage(img, width, height)
      }
    }

    val CONTAINS_DATA: String = "<img src=data:image/png;base64,"
    val REPLACE_STR: String = "]";

  }


  private object Image {

    def showImage(data: Array[Byte], width: Int, height: Int): Unit = {

      val frame = new JFrame
      frame.setContentPane(new ImagePanel(new ImageIcon(data).getImage))
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      frame.setBounds(100, 100, width, height)
      frame.setVisible(true)

      new Thread(new Runnable() {
        override def run(): Unit = {
          Thread.sleep(5000)
          frame.repaint()
        }
      }).start()

    }
  }
}





