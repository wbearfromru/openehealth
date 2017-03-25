package be.medx;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.lang.ArrayUtils;
import javax.activation.DataSource;

public class ByteArrayDatasource implements DataSource
{
    private byte[] byteArray;
    private String contentType;
    protected static final String DEFAULT_CONTENT_TYPE;
    
    public ByteArrayDatasource(final byte[] byteArray) {
        this.contentType = ByteArrayDatasource.DEFAULT_CONTENT_TYPE;
        this.byteArray = ArrayUtils.clone(byteArray);
        this.contentType = ByteArrayDatasource.DEFAULT_CONTENT_TYPE;
    }
    
    public ByteArrayDatasource(final byte[] byteArray, final String contentType) {
        this.contentType = ByteArrayDatasource.DEFAULT_CONTENT_TYPE;
        this.byteArray = ArrayUtils.clone(byteArray);
        this.contentType = contentType;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.byteArray);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("This is a read-only datasource");
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }
    
    @Override
    public String getName() {
        throw new UnsupportedOperationException("This is a read-only datasource");
    }
    
    public byte[] getByteArray() {
        return ArrayUtils.clone(this.byteArray);
    }
    
    static {
        DEFAULT_CONTENT_TYPE = MimeType.octectstream.getValue();
    }
}
