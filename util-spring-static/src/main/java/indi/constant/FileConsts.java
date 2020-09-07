package indi.constant;

public class FileConsts {
    
    /** 分块传输相关 */
    public static class Part {
        /** 请求头：分块大小 */
        public static final String PART_SIZE_HEADER = "Part-Size";
        /** 请求头：分块起始位置 */
        public static final String PART_BEGIN_BEGIN_INDEX_HEADER = "Begin";
        
        /** 下载中的文件的后缀 */
        public static final String DOWNLOADING_FILE_SUFFIX = "PartDownloading";
    }
    
    /* 通用字段 */
    
    /** 请求头：文件名（全名，含后缀） */
    public static final String FILE_NAME_HEADER = "File-Name";
    /** 请求头：文件总大小 */
    public static final String FILE_SIZE_HEADER = "Total-Size";
    /** 请求头：MD5值 */
    public static final String FILE_MD5_HEADER = "MD5";
}
