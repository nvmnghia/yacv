package com.uet.nvmnghia.yacv.parser.helper;

import java.io.FileNotFoundException;
import java.io.InputStream;


public interface InputStreamGenerator {
    InputStream generate() throws FileNotFoundException;
}