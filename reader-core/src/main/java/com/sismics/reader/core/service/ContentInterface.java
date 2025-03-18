package com.sismics.reader.core.service;

import java.io.*;

public interface ContentInterface {
    InputStream fetchContent(String api_call);
}