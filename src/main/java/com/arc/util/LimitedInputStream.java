package com.arc.util;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {

    private final InputStream delegate;
    private long left;

    public LimitedInputStream(InputStream delegate, long limit) {
        this.delegate = delegate;
        this.left = limit;
    }

    @Override
    public int read() throws IOException {
        if (left <= 0) return -1;
        int b = delegate.read();
        if (b != -1) left--;
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (left <= 0) return -1;
        if (len > left) len = (int) left;
        int read = delegate.read(b, off, len);
        if (read != -1) left -= read;
        return read;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
