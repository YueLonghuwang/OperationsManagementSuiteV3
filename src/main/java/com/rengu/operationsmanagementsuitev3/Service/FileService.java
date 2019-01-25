package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ChunkEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Repository.FileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 11:03
 **/

@Slf4j
@Service
@Transactional
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // 保存文件块
    public void saveChunk(ChunkEntity chunkEntity, MultipartFile multipartFile) throws IOException {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunk.getParentFile().mkdirs();
        chunk.createNewFile();
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunk));
    }

    // 保存文件信息
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public FileEntity saveFile(File file) throws IOException {
        FileEntity fileEntity = new FileEntity();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        String MD5 = DigestUtils.md5Hex(fileInputStream);
        if (hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_EXISTED + MD5);
        }
        fileEntity.setMD5(MD5);
        fileEntity.setType(FilenameUtils.getExtension(file.getName()));
        fileEntity.setSize(FileUtils.sizeOf(file));
        fileEntity.setLocalPath(file.getAbsolutePath());
        return fileRepository.save(fileEntity);
    }

    // 根据Id删除文件
    public FileEntity deleteFileById(String fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        FileUtils.forceDeleteOnExit(new File(fileEntity.getLocalPath()));
        fileRepository.delete(fileEntity);
        return fileEntity;
    }

    // 检查文件块是否存在
    public boolean hasChunk(ChunkEntity chunkEntity) {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        return chunk.exists() && chunkEntity.getChunkSize() == FileUtils.sizeOf(chunk);
    }

    // 根据Id判断文件是否存在
    public boolean hasFileById(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        return fileRepository.existsById(fileId);
    }

    // 根据Md5判断文件是否存在
    public boolean hasFileByMD5(String MD5) {
        if (StringUtils.isEmpty(MD5)) {
            return false;
        }
        return fileRepository.existsByMD5(MD5);
    }

    // 根据Id查询文件
    public FileEntity getFileById(String fileId) {
        if (!hasFileById(fileId)) {
            throw new RuntimeException(ApplicationMessages.FILE_ID_NOT_FOUND + fileId);
        }
        return fileRepository.findById(fileId).get();
    }

    // 根据MD5查询文件
    public FileEntity getFileByMD5(String MD5) {
        if (!hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_NOT_FOUND + MD5);
        }
        return fileRepository.findByMD5(MD5).get();
    }

    // 合并文件块
    public FileEntity mergeChunks(ChunkEntity chunkEntity) throws IOException {
        if (hasFileByMD5(chunkEntity.getIdentifier())) {
            return getFileByMD5(chunkEntity.getIdentifier());
        } else {
            File file = null;
            String extension = FilenameUtils.getExtension(chunkEntity.getFilename());
            if (StringUtils.isEmpty(extension)) {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + chunkEntity.getIdentifier());
            } else {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + "." + FilenameUtils.getExtension(chunkEntity.getFilename()));
            }
            return mergeChunks(file, chunkEntity);
        }
    }

    private FileEntity mergeChunks(File file, ChunkEntity chunkEntity) throws IOException {
        file.delete();
        file.getParentFile().mkdirs();
        file.createNewFile();
        for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
            File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + i + ".tmp");
            if (chunk.exists()) {
                FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(chunk), true);
            } else {
                throw new RuntimeException(ApplicationMessages.FILE_CHUNK_NOT_FOUND + chunk.getAbsolutePath());
            }
        }
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        if (!chunkEntity.getIdentifier().equals(DigestUtils.md5Hex(fileInputStream))) {
            throw new RuntimeException("文件合并失败，请检查：" + file.getAbsolutePath() + "是否正确。");
        }
        return saveFile(file);
    }
}
