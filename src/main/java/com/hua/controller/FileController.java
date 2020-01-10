package com.hua.controller;

import com.hua.utils.AjaxResult;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static com.hua.utils.AjaxResult.error;
import static com.hua.utils.AjaxResult.success;

/**
 * Created by  花季
 * Creation time  2019/12/25.
 */
@Controller
public class FileController {

    @GetMapping("/index")
    public String index(){
        return "index";
    }

    /**
     *   检查文件存在与否
     * @param fileName  文件名称
     * @return
     */
    @PostMapping("/checkFile")
    @ResponseBody
    public AjaxResult checkFile(@RequestParam(value = "fileName") String fileName) {
        String path = "D:/file/"+fileName;
        File file = new File(path);
        if(file.exists()){
            return success();
        }
        return error();
    }

    /**
     *   检查分片存不存在
     * @param md5File   文件唯一标记
     * @param chunk  当前分片
     * @return
     */
    @PostMapping("/checkChunk")
    @ResponseBody
    public AjaxResult checkChunk(@RequestParam(value = "md5File") String md5File,
                                 @RequestParam(value = "chunk") Long chunk) {
        String path = "D:/file/"+md5File+"/";//分片存放目录
        String chunkName = chunk+ ".tmp";//分片名
        File file = new File(path+chunkName);
        //  判断分片是否存在
        if (file.exists()) {
            return success();
        }
        return error();
    }

    /**
     *  上传
     * @param file  文件
     * @param md5File  文件唯一标记
     * @param chunk  分片
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult upload(@RequestParam(value = "file") MultipartFile file,
                             @RequestParam(value = "md5File") String md5File,
                             @RequestParam(value = "chunk",required= false) Long chunk) { //第几片，从0开始
        String path = "D:/file/"+md5File+"/";
        File dirfile = new File(path);
        if (!dirfile.exists()) {//目录不存在，创建目录
            dirfile.mkdirs();
        }
        String chunkName;
        if(chunk == null) {//表示是小文件，还没有一片
            chunkName = "0.tmp";
        }else {
            chunkName = chunk+ ".tmp";
        }
        String filePath = path+chunkName;
        File savefile = new File(filePath);

        try {
            if (!savefile.exists()) {
                savefile.createNewFile();//文件不存在，则创建
            }
            file.transferTo(savefile);//将文件保存
        } catch (IOException e) {
            return error("上传错误");
        }
        return success();
    }

    /**
     *    合成分片
     * @param chunks   分片总数
     * @param md5File  文件唯一标记
     * @param name   文件名称
     * @return
     * @throws Exception
     */
    @PostMapping("/merge")
    @ResponseBody
    public AjaxResult merge(@RequestParam(value = "chunks",required =false) Long chunks,
                            @RequestParam(value = "md5File") String md5File,
                            @RequestParam(value = "name") String name) throws Exception {
        String path = "D:/file";
        FileOutputStream fileOutputStream = new FileOutputStream(path+"/"+name);  //合成后的文件
        try {
            byte[] buf = new byte[1024];
            for(long i=0;i<chunks;i++) {
                String chunkFile=i+".tmp";
                File file = new File(path+"/"+md5File+"/"+chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len = 0;
                while((len=inputStream.read(buf))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                inputStream.close();
            }
            //删除md5目录，及临时文件
            File directory = new File("D:/file/"+md5File+"/");
            if(directory.isDirectory()){
                FileUtils.deleteDirectory(directory);
            }
        } catch (Exception e) {
            return error("上传错误");
        }finally {
            fileOutputStream.close();
        }
        return success();
    }
}
