package org.apache.linkis.ujes.client.response.image;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    BufferedImage image;

    public ImagePanel(Image image) {
        this.image = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = this.image.getGraphics();
        g.drawImage(image, 0, 0, null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}

