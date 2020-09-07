package indi.data.dto;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.math.LongMath;

import indi.bean.ObjectMapperUtils;
import indi.util.ArrayUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 分块传输下载记录
 * 
 * @author wzh
 * @since 2019.12.12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PartDownloadRecord {
    /** 分块下载记录所占用的大小（）。下载中文件的内容必须从此开始。 */
    public static final Integer RECORD_ALLOCATE_SIZE = 1024 * 1024;// 1M
    /** 末尾的占位符 */
    public static final Byte PADDING_SUFFIX = '0';
    
    
    private String md5;// 下载后文件的md5值，用于继续下载时校验
    private Long totalSize;// 总大小
    private Long partSize;// 分块大小
    private int[] table;// 数组下标代表分块的下标，元素的值为 -1表示未传输，0传输中，1已传输
    private Integer recordSize = RECORD_ALLOCATE_SIZE;
    
    public boolean hadDownloaded(int partSize) {
        // FIXME: 越界警告 （不急。。。）
        return table[partSize] == 1;
    }
    
    public static PartDownloadRecord build(Long totalSize, Long partSize, String md5) {
        PartDownloadRecord record = new PartDownloadRecord();
        
        record.setMd5(md5);
        record.setPartSize(partSize);
        record.setTotalSize(totalSize);
        
        // build init table
        // 用guava的工具类，计算分块的数量（有余数就+1）
        // BigDecimal也能实现该功能，但稍显繁琐
        int partCount = (int) LongMath.divide(totalSize, partSize, RoundingMode.UP);
        // 构建数组
        int[] table =  new int[partCount];
        // 初始化数组，均设为-1
        Arrays.fill(table, -1);
        
        record.setTable(table);
        
        return record;
    }
    
    
    /**
     * 序列化为json格式的字符串，并返回字节数组。用于向下载中文件写入下载信息。返回内容固定长度，不足补0（有现成api），其后接下载内容。
     * 由于方法重载不包括返回值的重载，因此只能每种返回值类型都使用一个专门的方法名。
     * 
     * @see #RECORD_ALLOCATE_SIZE
     * 
     * @return
     * @throws JsonProcessingException 
     */
    public byte[] toBytesAsJson() throws JsonProcessingException {
        // 生成json字符串
        String json = ObjectMapperUtils.getMapper().writeValueAsString(this);
            
        // 获得字节数组
        byte[] bytes = json.getBytes();
        if (bytes.length > RECORD_ALLOCATE_SIZE) {
            throw new IllegalArgumentException("下载记录长度超过上限！(bytes.length=" + bytes.length + ",table.length=" + table.length + ")");
        }
        // 构建新字节数组，不足补0
        bytes = ArrayUtils.copyOf(bytes, RECORD_ALLOCATE_SIZE, PADDING_SUFFIX);
        return bytes;
    }
    
    /**
     * 
     * 
     * @param channel 不会关闭
     * @return
     * @throws IOException
     */
    public static PartDownloadRecord fromJson(FileChannel channel) throws IOException {
        // check for channel
        channel.position(0);
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_ALLOCATE_SIZE);
        channel.read(buffer);
        byte[] bytes = buffer.array();
        

        //去掉占位字节
        bytes = ArrayUtils.truncateSuffix(bytes, (byte)'0');
        try {
            String recordJson = new String(bytes, "utf-8");
            log.debug("解析得到旧下载记录：{}", recordJson);
            return ObjectMapperUtils.getMapper().readValue(recordJson, PartDownloadRecord.class);
        } catch (Exception e) {
            // 无法解析
            log.debug("解析下载记录失败", e);
            return null;
        }
    }
}
