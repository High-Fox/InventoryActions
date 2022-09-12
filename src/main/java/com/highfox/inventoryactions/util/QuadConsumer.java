package com.highfox.inventoryactions.util;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {

	void consume(A a, B b, C c, D d);

}
