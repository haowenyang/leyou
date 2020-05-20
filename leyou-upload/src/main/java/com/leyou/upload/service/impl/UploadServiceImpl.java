package com.leyou.upload.service.impl;

import com.leyou.upload.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
public class UploadServiceImpl implements UploadService {


    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/gif","image/jpeg");

    @Override
    public String uploadImage(MultipartFile file) {
        //1. 判断文件类型是否合法
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (!CONTENT_TYPES.contains(contentType)){
            LOGGER.info("文件类型不合法:{}", originalFilename);
            return null;
        }
        //2. 判断文件内容是否合法
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image==null){
                LOGGER.info("文件内容不合法:{}", originalFilename);
                return null;
            }
            //保存到服务器
            file.transferTo(new File("D:\\我的项目\\leyou\\image"+originalFilename));
            //生成url返回
            return "http://image.leyou.com/" + originalFilename;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
