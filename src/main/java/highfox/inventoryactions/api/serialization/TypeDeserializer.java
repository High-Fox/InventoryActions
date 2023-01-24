package highfox.inventoryactions.api.serialization;

/**
 * Wrapper for a type of {@link IDeserializer}
 */
public class TypeDeserializer<T> {
	private final IDeserializer<? extends T> deserializer;

	public TypeDeserializer(IDeserializer<? extends T> deserializer) {
		this.deserializer = deserializer;
	}

	/**
	 * Gets the contained {@link IDeserializer}
	 *
	 * @return the contained deserializer
	 */
	public IDeserializer<? extends T> getDeserializer() {
		return this.deserializer;
	}

}
