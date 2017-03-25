// 
// Decompiled by Procyon v0.5.29
// 

package be.medx.utils;

import org.bouncycastle.util.encoders.Base64;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.net.URL;
import java.io.FileOutputStream;
import java.io.File;

import be.medx.exceptions.TechnicalConnectorException;
import be.medx.soap.enums.Charset;

import com.gc.iotools.stream.is.InputStreamFromOutputStream;

import java.io.BufferedInputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.Inflater;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.zip.DeflaterInputStream;
import java.util.zip.Deflater;
import java.io.OutputStream;

import org.apache.commons.lang.Validate;

import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;

public final class ConnectorIOUtils
{
    private static final String BASE64_VALIDATOR_REGEX = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$";
    public static final String COMPRESSION_ALGO_DEFLATE = "deflate";
    public static final String COMPRESSION_ALGO_GZIP = "gz";
    private static CompressorStreamFactory factory;
    private static ArchiveStreamFactory asFactory;
    
    private ConnectorIOUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static byte[] getBytes(final InputStream inputStream) throws TechnicalConnectorException {
        if (inputStream == null) {
            throw new TechnicalConnectorException();
        }
        try {
            return IOUtils.toByteArray(inputStream);
        }
        catch (IOException e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(inputStream);
        }
    }
    
    public static byte[] toBytes(final String content, final Charset charsetName) throws TechnicalConnectorException {
        byte[] bytes = null;
        if (content == null) {
            throw new TechnicalConnectorException();
        }
        if (charsetName == null) {
            throw new TechnicalConnectorException();
        }
        try {
            bytes = content.getBytes(charsetName.getName());
        }
        catch (UnsupportedEncodingException e) {
            throw new TechnicalConnectorException();
        }
        return bytes;
    }
    
    public static String toString(final byte[] message, final Charset charsetName) throws TechnicalConnectorException {
        Validate.notNull((Object)message);
        Validate.notNull((Object)charsetName);
        try {
            return new String(message, charsetName.getName());
        }
        catch (UnsupportedEncodingException e) {
            throw new TechnicalConnectorException();
        }
    }
    
    public static byte[] compress(final byte[] input) throws TechnicalConnectorException {
        return compress(input, "gz");
    }
    
    public static void compress(final InputStream in, final OutputStream out, final String algo) throws TechnicalConnectorException {
        InputStream compressedInputStream = null;
        OutputStream gzippedOut = null;
        try {
            if ("deflate".equalsIgnoreCase(algo)) {
                compressedInputStream = new DeflaterInputStream(in, new Deflater(-1, true));
                IOUtils.copy(compressedInputStream, out);
            }
            else {
                gzippedOut = (OutputStream)ConnectorIOUtils.factory.createCompressorOutputStream(algo, out);
                IOUtils.copy(in, gzippedOut);
            }
        }
        catch (Exception e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(compressedInputStream, gzippedOut);
        }
    }
    
    public static byte[] compress(final byte[] input, final String algo) throws TechnicalConnectorException {
        if (ArrayUtils.isEmpty(input) || StringUtils.isEmpty(algo)) {
            throw new TechnicalConnectorException();
        }
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(input);
            out = new ByteArrayOutputStream();
            compress(in, (OutputStream)out, algo);
            out.flush();
            return out.toByteArray();
        }
        catch (IOException e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(in, out);
        }
    }
    
    public static void decompress(final InputStream in, final OutputStream out, final boolean noWrap) throws TechnicalConnectorException {
        OutputStream decompressed = null;
        try {
            decompressed = new InflaterOutputStream(out, new Inflater(noWrap));
            IOUtils.copy(in, decompressed);
        }
        catch (Exception e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(decompressed);
        }
    }
    
    public static byte[] decompress(final byte[] input) throws TechnicalConnectorException {
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(input);
            return getBytes(decompress(in));
        }
        finally {
            closeQuietly(in);
        }
    }
    
    public static InputStream decompress(final InputStream input) throws TechnicalConnectorException {
        Validate.notNull((Object)input);
        final BufferedInputStream is = new BufferedInputStream(input);
        is.mark(1024);
        try {
            try {
                return (InputStream)ConnectorIOUtils.factory.createCompressorInputStream((InputStream)is);
            }
            catch (Exception ex) {
                is.reset();
                try {
                    return (InputStream)ConnectorIOUtils.asFactory.createArchiveInputStream((InputStream)is);
                }
                catch (Exception ex1) {
                    is.reset();
                    try {
                        return deflater(is, true);
                    }
                    catch (Exception ex2) {
                        is.reset();
                        try {
                            return deflater(is, false);
                        }
                        catch (Exception ex3) {
                        }
                    }
                }
            }
        }
        catch (IOException e2) {
            throw new TechnicalConnectorException();
        }
        throw new TechnicalConnectorException();
    }
    
    private static InputStream deflater(final InputStream is, final boolean noWrap) throws Exception {
        final InputStream result = (InputStream)new InputStreamFromOutputStream<Void>() {
            protected Void produce(final OutputStream sink) throws Exception {
                ConnectorIOUtils.decompress(is, sink, noWrap);
                return null;
            }
        };
        OutputStream fos = null;
        try {
            final File temp = File.createTempFile("connector-io-", ".tmp");
            fos = new FileOutputStream(temp);
            IOUtils.copy(result, fos);
            return new AutoDeleteFileInputStream(temp);
        }
        finally {
            closeQuietly(fos);
        }
    }
    
    @Deprecated
    public static byte[] decompress(final boolean noWrap, final byte[] input) throws TechnicalConnectorException {
        return decompress(input);
    }
    
    public static InputStream getResourceAsStream(final String location) throws TechnicalConnectorException {
        return getResourceAsStream(location, true);
    }
    
    public static String getResourceAsString(final String location) throws TechnicalConnectorException {
        return convertStreamToString(getResourceAsStream(location));
    }
    
    public static byte[] getResourceAsByteArray(final String location) throws TechnicalConnectorException {
        return getBytes(getResourceAsStream(location));
    }
    
    public static InputStream getResourceAsStream(final String location, final boolean bootstrap) throws TechnicalConnectorException {
        if (location == null) {
            throw new TechnicalConnectorException();
        }
        InputStream stream = ConnectorIOUtils.class.getResourceAsStream(location);
        if (stream == null) {
            final File file = new File(location);
            if (!file.exists()) {
                try {
                    if (bootstrap) {
//                       ConfigFactory.getConfigValidator().getConfig();
                    }
                    final URL resource = new URL(location);
                    return resource.openStream();
                }
                catch (Exception e) {
                    throw new TechnicalConnectorException();
                }
            }
            try {
                stream = new FileInputStream(file);
            }
            catch (FileNotFoundException e2) {
                throw new TechnicalConnectorException();
            }
        }
        return stream;
    }
    
    public static File getResourceAsFile(final String location) throws TechnicalConnectorException {
        InputStream in = null;
        OutputStream out = null;
        try {
            final File tempFile = File.createTempFile("connector-io", ".tmp");
            tempFile.deleteOnExit();
            out = new FileOutputStream(tempFile);
            in = getResourceAsStream(location);
            IOUtils.copy(in, out);
            return tempFile;
        }
        catch (IOException e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(in, out);
        }
    }
    
    public static String getResourceFilePath(final String location) throws TechnicalConnectorException {
        if (location == null) {
            throw new TechnicalConnectorException();
        }
        String filePath = null;
        InputStream stream = null;
        try {
            stream = ConnectorIOUtils.class.getResourceAsStream(location);
            if (stream != null) {
                filePath = ConnectorIOUtils.class.getResource(location).getFile();
            }
            else {
                final File file = new File(location);
                if (!file.exists()) {
                    try {
                        final URL resource = new URL(location);
                        filePath = resource.getFile();
                        return filePath;
                    }
                    catch (MalformedURLException e) {
                        throw new TechnicalConnectorException();
                    }
                }
                filePath = location;
            }
        }
        finally {
            closeQuietly(stream);
        }
        return filePath;
    }
    
    public static String convertStreamToString(final InputStream is) throws TechnicalConnectorException {
        if (is == null) {
            throw new TechnicalConnectorException();
        }
        String result;
        try {
            result = IOUtils.toString(is, Charset.UTF_8.getName());
        }
        catch (IOException e) {
            throw new TechnicalConnectorException();
        }
        finally {
            closeQuietly(is);
        }
        return result;
    }
    
    public static void closeQuietly(final Object closeable) {
        try {
            if (closeable != null) {
                final Method closeMethod = closeable.getClass().getMethod("close", (Class<?>[])new Class[0]);
                closeMethod.invoke(closeable, new Object[0]);
            }
        }
        catch (SecurityException e) {}
        catch (NoSuchMethodException e2) {}
        catch (IllegalArgumentException e3) {}
        catch (IllegalAccessException e4) {}
        catch (InvocationTargetException ex) {}
    }
    
    public static void closeQuietly(final Object... closeables) {
        for (final Object closeable : closeables) {
            closeQuietly(closeable);
        }
    }
    
   
    public static byte[] base64Decode(final byte[] input, final boolean recursive) throws TechnicalConnectorException {
        byte[] result = ArrayUtils.clone(input);
        final String content = toString(result, Charset.UTF_8);
        if (content.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$")) {
            result = Base64.decode(result);
            if (recursive) {
                result = base64Decode(result, recursive);
            }
        }
        return result;
    }
    
    static {
        ConnectorIOUtils.factory = new CompressorStreamFactory();
        ConnectorIOUtils.asFactory = new ArchiveStreamFactory();
    }
    
    private static class AutoDeleteFileInputStream extends FileInputStream
    {
        private File file;
        private boolean isClosed;
        private boolean isDeleted;
        
        public AutoDeleteFileInputStream(final File file) throws FileNotFoundException {
            super(file);
            (this.file = file).deleteOnExit();
        }
        
        @Override
        public void close() {
            if (this.isClosed) {
                return;
            }
            this.isClosed = true;
            try {
                super.close();
                this.isDeleted = this.file.delete();
            }
            catch (IOException e) {
            }
            catch (RuntimeException e2) {
            }
        }
    }
}
