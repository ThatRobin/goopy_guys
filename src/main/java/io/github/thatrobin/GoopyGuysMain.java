package io.github.thatrobin;

import io.github.thatrobin.entities.SlimeFriendEntity;
import io.github.thatrobin.networking.GoopyGuysC2S;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoopyGuysMain implements ModInitializer {

	public static final String MODID = "goopy_guys";
	public static final Logger LOGGER = LogManager.getLogger(GoopyGuysMain.class);

	public static final EntityType<SlimeFriendEntity> SLIME_FRIEND = Registry.register(
			Registries.ENTITY_TYPE,
			identifier("slime_friend"),
			FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, SlimeFriendEntity::new).dimensions(EntityDimensions.fixed(2.04f, 2.04f)).build()
	);

	@Override
	public void onInitialize() {
		GoopyGuysC2S.register();
		FabricDefaultAttributeRegistry.register(SLIME_FRIEND, HostileEntity.createHostileAttributes().build());
	}

	public static Identifier identifier(String path) {
		return new Identifier(MODID, path);
	}
}
