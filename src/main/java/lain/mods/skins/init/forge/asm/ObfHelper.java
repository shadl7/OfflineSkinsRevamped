package lain.mods.skins.init.forge.asm;

import org.apache.commons.lang3.Validate;

public class ObfHelper {

    public static ObfHelper newMethod(String obfuscatedName, String methodName, String ownerName, String descriptor) {
        Validate.notNull(methodName);
        Validate.notNull(ownerName);
        Validate.notNull(descriptor);
        ObfHelper result = new ObfHelper();
        result.data[0] = ownerName;
        result.data[1] = methodName;
        result.data[2] = descriptor;
        return result;
    }
    private String[] data = new String[3];

    private ObfHelper() {
    }

    public boolean match(Object... obj) {
        if (obj.length == 2)
            return data[1].equals(obj[0]) && data[2].equals(obj[1]);
        else if (obj.length == 3)
            return data[0].equals(obj[0]) && data[1].equals(obj[1]) && data[2].equals(obj[2]);
        return false;
    }
}

