package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties({UploadProperties.class})
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    UploadProperties porp;

    //private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg");
    public String upload(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            //contains:集合中是否包含指定的元素
            if(!porp.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }


            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image==null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }


            //准备目标路径 file.getOriginalFilename():获得文件上传的原名
            //File file1 = new File("F:\\a.code\\ideaHome03\\SpringBoot_Shopping\\SBS_01\\img_upload", file.getOriginalFilename());
            //上传到DFS
            //截取后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            //保存文件到本地
            //file.transferTo(file1);
            //返回路径
            return porp.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
