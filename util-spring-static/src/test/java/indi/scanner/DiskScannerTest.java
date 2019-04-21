package indi.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.data.dto.FileDTO;
import indi.util.TestSeparateExtension;

@ExtendWith(TestSeparateExtension.class)
class DiskScannerTest {
    private static final DiskScanner scanner = new DiskScanner();

    // 测试读取文件
    @Test
    void testForFile() {
        Path path = Paths.get("e:", "for test", "link file test", "test.txt");
        Optional<FileDTO> scanFileOptional = scanner.scanFile(path);
        scanFileOptional.ifPresent(System.out::print);
    }
    
    // 测试读取文件夹
    @Test
    void testForDir() {
        Path path = Paths.get("e:", "for test", "link file test");
        Optional<?> scanFileOptional = scanner.scanDirectory(path);
        scanFileOptional.ifPresent(System.out::print);
    }

}
