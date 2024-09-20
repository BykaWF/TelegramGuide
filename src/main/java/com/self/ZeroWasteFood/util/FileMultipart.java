package com.self.ZeroWasteFood.util;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMultipart implements MultipartFile {
    private Path path;

    public FileMultipart(Path path) {
        this.path = path;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getOriginalFilename() {
        return getName();
    }

    @Override
    public String getContentType() {
        try {
            return Files.probeContentType(path);
        }catch (IOException e){
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }


    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        transferTo(dest.toPath());
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        Files.copy(path,dest);
    }
}
