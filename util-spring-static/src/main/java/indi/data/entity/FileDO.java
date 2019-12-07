package indi.data.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

/**
 * 基础文件表
 * 
 * @author DragonBoom
 *
 */
@Data
@Entity(name = "FILE")
public class FileDO implements Serializable {
    private static final long serialVersionUID = 2224476433527366988L;
    @Id
    @GeneratedValue// 设置自增
    private Long id;
    @Column
    private String path;// 文件路径 可能会特别长，不能当作主键
    @Column
    private String name;
    @Column
    private Long size;// 文件大小
    @Column
    private byte[] content;// 内容
    @Column
    private Boolean isDeleted;
    @Column
    private String description;// 描述
    @Column
    private String category;// 标签，用于分类
    @Column
    private String contentType;
}
