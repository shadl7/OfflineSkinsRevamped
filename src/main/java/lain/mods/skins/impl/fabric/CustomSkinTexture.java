package lain.mods.skins.impl.fabric;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import lain.mods.skins.api.interfaces.ISkinTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class CustomSkinTexture extends AbstractTexture implements ISkinTexture
{

    private final Identifier _location;
    private WeakReference<ByteBuffer> _data;

    public CustomSkinTexture(Identifier location, ByteBuffer data)
    {
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        _location = location;
        _data = new WeakReference<>(data);
    }

    @Override
    public ByteBuffer getData()
    {
        return _data.get();
    }

    public Identifier getLocation()
    {
        return _location;
    }

    @Override
    public void load(ResourceManager manager) throws IOException
    {
        clearGlId();

        ByteBuffer buf;
        if ((buf = _data.get()) == null) // gc
            throw new FileNotFoundException(getLocation().toString());

        try (NativeImage image = NativeImage.read(buf.duplicate()))
        {
            synchronized (this)
            {
                TextureUtil.prepareImage(getGlId(), 0, image.getWidth(), image.getHeight());
                image.upload(0, 0, 0, false);
            }
        }
    }

}