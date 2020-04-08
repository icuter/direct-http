package cn.icuter.directhttp.mime;

import cn.icuter.directhttp.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FilePart extends BodyPart {
    private final File file;

    public FilePart(File file) {
        this.file = file;
    }

    @Override
    public void writeBodyTo(OutputStream out) throws Exception {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            IOUtils.readBytesTo(in, out);
        }
    }

    @Override
    public long bodyLength() {
        return file.length();
    }
}
