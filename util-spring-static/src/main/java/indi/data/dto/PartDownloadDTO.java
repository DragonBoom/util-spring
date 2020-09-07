package indi.data.dto;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import indi.constant.FileConsts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PartDownloadDTO {
    private Long partIndex;
    private InputStream inStream;
    
    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseDTO {
        private String fileName;
        private Long fileSize;
        private InputStream inStream;
        private String md5;// 考虑仅请求第1块数据时才计算、返回
        private Long partSize;
        private Long partIndex;
        private Long contentLength;
        
        /**
         * not close stream
         * 
         * @param httpResponse
         * @return
         * @throws UnsupportedOperationException
         * @throws IOException
         */
        public static ResponseDTO from(HttpResponse httpResponse) throws UnsupportedOperationException, IOException {
            // 获取文件信息
            // 1. 文件名
            Header fileNameHeader = httpResponse.getLastHeader(FileConsts.FILE_NAME_HEADER);
            String fileName = fileNameHeader.getValue();// 文件名
            // 2. 文件总大小
            Header fileSizeHeader = httpResponse.getLastHeader(FileConsts.FILE_SIZE_HEADER);
            long fileSize = Long.parseLong(fileSizeHeader.getValue());// 文件总大小
            // 3. md5
            Header md5Header = httpResponse.getLastHeader(FileConsts.FILE_MD5_HEADER);
            String md5 = md5Header.getValue();
            // 4. 分块大小
            Header partSizeHeader = httpResponse.getLastHeader(FileConsts.Part.PART_SIZE_HEADER);
            Long partSize = Long.parseLong(partSizeHeader.getValue());
            // 5. 当前响应返回的块的下标
            Header indexHeader = httpResponse.getLastHeader(FileConsts.Part.PART_BEGIN_BEGIN_INDEX_HEADER);
            Long partIndex = Long.parseLong(indexHeader.getValue());
            // 实际传输大小
            Header contentLengthHeader = httpResponse.getFirstHeader("Content-Length");
            Long contentLength = Long.parseLong(contentLengthHeader.getValue());
            
            HttpEntity responseEntity = httpResponse.getEntity();
            InputStream inStream = responseEntity.getContent();
            
            return new ResponseDTO(fileName, fileSize, inStream, md5, partSize, partIndex, contentLength);
        }
    }
}
