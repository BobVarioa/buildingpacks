package com.bobvarioa.buildingpacks.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.NotNull;

public class Wrench extends Item {
    public Wrench(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var block = level.getBlockState(pos);

        if (block.hasProperty(HorizontalDirectionalBlock.FACING)) {
            var state = block.getValue(HorizontalDirectionalBlock.FACING);
            level.setBlockAndUpdate(pos, block.setValue(HorizontalDirectionalBlock.FACING, cycleDirection(state)));
            return InteractionResult.CONSUME;
        }

        if (block.hasProperty(BlockStateProperties.ROTATION_16)) {
            var state = block.getValue(BlockStateProperties.ROTATION_16);
            level.setBlockAndUpdate(pos, block.setValue(BlockStateProperties.ROTATION_16, state + 1 > RotationSegment.getMaxSegmentIndex() ? 0 : state + 1));
            return InteractionResult.CONSUME;
        }

        if (block.hasProperty(BlockStateProperties.AXIS)) {
            var state = block.getValue(BlockStateProperties.AXIS);
            level.setBlockAndUpdate(pos, block.setValue(BlockStateProperties.AXIS, switch (state) {
                case X -> Direction.Axis.Y;
                case Y -> Direction.Axis.Z;
                case Z -> Direction.Axis.X;
            }));
            return InteractionResult.CONSUME;
        }

        if (block.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            var state = block.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            level.setBlockAndUpdate(pos, block.setValue(BlockStateProperties.HORIZONTAL_AXIS, state == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X));
            return InteractionResult.CONSUME;
        }

        var props = block.getProperties();
        for (var prop : props) {
            if (prop.getValueClass() == Direction.class) {
                var values = prop.getAllValues();
                if (values.count() == 4) {
                    var dir = cycleDirection(((Direction) block.getValue(prop)));
                    level.setBlockAndUpdate(pos, block.setValue((Property<Direction>) prop, dir));
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @NotNull
    private static Direction cycleDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> dir;
        };
    }
}
