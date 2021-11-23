package com.alibaba.datax.plugin.unstructuredstorage.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author davidhua
 * 2019/6/4
 */
public class ZipCollectOutputStream extends OutputStream {

    private ZipOutputStream zipOutputStream;

    private String entryName;

    private ZipEntry zipEntry;

    public ZipCollectOutputStream(String entryName, OutputStream outputStream,
    String encoding){
        if(null == encoding){
            this.zipOutputStream = new ZipOutputStream(outputStream);
        }else {
            this.zipOutputStream = new ZipOutputStream(outputStream, Charset.forName(encoding));
        }
        this.entryName = entryName;
    }

    public ZipCollectOutputStream(String entryName, OutputStream outputStream){
        this(entryName, outputStream, null);
    }

    @Override
    public void write(int b) throws IOException {
        if(null == zipEntry){
            this.zipEntry = new ZipEntry(entryName);
            this.zipOutputStream.putNextEntry(this.zipEntry);
        }
        this.zipOutputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        this.zipOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        this.zipOutputStream.flush();
    }
}
