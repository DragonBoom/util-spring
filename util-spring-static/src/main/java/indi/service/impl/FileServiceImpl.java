package indi.service.impl;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.math.LongMath;

import indi.bean.BeanUtils;
import indi.constant.FileConsts;
import indi.constant.StoreType;
import indi.dao.BaseDao;
import indi.dao.FileDao;
import indi.data.Result;
import indi.data.Results;
import indi.data.dto.PartDownloadDTO;
import indi.data.dto.PartDownloadDTO.ResponseDTO;
import indi.data.dto.PartDownloadRecord;
import indi.data.dto.UploadFileDTO;
import indi.data.entity.FileDO;
import indi.exception.WrapperException;
import indi.io.FileUtils;
import indi.scanner.DiskScanner;
import indi.service.FileService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl extends BasicServiceImpl<FileDO, Long> implements FileService {
    
    @Override
    public BaseDao<FileDO, Long> getDao() {
        return fileDao;
    }

    @Override
    public Result<FileDO> persist(Path path) {
        // get file
        return Results.fromOptional(scanner.scanFile(path), "文件不存在: " + path)
                .map(fileDTO -> {
                    fileDTO.setStoreType(StoreType.DATABASE);
                    FileDO entity = fileDTO.toEntity();
                    
                    fileDao.save(entity);
                    return entity;
                });
    }
    
    @Override
    public Result<FileDO> upload(UploadFileDTO dto) {
        FileDO entity = new FileDO();
        BeanUtils.copyProperties(dto, entity, "id");
        fileDao.save(entity);
        return saveFile(dto.getPath(), dto.getContent())
                .map(args -> entity);
    }
    
    private Result<?> saveFile(Path path, byte[] bytes) {
        try {
            Files.write(path, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            return Results.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Results.error(e.getMessage());
        }
    }
    
    public static final String PART_HEAD_END_MARK = "$$";

    // 考虑记录ip
    @Override
    public Result<?> responseFilePart(Path path, Long partSize, Integer partIndex, HttpServletResponse response) throws IOException {
        log.info("开始处理分块传输请求 {} {}", partSize, partIndex);
        long totalSize = Files.size(path);
        // 计算文件md5值
        String md5 = DigestUtils.md5Hex(Files.newInputStream(path, StandardOpenOption.READ));
        // 设置响应头
        // 生成分块信息
        // 文件名 + 文件总字节 + 开始字节 + 此次长度 + md5
        
        response.setHeader(FileConsts.FILE_NAME_HEADER, path.getFileName().toString());
        response.setHeader(FileConsts.FILE_SIZE_HEADER, Long.toString(totalSize));
        response.setHeader(FileConsts.FILE_MD5_HEADER, md5);
        
        
        response.setHeader(FileConsts.Part.PART_SIZE_HEADER, partSize.toString());
        response.setHeader(FileConsts.Part.PART_BEGIN_BEGIN_INDEX_HEADER, partIndex.toString());
        
        // 设置响应大小为实际可传输大小
        long beginPosition = partIndex * partSize;
        long remainSize = beginPosition + partSize > totalSize ? totalSize - beginPosition : partSize;
        response.setContentLengthLong(remainSize);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);// 字节流
        
        try (WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream());// faker
                FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {// faker too
            // 设置起始位置
            
            // 利用FileChannel现成的api，传输指定范围的字节
            log.debug("transfer to begin at {}  for {} b, index = {}, contentLength = {}", beginPosition, partSize, partIndex, remainSize);
            fileChannel.transferTo(beginPosition, partSize, outChannel);
            log.debug("transfer completed, begin at {}  for {} b, index = {}, contentLength = {}", beginPosition, partSize, partIndex, remainSize);
            // FIXME: 远程主机强迫关闭了一个现有的连接。
        }
        
        return Results.success();
    }
    
    // 存在问题，无法指定文件名（考虑下载前就先获取文件名）
    // 下载中记录是否有必要和文件放一起？
    @Override
    public Result<?> downloadFilePart(String url, Long partSize, Path downloadDirPath) throws Exception {
        // 初始化下载路径
        FileUtils.createDirectoryIfNotExist(downloadDirPath);
        // 发送第一个请求
        log.debug("send first part");
        ResponseDTO responseDTO = sendRequestPart(url, 0L, 0);
        log.debug("send first part complete： {}", responseDTO);
        // if response part size not correct ?
        
        // TODO: 若文件已存在，新建一个，或清空下载中文件，或在下载前先校验。。。
        // 获取下载中文件的路径
        String targetFileName = responseDTO.getFileName();
        Path downloadPath = downloadDirPath.resolve(targetFileName);
        Path downloadingPath = getDownloadingFilePath(targetFileName, downloadDirPath);
        
        log.debug("downloadingPath:{}", downloadingPath);

        // 生成下载记录
        PartDownloadRecord record = PartDownloadRecord.build(responseDTO.getFileSize(), partSize, responseDTO.getMd5());
        
        // 初始化下载中文件
        boolean isModifiedDownloadingFile = initDownloadingFile(downloadingPath, responseDTO, record);
        
        if (!isModifiedDownloadingFile) {
            // 初始化时没有修改下载中文件，即下载中文件可能有效，尝试从下载中文件获取下载记录，从而继续下载
            PartDownloadRecord oldRecord = readPartRecord(downloadingPath, responseDTO, record);
            record = Optional.ofNullable(oldRecord).orElse(record);
        }
        
        // 更新下载中文件
//        updateDownloading(downloadingPath, responseDTO, record, 0, partSize);// since first read at 0
        
        // 开始分块获取文件
        Long fileSize = responseDTO.getFileSize();
        long partCount = LongMath.divide(fileSize, partSize, RoundingMode.UP);
        
        FileChannel downloadingFileChannel = FileChannel.open(downloadingPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        ByteBuffer transferBuffer = ByteBuffer.allocate(2048);// 不重复申请
        try {
            for (int i = 0; i < partCount;) {
                // 发送请求
                responseDTO = sendRequestPart(url, partSize, i);
                
                // 更新下载中文件，返回新下标
                i = updateDownloading(downloadingFileChannel, transferBuffer, responseDTO, record, i, partSize);
            }
        } finally {
            downloadingFileChannel.close();
        }
        
        // 还原下载目标文件 FIXME: 性能极差？？
        log.debug("create download file");
        // 将同一个文件写入它本身
        try (FileChannel downloadChannel = FileChannel.open(downloadPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                FileChannel downloadingChannel = FileChannel.open(downloadingPath, StandardOpenOption.READ);) {
            downloadingChannel.transferTo(record.getRecordSize(), responseDTO.getFileSize(), downloadChannel);
        }
        // 移除下载中文件
        log.debug("download completed, remove downloading file");
        Files.delete(downloadingPath);
        return Results.success();
    }
    
    /**
     * 发送请求，获取指定块的文件。该方法不会关闭响应流
     * 
     * @param url
     * @param partSize
     * @param partIndex
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private ResponseDTO sendRequestPart(String url, Long partSize, Integer partIndex) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(FileConsts.Part.PART_SIZE_HEADER, partSize.toString());
        httpGet.setHeader(FileConsts.Part.PART_BEGIN_BEGIN_INDEX_HEADER, partIndex.toString());
        HttpResponse httpResponse = httpClient.execute(httpGet);
        // 将响应进一步地结构化，转化为DTO （含解析响应头的逻辑）
        try {
            return PartDownloadDTO.ResponseDTO.from(httpResponse);
        } catch (Exception e) {
            throw new WrapperException("解析分块响应失败： " + e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param fileName
     * @param downloadDirPath 下载目录
     * @return
     */
    private Path getDownloadingFilePath(String fileName, Path downloadDirPath) {
        return downloadDirPath.resolveSibling(fileName + FileConsts.Part.DOWNLOADING_FILE_SUFFIX);
    }
    
    /**
     * 尝试重新下载
     */
    private PartDownloadRecord readPartRecord(Path downloadingFilePath, ResponseDTO responseDTO, PartDownloadRecord record) throws IOException {
        FileUtils.createEmptyFileIfNotExist(downloadingFilePath);
        // 校验
        // 有完整的头文件
        PartDownloadRecord oldRecord = null;
        try (FileChannel fileChannel = FileChannel.open(downloadingFilePath, StandardOpenOption.READ);) {
            oldRecord = PartDownloadRecord.fromJson(fileChannel);
        }
        if (oldRecord == null) {
            return null;
        }
        // 比较分块数量，若不一致，无法继续下载
        if (!Objects.equal(oldRecord.getPartSize(), record.getPartSize())) {
            log.debug("旧下载中文件分块大小不符，无法继续下载");
            return null;
        }
        if (!Objects.equal(oldRecord.getTable().length, record.getTable().length)) {
            log.debug("旧下载中文件分块数量不符，无法继续下载");
            return null;
        }
        // FIXME: 文件大小不一致
        // 比较md5
        if (!Objects.equal(oldRecord.getMd5(), record.getMd5())) {
            log.debug("旧下载中文件md5值不同，无法继续下载");
            return null;
        }
        
        return oldRecord;
    }
    
    /**
     * 初始化下载中文件
     * 
     * @return 是否有修改下载中文件
     */
    private boolean initDownloadingFile(Path downloadingFilePath, ResponseDTO responseDTO, PartDownloadRecord record) throws IOException {
        FileUtils.createEmptyFileIfNotExist(downloadingFilePath);
        // 只要下载中文件容量足够就跳过处理，确保能继续下载
        long fileSize = Files.size(downloadingFilePath);
        long requireSize = responseDTO.getFileSize() + record.getRecordSize();
        if (fileSize >= requireSize) {
            log.debug("下载中文件容量足够，不需要填充");
            return false;
        }
        // 填充下载中文件
        log.debug("fill download file...");
        
        byte[] bytes = new byte[requireSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) requireSize];
        while (fileSize < requireSize) {// FIXME: 将超过范围。。。 
            if (requireSize - fileSize < bytes.length) {
                bytes = new byte[(int) (requireSize - fileSize)];
            }
            Files.write(downloadingFilePath, bytes, 
                    StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            fileSize += bytes.length;
        }
        log.debug("fill download file complete");
        return true;
    }
    
    /**
     * 更新下载中文件，将关闭响应；
     * 
     * @param downloadingFilePath
     * @param responseDTO
     * @param record
     * @param partIndex 分块下标
     * @param partSize 分块大小
     * @return 返回最近一个需要下载的下标
     * @throws IOException
     */
    private int updateDownloading(FileChannel fileChannel, ByteBuffer transferBuffer, ResponseDTO responseDTO,
            PartDownloadRecord record, Integer partIndex, Long partSize) throws IOException {
        // TODO: 校验响应块大小与大小 
        log.debug("start update downloading");
        // 根据记录表，判断是否已下载该块
        if (record.hadDownloaded(partIndex)) {
            // 主动断开响应流连接。该操作很占用资源？能否无视该连接，让其自动关闭？测试中
            log.debug("already downloaded, skip");
            
            // 遍历至下一次需要下载的分块下标
            int[] table = record.getTable();
            for (int i = 0; i < table.length; i++) {
                if (table[i] != 1) {
                    return i;
                }
            }
            // over?
            return partIndex + 1;
        }
        // 写入所需数据
        // a. 计算起始位置
        long beginPosition = record.getRecordSize() + partIndex * partSize;
        
        // 不主动关闭流？
        
        ReadableByteChannel readableByteChannel = Channels.newChannel(responseDTO.getInStream());
//        try (InputStream inStream = responseDTO.getInStream();
//                ReadableByteChannel readableByteChannel = Channels.newChannel(inStream);) {
            // b. 利用FileChannel的现成的api传输文件 ?
            log.debug("part download begin at {} for part size {} , contentLength {}", beginPosition, responseDTO.getPartSize(), responseDTO.getContentLength());
            
            // 最后一块会很卡 ？
            // 超过2G会出现问题？
            // 2020.01.01 似乎没必要关闭文件流
            // 初始化位置
            fileChannel.position(beginPosition);
            transferBuffer.clear();
            long transferBytes = 0;
            try {
                while (readableByteChannel.read(transferBuffer) > -1) {
                    transferBytes += transferBuffer.position();
                    transferBuffer.flip();
                    fileChannel.write(transferBuffer);
                    transferBuffer.clear();
                }
            } catch (ConnectionClosedException e) {
                if (transferBytes == responseDTO.getContentLength()) {
                    // 当作下载完成 FIXME:
                    log.debug("throw ConnectionClosedException but already downloaded all response");
                } else {
                    throw e;
                }
                
            }
//            fileChannel.transferFrom(readableByteChannel, beginPosition, partSize);
            
            // FIXME: 性能？
            
            log.debug("part download complete {}, {}/{}", transferBytes, partIndex, record.getTable().length);
            // 更新记录表
            // 更新传输标记
            record.getTable()[partIndex] = 1;//  -1表示未传输，0传输中，1已传输
            // 将记录表写入下载中文件
            byte[] bytes = record.toBytesAsJson();
            // 仅写入头部，不修改其他内容
            fileChannel.position(0);
            fileChannel.write(ByteBuffer.wrap(bytes));
//        }
        
        return partIndex + 1;
    }
    
    @Autowired
    private DiskScanner scanner;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private FileDao fileDao;
    
}
