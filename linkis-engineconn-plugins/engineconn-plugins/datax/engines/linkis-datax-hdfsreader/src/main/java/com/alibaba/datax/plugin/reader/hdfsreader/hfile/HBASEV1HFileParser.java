package com.alibaba.datax.plugin.reader.hdfsreader.hfile;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.reader.hdfsreader.HdfsReaderErrorCode;
import com.alibaba.datax.plugin.reader.hdfsreader.Key;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * HFile parser of HBase 1.x
 * @author davidhua
 * 2020/2/24
 */
public class HBASEV1HFileParser implements HFileParser{

    private static final Logger LOG = LoggerFactory.getLogger(HBASEV1HFileParser.class);
    /**
     * File system
     */
    private FileSystem fileSystem;

    private Charset encoding = Charset.defaultCharset();

    public HBASEV1HFileParser(FileSystem fileSystem){
        this.fileSystem = fileSystem;
    }

    public HBASEV1HFileParser(FileSystem fileSystem, String encoding){
        this.fileSystem = fileSystem;
        this.encoding = Charset.forName(encoding);
    }
    @Override
    public void parse(String inputPath, Configuration parseConf, Action action) {
        org.apache.hadoop.conf.Configuration configuration = fileSystem.getConf();
        LOG.info("Start to parse HFile: [" + inputPath + "] in HBASEV1HFileParser");
        try (HFile.Reader reader = HFile.createReader(fileSystem, new Path(inputPath),
                new CacheConfig(configuration), configuration)) {
            HFileScanner scanner = reader.getScanner(true, true);
            if(null == parseConf){
                parseConf = Configuration.from("{}");
                for(String parseColumn : PARSE_COLUMNS){
                    parseConf.set(parseColumn, true);
                }
            }
            reader.loadFileInfo();
            if(scanner.seekTo()) {
                do {
                    //Cell entity
                    Cell cell = scanner.getKeyValue();
                    List<String> sourceList = new ArrayList<>();
                    parseConf.getKeys().forEach(configKey -> {
                        switch(configKey){
                            case Key.HFILE_PARSE_ROW_KEY:
                                sourceList.add(new String(CellUtil.cloneRow(cell), encoding));
                                break;
                            case Key.HFILE_PARSE_FAMILY:
                                sourceList.add(new String(CellUtil.cloneFamily(cell), encoding));
                                break;
                            case Key.HFIEL_PARSE_QUALIFIER:
                                sourceList.add(new String(CellUtil.cloneQualifier(cell), encoding));
                                break;
                            case Key.HFILE_PARSE_VALUE:
                                sourceList.add(new String(CellUtil.cloneValue(cell), encoding));
                                break;
                            case Key.HFILE_TIMESTAMP:
                                sourceList.add(String.valueOf(cell.getTimestamp()));
                                break;
                        }
                    });
                    String[] sourceLine = new String[sourceList.size()];
                    sourceList.toArray(sourceLine);
                    action.process(sourceLine);
                } while (scanner.next());
            }
        } catch (IOException e) {
            String message = "解析读取[" + inputPath + "]";
            throw DataXException.asDataXException(HdfsReaderErrorCode.READ_HFILE_ERROR, message, e);
        }
        //Ignore exception
    }

}
