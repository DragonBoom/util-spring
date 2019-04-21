package indi.data.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import indi.constant.DiskEntryType;
import indi.data.entity.DirectoryDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 描述目录信息
 */
@Getter
@Setter
@ToString(callSuper = true)// 使得转化为字符串时包含继承来的属性
public class DirectoryDTO extends DirectoryEntryDTO {
    private List<DirectoryEntryDTO> entries;// 目录下的所有条目
    private DiskEntryType type = DiskEntryType.DIRECTORY;
    
    public DirectoryDO toEntity() {
        DirectoryDO entity = new DirectoryDO();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}
