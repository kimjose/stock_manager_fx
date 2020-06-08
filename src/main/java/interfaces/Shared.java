package interfaces;

import java.io.File;
import java.util.regex.Pattern;

public interface Shared {
    String LOGO_IMAGE = "file:/"+System.getProperty("user.dir")+ File.separator+"logo.png";
    Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9.%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
}
