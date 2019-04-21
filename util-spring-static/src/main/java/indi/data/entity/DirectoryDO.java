package indi.data.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

/**
 * 目录表
 */
@Data
@Entity(name = "DIRECTORY")
public class DirectoryDO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue// 设置自增
    private Long id;
    @Column
    private String path;// 路径 可能会特别长，不能当作主键
    @Column
    private Boolean isDeleted;
    @Column
    private String description;// 描述
    @Column
    private String category;// 标签，用于分类
}
