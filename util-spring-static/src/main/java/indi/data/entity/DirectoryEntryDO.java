package indi.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

/**
 * 目录条目表
 * 
 * @author DragonBoom
 *
 */
@Data
@Entity(name = "DIRECTORY_ENTRY")
public class DirectoryEntryDO {
    @Id
    @GeneratedValue// 设置自增
    private Long id;
    @Column
    private Long parentDirectoryId;
    @Column
    private Long childFileId;
    @Column
    private Long childDirectoryId;
}
