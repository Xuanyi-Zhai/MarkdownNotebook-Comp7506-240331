package net.group_29.master.format.keyvalue;

import net.group_29.master.format.plaintext.PlaintextTextConverter;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class KeyValueTextConverter extends PlaintextTextConverter {
    private static final List<String> EXT = Arrays.asList(".yml", ".yaml", ".toml", ".vcf", ".ics", ".ini", ".json", ".zim");

    @Override
    public boolean isFileOutOfThisFormat(String filepath, String extWithDot) {
        return EXT.contains(extWithDot);
    }
}
