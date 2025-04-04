package com.berksire.applewood.core.block;

import com.berksire.applewood.core.block.entity.AppleWoodStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.vinery.core.block.FacingBlock;
import net.satisfy.vinery.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public abstract class AppleWoodStorageBlock extends FacingBlock implements EntityBlock {

    public static final SoundEvent event = SoundEvents.WOOD_PLACE;

    public AppleWoodStorageBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AppleWoodStorageBlockEntity shelfBlockEntity) {
            Optional<Tuple<Float, Float>> optional = GeneralUtil.getRelativeHitCoordinatesForBlockFace(hit, state.getValue(FACING), unAllowedDirections());
            if (optional.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                Tuple<Float, Float> ff = optional.get();
                int i = getSection(ff.getA(), ff.getB());
                if (i == Integer.MIN_VALUE) {
                    return InteractionResult.PASS;
                }
                if (!shelfBlockEntity.getInventory().get(i).isEmpty()) {
                    remove(world, pos, player, shelfBlockEntity, i);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                } else {
                    ItemStack stack = player.getItemInHand(hand);
                    if (!stack.isEmpty() && canInsertStack(stack)) {
                        add(world, pos, player, shelfBlockEntity, stack, i);
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    public void add(Level level, BlockPos blockPos, Player player, AppleWoodStorageBlockEntity shelfBlockEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide) {
            SoundEvent soundEvent = getAddSound(level, blockPos, player, i);
            shelfBlockEntity.setStack(i, itemStack.split(1));
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }
    public void remove(Level level, BlockPos blockPos, Player player, AppleWoodStorageBlockEntity shelfBlockEntity, int i) {
        if (!level.isClientSide) {
            ItemStack itemStack = shelfBlockEntity.removeStack(i);
            SoundEvent soundEvent = getRemoveSound(level, blockPos, player, i);
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }

    public SoundEvent getRemoveSound(Level level, BlockPos blockPos, Player player,  int i){
        return event;
    }

    public SoundEvent getAddSound(Level level, BlockPos blockPos, Player player,  int i){
        return event;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AppleWoodStorageBlockEntity shelf) {
                if (world instanceof ServerLevel) {
                    Containers.dropContents(world, pos, shelf.getInventory());
                }
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public abstract int size();

    public abstract ResourceLocation type();

    public abstract Direction[] unAllowedDirections();

    public abstract boolean canInsertStack(ItemStack stack);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AppleWoodStorageBlockEntity(pos, state, size());
    }
    public abstract int getSection(Float x, Float y);
}
