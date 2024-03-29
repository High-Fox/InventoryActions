package highfox.inventoryactions.util;

import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

public class NbtUtils {
	private static final Pattern NUMBER_RANGE_PATTERN = Pattern.compile("^(\\d*\\.?\\d+)(?=\\.{3}(\\d*\\.?\\d+)$)");
	private static final Byte2ObjectMap<Tag> DEFAULT_TAGS = Byte2ObjectMaps.unmodifiable(new Byte2ObjectArrayMap<>(ImmutableMap.<Byte, Tag>builderWithExpectedSize(13)
			.put((byte)0, EndTag.INSTANCE)
			.put((byte)1, ByteTag.ZERO)
			.put((byte)2, ShortTag.valueOf((short)0))
			.put((byte)3, IntTag.valueOf(0))
			.put((byte)4, LongTag.valueOf(0))
			.put((byte)5, FloatTag.ZERO)
			.put((byte)6, DoubleTag.ZERO)
			.put((byte)7, new ByteArrayTag(new byte[0]))
			.put((byte)8, StringTag.valueOf(""))
			.put((byte)9, new ListTag())
			.put((byte)10, new CompoundTag())
			.put((byte)11, new IntArrayTag(new int[0]))
			.put((byte)12, new LongArrayTag(new long[0]))
			.build()));

	public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag otherTag) {
		if (tag == otherTag || tag == null) {
			return true;
		} else if (otherTag == null || !tag.getClass().equals(otherTag.getClass())) {
			return false;
		} else if (tag instanceof CompoundTag) {
			CompoundTag compound = (CompoundTag) tag;
			CompoundTag otherCompound = (CompoundTag) otherTag;

			for (String key : compound.getAllKeys()) {
				Tag value = compound.get(key);
				Tag otherValue = otherCompound.contains(key) ? otherCompound.get(key) : DEFAULT_TAGS.get(value.getId());

				if (otherValue instanceof NumericTag) {
					double otherNumber = ((NumericTag) otherValue).getAsDouble();

					if (value instanceof NumericTag) {
						double number = ((NumericTag) value).getAsDouble();

						if (!DoubleMath.fuzzyEquals(number, otherNumber, Mth.EPSILON)) {
							return false;
						}
					} else if (value instanceof StringTag) {
						String str = ((StringTag) value).getAsString();
						Pair<OptionalDouble, OptionalDouble> range = rangeFromString(str);

						if (range != null) {
							OptionalDouble minimum = range.getFirst();
							OptionalDouble maximum = range.getSecond();
							boolean validMin = true;
							boolean validMax = true;

							if (minimum.isPresent()) {
								validMin = otherNumber >= minimum.orElseThrow();
							}
							if (maximum.isPresent()) {
								validMax = otherNumber <= maximum.orElseThrow();
							}

							if (validMin && validMax) {
								return true;
							}
						}
					}
				}

				if (!compareNbt(value, otherValue)) {
					return false;
				}
			}

			return true;
		} else if (tag instanceof ListTag) {
			ListTag list = (ListTag) tag;
			ListTag otherList = (ListTag) otherTag;

			if (list.isEmpty()) {
				return otherList.isEmpty();
			} else {
				for(int i = 0; i < list.size(); ++i) {
					Tag element = list.get(i);
					boolean flag = false;

					for(int j = 0; j < otherList.size(); ++j) {
						if (compareNbt(element, otherList.get(j))) {
							flag = true;
							break;
						}
					}

					if (!flag) {
						return false;
					}
				}

				return true;
			}
		} else {
			return tag.equals(otherTag);
		}
	}

	@Nullable
	private static Pair<OptionalDouble, OptionalDouble> rangeFromString(String str) {
		Matcher rangeMatcher = NUMBER_RANGE_PATTERN.matcher(str);

		if (rangeMatcher.find()) {
			OptionalDouble minimum = OptionalDouble.of(Double.parseDouble(rangeMatcher.group(1)));
			OptionalDouble maximum = OptionalDouble.of(Double.parseDouble(rangeMatcher.group(2)));

			return Pair.of(minimum, maximum);
		} else {
			OptionalDouble minimum = OptionalDouble.empty();
			OptionalDouble maximum = OptionalDouble.empty();

			if (str.startsWith(">")) {
				try {
					minimum = OptionalDouble.of(Double.parseDouble(str.substring(1)));
				} catch (NumberFormatException e) {
					if (!minimum.isEmpty()) {
						minimum = OptionalDouble.empty();
					}
				}

			} else if (str.startsWith("<")) {
				try {
					maximum = OptionalDouble.of(Double.parseDouble(str.substring(1)));
				} catch (NumberFormatException e) {
					if (!maximum.isEmpty()) {
						maximum = OptionalDouble.empty();
					}
				}
			}

			if (minimum.isEmpty() && maximum.isEmpty()) {
				return null;
			} else {
				return Pair.of(minimum, maximum);
			}
		}
	}

}
