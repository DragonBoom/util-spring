package indi.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 适用于重命名接口的DTO
 * 
 * @author DragonBoom
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RenameDirectoryEntryDTO {
    private String oldPath;
    private String newName;
}
