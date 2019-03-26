package org.springframework.beans.factory.support.model;

/**
 * Created by rnthd2 on 2019. 3. 26..
 */
public abstract class AbstractCar implements Car {

	public void on() {
		System.out.println("시동을 켜다");
	}

	public void off() {
		System.out.println("시동을 끄다");
	}
}
