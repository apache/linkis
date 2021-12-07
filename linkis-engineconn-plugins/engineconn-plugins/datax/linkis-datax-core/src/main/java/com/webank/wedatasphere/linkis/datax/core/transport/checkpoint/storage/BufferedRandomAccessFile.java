/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datax.core.transport.checkpoint.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * randomAccessFile for buffer
 * @author davidhua
 *
 */
public class BufferedRandomAccessFile extends RandomAccessFile{

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final int DEFAULT_BUFFER_SIZE = 8921;

    private static final int DEFAULT_CHAR_BUFFER_SIZE = 5120;

    private volatile byte[] buffer;

    private volatile CharBuffer charBuffer;

    private long currentPos = -1L;

    private long stPos = -1L,endPos = -1L;

    private boolean isWrite = false;

    public BufferedRandomAccessFile(File file, String mode)
            throws FileNotFoundException {
        super(file, mode);
        buffer = new byte[DEFAULT_BUFFER_SIZE];
        this.seek(0);
        currentPos = 0L;
    }
    public BufferedRandomAccessFile(String fileName,String mode)
            throws FileNotFoundException {
        super(fileName, mode);
        buffer = new byte[DEFAULT_BUFFER_SIZE];
        this.seek(0);
        currentPos = 0L;
    }

    /**
     * Fill the buffer
     * @param pos
     * @throws IOException
     */
    private void fill(long pos) throws IOException{
        int offset = 0;
        if(pos == stPos){
            return;
        }else{
            offset = super.read(buffer);
        }
        if(offset<=-1) stPos = endPos = -1L;
        else{
            stPos = pos;
            endPos = stPos + offset;
        }
        super.seek(pos);//return the position
    }
    public void seek(long pos){
        try {
            currentPos = pos;
            flush();//flush
            if(pos>-1) super.seek(pos);
            fill(pos);
        } catch (IOException e) {
            throw new RuntimeException("there is a wrong in seeking file!");
        }
    }

    /**
     * Flush the buffer
     */
    public void flush()throws IOException{
        //If the buffer is written,write the buff to the file
        if(isWrite & (stPos|endPos) >= 0){
            super.write(buffer, 0,(int)(endPos-stPos));
            isWrite = false;
        }
        stPos = endPos = -1L;
    }

    /**
     * Read from buffer
     */
    public synchronized int readBuffer() throws IOException{
        long pos = currentPos;
        //In the buffer
        if(pos >= stPos && pos < endPos){
            currentPos++;
            return buffer[(int)(pos-stPos)]&0xff;
        }else{
            seek(endPos);
            if((stPos|endPos) == -1L){
                return -1;
            }
            currentPos ++;
            return buffer[0]&0xff;
        }
    }

    public synchronized int readBuffer(byte[] b) throws IOException{
        return readBuffer(b,0,b.length);
    }

    public synchronized int readBuffer(byte[] b,int off,int len) {
        long pos = currentPos;
        int avail = (int)(endPos - pos);
        if(avail <= 0){
            //No rest size in buffer
            seek(pos);
            if((stPos|endPos) == -1L) return -1;
            avail = (int)(endPos - pos);
        }
        int cut = Math.min(avail, len);
        System.arraycopy(buffer, (int) (pos - stPos), b, off, cut);
        currentPos+=cut;
        while(cut < len){
            seek(endPos);
            if((stPos|endPos) != -1L){
                avail = (int)(endPos - stPos);
                int extra = Math.min(avail, len - cut);
                System.arraycopy(buffer,0, b, off+cut,extra);
                cut+=extra;
                currentPos+=extra;
            }else{
                break;
            }
        }
        return cut;
    }
    public synchronized void writeBuffer(int b) throws IOException{
        long pos = currentPos;
        if(pos >= stPos && pos < stPos + buffer.length){
            //If in the buffer size
            currentPos ++;
            //Overlap the buffer
            buffer[(int)(pos-stPos)] = (byte)b;
            //Enlarge the end position
            if(pos == endPos)
                endPos++;
        }else{
//			flush();
            seek(pos);
            if((stPos|endPos) == -1L){
                stPos = pos;
                endPos = stPos+1;
            }
            currentPos++;
            buffer[0] = (byte)b;
        }
        //Buffer is changed
        isWrite = true;
    }
    public synchronized void writeBuffer(byte[] b) throws IOException{
        writeBuffer(b,0,b.length);
    }

    public synchronized void writeBuffer(byte[] b,int off,int len) {
        long pos = currentPos;
        int avail = (stPos|endPos) == -1L? -1 : (int)(buffer.length - pos+stPos);
        if(avail <= 0){
            seek(pos);
            if((stPos|endPos) == -1L){
                stPos = pos;
                endPos = stPos+1;
            }
            avail = buffer.length;
        }
        int write = Math.min(len, avail);
        System.arraycopy(b, off, buffer, (int) (pos - stPos), write);
        if(pos + write >= endPos) endPos = pos+write;
        //Buffer is changed
        isWrite = true;
        currentPos += write;
        while(write < len){
            long nPos = stPos+buffer.length;
            seek(nPos);
            int extra = Math.min(buffer.length, len - write);
            System.arraycopy(b,off + write, buffer, 0,extra);
            if((stPos|endPos) == -1L){
                stPos = nPos;
                endPos = stPos+extra;
            }
            //Buffer is changed
            isWrite = true;
            write+=extra;
            currentPos+=extra;
        }
    }
    /**
     *Read line from character stream
     */
    public synchronized String readBufferLine() throws IOException{
        if(charBuffer == null){
            charBuffer = CharBuffer.allocate(DEFAULT_CHAR_BUFFER_SIZE);
            //Set limit to 0
            charBuffer.limit(0);
        }
        StringBuilder input = new StringBuilder();
        int lef = 0;
        boolean eol = false;
        while(!eol){
            if(!charBuffer.hasRemaining()){
                lef = implyRead(charBuffer);
                if(lef<0){
                    eol = true;
                }
            }
            while(charBuffer.hasRemaining()&&!eol){
                char c = charBuffer.get();
                switch(c){
                    case '\n':
                        eol = true;
                        break;
                    case '\r':
                        continue;
                    default:
                        input.append(c);
                        break;
                }
            }
        }
        if(input.length() == 0&&(lef == -1)){
            return null;
        }
        return input.toString();
    }
    private int implyRead(CharBuffer buf) {
        if((stPos|endPos) == -1L){
            return -1;
        }
        //Set position to 0, and start to read data written
        buf.rewind();
        //Set limit to max value to try the best to receive the data decoded
        buf.limit(DEFAULT_CHAR_BUFFER_SIZE);
        int st =(int)(currentPos-stPos);
        int end = (int)(endPos-currentPos);
        ByteBuffer in = ByteBuffer.wrap(buffer,st,end);
        CharsetDecoder decoder =
                Charset.forName(DEFAULT_CHARSET).newDecoder();
        boolean endOfInput = false;
        CoderResult result = decoder.decode(in, buf, endOfInput);
        currentPos +=(in.position() - st);
        if(result.isUnderflow()){
            if(currentPos >= endPos){
                seek(currentPos);
            }else if(buf.position()<=0){
                //Because the endOfInput == 0
                if(in.hasRemaining()){
                    seek(currentPos);
                }
            }
        }else if(result.isError()||result.isMalformed()){
            return -1;
        }
        int len = buf.position();
        //Set the limit to the length of data
        buf.limit(len);
        //Set the position to 0
        buf.rewind();
        return len;
    }

    @Override
    public void close() throws IOException {
        if(null != charBuffer){
            charBuffer.clear();
        }
        super.close();
    }
}
