package lol.vifez.vouchers.util;

import org.bukkit.ChatColor;

public class CC {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String[] translate(String... messages) {
        String[] translated = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            translated[i] = translate(messages[i]);
        }
        return translated;
    }
}