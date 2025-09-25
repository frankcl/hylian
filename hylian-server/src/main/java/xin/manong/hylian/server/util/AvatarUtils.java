package xin.manong.hylian.server.util;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import xin.manong.hylian.server.common.Constants;
import xin.manong.hylian.server.config.ServerConfig;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.util.FileUtil;
import xin.manong.weapon.base.util.RandomID;

import java.io.InputStream;

/**
 * 头像工具
 *
 * @author frankcl
 * @date 2024-11-09 22:51:14
 */
public class AvatarUtils {

    /**
     * 上传头像
     *
     * @param fileDetail 上传文件信息
     * @param fileInputStream 上传文件流
     * @param ossClient OSS客户端
     * @param serverConfig 服务配置
     * @return 成功返回加签OSS头像地址
     */
    public static String uploadAvatar(FormDataContentDisposition fileDetail, final InputStream fileInputStream,
                                      OSSClient ossClient, ServerConfig serverConfig) {
        String ossKey = upload(fileDetail, fileInputStream, ossClient, serverConfig);
        return ossClient.sign(serverConfig.ossBucket, ossKey);
    }

    /**
     *
     * @param fileDetail 上传文件信息
     * @param fileInputStream 上传文件流
     * @param ossClient OSS客户端
     * @param serverConfig 服务配置
     * @return 成功返回未加签OSS头像地址
     */
    public static String uploadAvatarWithoutSign(FormDataContentDisposition fileDetail,
                                                 final InputStream fileInputStream,
                                                 OSSClient ossClient, ServerConfig serverConfig) {
        String ossKey = upload(fileDetail, fileInputStream, ossClient, serverConfig);
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.key = ossKey;
        ossMeta.region = serverConfig.ossRegion;
        ossMeta.bucket = serverConfig.ossBucket;
        return OSSClient.buildURL(ossMeta);
    }

    /**
     *
     * @param fileDetail 上传文件信息
     * @param fileInputStream 上传文件流
     * @param ossClient OSS客户端
     * @param serverConfig 服务配置
     * @return 成功返回OSS key
     */
    private static String upload(FormDataContentDisposition fileDetail,
                                 final InputStream fileInputStream,
                                 OSSClient ossClient, ServerConfig serverConfig) {
        String suffix = FileUtil.getFileSuffix(fileDetail.getFileName());
        String ossKey = String.format("%s%s%s", serverConfig.ossBaseDirectory,
                Constants.TEMP_AVATAR_DIR, RandomID.build());
        if (StringUtils.isNotEmpty(suffix)) ossKey = String.format("%s.%s", ossKey, suffix);
        if (!ossClient.putObject(serverConfig.ossBucket, ossKey, fileInputStream)) {
            throw new IllegalStateException("上传头像失败");
        }
        return ossKey;
    }
}
