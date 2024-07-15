package lain.mods.skins.init.forge;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.impl.LegacyConversion;
import lain.mods.skins.impl.PlayerProfile;
import lain.mods.skins.impl.forge.CustomSkinTexture;
import lain.mods.skins.providers.CrafatarCapeProvider;
import lain.mods.skins.providers.CrafatarSkinProvider;
import lain.mods.skins.providers.CustomServerCapeProvider;
import lain.mods.skins.providers.CustomServerSkinProvider;
import lain.mods.skins.providers.MojangCapeProvider;
import lain.mods.skins.providers.MojangSkinProvider;
import lain.mods.skins.providers.UserManagedCapeProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "offlineskins", useMetadata = true, acceptedMinecraftVersions = "[1.12, 1.13)", certificateFingerprint = "aaaf83332a11df02406e9f266b1b65c1306f0f76")
public class ForgeOfflineSkins
{

    @SideOnly(Side.CLIENT)
    private static boolean SkinPass;
    @SideOnly(Side.CLIENT)
    private static boolean CapePass;
    @SideOnly(Side.CLIENT)
    public static boolean OverrideVanilla;
    @SideOnly(Side.CLIENT)
    private static Set<String> DefaultSkins;
    @SideOnly(Side.CLIENT)
    private static Map<ByteBuffer, CustomSkinTexture> textures;

    @SideOnly(Side.CLIENT)
    public static ResourceLocation bindTexture(GameProfile profile, ResourceLocation result)
    {
        if ((OverrideVanilla || isDefaultSkin(result)) && profile != null)
        {
            ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
            if (skin != null && skin.isDataReady())
                return getOrCreateTexture(skin.getData(), skin).getLocation();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static ResourceLocation generateRandomLocation()
    {
        return new ResourceLocation("offlineskins", String.format("textures/generated/%s", UUID.randomUUID().toString()));
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (CapePass)
            return null;

        if (OverrideVanilla || result == null)
        {
            ISkin skin = SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
            if (skin != null && skin.isDataReady())
                return getOrCreateTexture(skin.getData(), skin).getLocation();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (SkinPass)
            return null;

        if (OverrideVanilla || isDefaultSkin(result))
        {
            ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
            if (skin != null && skin.isDataReady())
                return getOrCreateTexture(skin.getData(), skin).getLocation();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static CustomSkinTexture getOrCreateTexture(ByteBuffer data, ISkin skin)
    {
        if (!textures.containsKey(data))
        {
            CustomSkinTexture texture = new CustomSkinTexture(generateRandomLocation(), data);
            FMLClientHandler.instance().getClient().getTextureManager().loadTexture(texture.getLocation(), texture);
            textures.put(data, texture);

            if (skin != null)
            {
                skin.setRemovalListener(s -> {
                    if (data == s.getData())
                    {
                        FMLClientHandler.instance().getClient().addScheduledTask(() -> {
                            FMLClientHandler.instance().getClient().getTextureManager().deleteTexture(texture.getLocation());
                            textures.remove(data);
                        });
                    }
                });
            }
        }
        return textures.get(data);
    }

    @SideOnly(Side.CLIENT)
    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        ResourceLocation location = getLocationSkin(player, null);
        if (location != null)
        {
            ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
            if (skin != null && skin.isDataReady())
                return skin.getSkinType();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static boolean isDefaultSkin(ResourceLocation location)
    {
        return "minecraft".equals(location.getNamespace()) && DefaultSkins.contains(location.getPath());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleClientTicks(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            World world = Minecraft.getMinecraft().world;
            if (world != null)
            {
                for (EntityPlayer player : world.playerEntities)
                {
                    SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                    SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                }
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            DefaultSkins = ImmutableSet.of("textures/entity/steve.png", "textures/entity/alex.png");
            textures = new WeakHashMap<>();

            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            boolean useMojang = config.getBoolean("useMojang", Configuration.CATEGORY_CLIENT, true, "");
            boolean useCrafatar = config.getBoolean("useCrafatar", Configuration.CATEGORY_CLIENT, true, "");
            boolean useCustomServer = config.getBoolean("useCustomServer", Configuration.CATEGORY_CLIENT, false, "");
            String hostCustomServer = config.getString("hostCustomServer", Configuration.CATEGORY_CLIENT, "http://example.com", "/skins/(uuid|username) and /capes/(uuid|username) will be queried for respective resources");
            if (config.hasChanged())
                config.save();

            SkinProviderAPI.SKIN.clearProviders();
            SkinProviderAPI.SKIN.registerProvider(new UserManagedSkinProvider(Paths.get(".", "cachedImages")).withFilter(LegacyConversion.createFilter()));
            if (useCustomServer)
                SkinProviderAPI.SKIN.registerProvider(new CustomServerSkinProvider().setHost(hostCustomServer).withFilter(LegacyConversion.createFilter()));
            if (useMojang)
                SkinProviderAPI.SKIN.registerProvider(new MojangSkinProvider().withFilter(LegacyConversion.createFilter()));
            if (useCrafatar)
                SkinProviderAPI.SKIN.registerProvider(new CrafatarSkinProvider().withFilter(LegacyConversion.createFilter()));

            SkinProviderAPI.CAPE.clearProviders();
            SkinProviderAPI.CAPE.registerProvider(new UserManagedCapeProvider(Paths.get(".", "cachedImages")));
            if (useCustomServer)
                SkinProviderAPI.CAPE.registerProvider(new CustomServerCapeProvider().setHost(hostCustomServer));
            if (useMojang)
                SkinProviderAPI.CAPE.registerProvider(new MojangCapeProvider());
            if (useCrafatar)
                SkinProviderAPI.CAPE.registerProvider(new CrafatarCapeProvider());

            OverrideVanilla = true;
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

}
