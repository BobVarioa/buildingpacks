package com.bobvarioa.buildingpacks.block;

import com.bobvarioa.buildingpacks.block.entity.BlueprintDeskEntity;
import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.register.ModBlockEntities;
import com.bobvarioa.buildingpacks.register.ModCaps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlueprintDesk extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH);

    public BlueprintDesk(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(BlockStateProperties.WATERLOGGED);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean inWater = false;
        if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.WATER)) {
            inWater = true;
        }
        var state = defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection());
        if (inWater) {
            state.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        return state;
    }


    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        return SimpleWaterloggedBlock.super.canPlaceLiquid(pLevel, pPos, pState, pFluid);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(
                Block.box(0, 5, 0, 16, 11, 16),
                Block.box(0, 0, 0, 4, 5, 4),
                Block.box(12, 0, 0, 16, 5, 4),
                Block.box(0, 0, 12, 4, 5, 16),
                Block.box(12, 0, 12, 16, 5, 16)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModBlockEntities.BLUEPRINT_DESK.get().create(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> beType) {
        return beType == ModBlockEntities.BLUEPRINT_DESK.get() ? BlueprintDeskEntity::tick : null;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand == InteractionHand.MAIN_HAND) {
            var cap = player.getCapability(ModCaps.BUILDING_POWERS);
            if (cap.isPresent()) {
                var powers = cap.resolve().get();
                boolean enabled = !powers.hasPower(BuildingPower.DRAFTING_META);
                powers.setPower(BuildingPower.DRAFTING_META, enabled);
                if (!player.isCreative()) {
                    player.getAbilities().flying = enabled;
                    player.getAbilities().mayfly = enabled;
                    player.onUpdateAbilities();
                }

                if (!pLevel.isClientSide) {

                }

                return InteractionResult.CONSUME;
            }
        }
        return super.use(pState, pLevel, pPos, player, pHand, pHit);
    }
}
