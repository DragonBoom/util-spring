package indi.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import indi.constant.StoreType;
import indi.dao.BaseDao;
import indi.dao.FileDao;
import indi.data.Result;
import indi.data.Results;
import indi.data.dto.UploadFileDTO;
import indi.data.entity.FileDO;
import indi.scanner.DiskScanner;
import indi.service.FileService;
import indi.util.BeanUtils;

@Service
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

    @Autowired
    private DiskScanner scanner;
    @Autowired
    private FileDao fileDao;

}
