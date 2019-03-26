package org.springframework.beans.factory.support.model;

/**
 * Created by rnthd2 on 2019. 3. 26..
 */
public class Avante extends AbstractCar{
	public Avante() {}

	public Avante(String name) {
		this.name = name;
	}

	private String name;

}