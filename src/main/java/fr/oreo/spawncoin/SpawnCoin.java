package fr.oreo.spawncoin;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

@Mod(SpawnCoin.ID)
public final class SpawnCoin {
 public static final String ID="spawncoin";
 private static final String RETURN="spawncoin_return";
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(ID);
 public static final DeferredItem<Item> COIN=ITEMS.register("coin",()->new Coin(new Item.Properties().stacksTo(1).fireResistant()));
 public SpawnCoin(IEventBus bus){
  ITEMS.register(bus);
  NeoForge.EVENT_BUS.addListener(SpawnCoin::commands);
  NeoForge.EVENT_BUS.addListener(SpawnCoin::clonePlayer);
 }
 private static void clonePlayer(PlayerEvent.Clone e){
  CompoundTag old=e.getOriginal().getPersistentData();
  if(old.contains(RETURN)) e.getEntity().getPersistentData().put(RETURN,old.getCompound(RETURN).copy());
 }
 private static void commands(RegisterCommandsEvent e){
  e.getDispatcher().register(Commands.literal("spawncoin").requires(s->s.hasPermission(2)).executes(ctx->{
   ServerPlayer p=ctx.getSource().getPlayerOrException();
   ItemStack stack=new ItemStack(COIN.get());
   if(!p.getInventory().add(stack)) p.drop(stack,false);
   ctx.getSource().sendSuccess(()->Component.translatable("message.spawncoin.given"),false);
   return 1;
  }));
 }
 private static class Coin extends Item {
  Coin(Properties properties){super(properties);}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
   ItemStack stack=player.getItemInHand(hand);
   if(level.isClientSide) return InteractionResultHolder.success(stack);
   if(!(player instanceof ServerPlayer p)) return InteractionResultHolder.fail(stack);
   if(p.isPassenger()){message(p,"riding");return InteractionResultHolder.fail(stack);}
   CompoundTag data=p.getPersistentData();
   boolean returning=data.contains(RETURN);
   CompoundTag saved=data.getCompound(RETURN);
   ServerLevel dest; Vec3 target; float yaw,pitch;
   if(returning){
    ResourceLocation id=ResourceLocation.tryParse(saved.getString("dimension"));
    dest=id==null?null:p.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,id));
    if(dest==null){message(p,"missing");return InteractionResultHolder.fail(stack);}
    target=new Vec3(saved.getDouble("x"),saved.getDouble("y"),saved.getDouble("z"));
    yaw=saved.getFloat("yaw");pitch=saved.getFloat("pitch");
   }else{
    dest=p.getServer().overworld();BlockPos pos=dest.getSharedSpawnPos();
    target=new Vec3(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
    yaw=dest.getSharedSpawnAngle();pitch=0;
   }
   BlockPos pos=BlockPos.containing(target);
   if(!Double.isFinite(target.x)||!Double.isFinite(target.y)||!Double.isFinite(target.z)||!dest.getWorldBorder().isWithinBounds(pos)||pos.getY()<dest.getMinBuildHeight()||pos.getY()>=dest.getMaxBuildHeight()-1){message(p,"unsafe");return InteractionResultHolder.fail(stack);}
   dest.getChunkAt(pos);
   if(!dest.noCollision(p,p.getBoundingBox().move(target.subtract(p.position())))||dest.getBlockState(pos).getFluidState().is(net.minecraft.tags.FluidTags.LAVA)){
    message(p,"unsafe");return InteractionResultHolder.fail(stack);
   }
   CompoundTag origin=new CompoundTag();
   if(!returning){
    origin.putString("dimension",p.serverLevel().dimension().location().toString());
    origin.putDouble("x",p.getX());origin.putDouble("y",p.getY());origin.putDouble("z",p.getZ());
    origin.putFloat("yaw",p.getYRot());origin.putFloat("pitch",p.getXRot());
   }
   p.teleportTo(dest,target.x,target.y,target.z,yaw,pitch);
   p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
   if(returning)data.remove(RETURN);else data.put(RETURN,origin);
   p.getCooldowns().addCooldown(this,20);
   message(p,returning?"returned":"spawn");
   return InteractionResultHolder.success(stack);
  }
  private static void message(ServerPlayer p,String key){p.displayClientMessage(Component.translatable("message.spawncoin."+key),true);}
 }
}
