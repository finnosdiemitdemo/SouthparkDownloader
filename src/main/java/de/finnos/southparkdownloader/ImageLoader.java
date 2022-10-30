package de.finnos.southparkdownloader;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;

public class ImageLoader {
    public static final Color GREEN = new Color(34, 139, 34);
    public static final Color RED = new Color(170, 10, 0);

    public static final Image IMAGE_ICON = Toolkit.getDefaultToolkit().getImage(Helper.class.getClassLoader().getResource("img/icon.png"));
    public static final Icon IMAGE_CHECK = ImageLoader.build(FontAwesome.CHECK, GREEN);
    public static final Icon IMAGE_CROSS = ImageLoader.build(FontAwesome.TIMES, RED);
    public static final Icon IMAGE_DOWNLOAD = ImageLoader.build(FontAwesome.DOWNLOAD);
    public static final Icon IMAGE_DELETE = ImageLoader.build(FontAwesome.TRASH, RED);
    public static final Icon IMAGE_OPEN_FOLDER = ImageLoader.build(FontAwesome.FOLDER_OPEN);
    public static final Icon IMAGE_CONNECT = ImageLoader.build(FontAwesome.LINK);
    public static final Icon IMAGE_CANCEL = ImageLoader.build(FontAwesome.BAN, RED);
    public static final Icon IMAGE_PLAY = ImageLoader.build(FontAwesome.PLAY);
    public static final Icon IMAGE_FIND = ImageLoader.build(FontAwesome.SEARCH);
    public static final Icon IMAGE_REPAIR = ImageLoader.build(FontAwesome.BUG);


    public static Icon build(final FontAwesome icon, final Color color) {
        return IconFontSwing.buildIcon(icon, 16, color);
    }

    public static Icon build(final FontAwesome icon) {
        return build(icon,UIManager.getColor("foreground"));
    }
}
