package xin.manong.hylian.client.core;

/**
 * HTTP响应
 *
 * @author frankcl
 * @date 2025-05-29 11:19:30
 */
public class HTTPResponse {

    public String mimeType;
    public byte[] content;

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
}
