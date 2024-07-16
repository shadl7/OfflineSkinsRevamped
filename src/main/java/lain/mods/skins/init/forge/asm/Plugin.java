package lain.mods.skins.init.forge.asm;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("offlineskins")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.skins.init.forge.asm.")
public class Plugin implements IFMLLoadingPlugin
{

    public static boolean runtimeDeobfuscationEnabled = false;

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.skins.init.forge.asm.ASMTransformer" };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

}
