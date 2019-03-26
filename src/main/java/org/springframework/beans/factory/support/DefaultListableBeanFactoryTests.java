package org.springframework.beans.factory.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.model.Avante;
import org.springframework.beans.factory.support.model.Car;

/**
 * Created by rnthd2 on 2019. 3. 26..
 */
public class DefaultListableBeanFactoryTests {
	DefaultListableBeanFactory dlbf;

	@Before
	public void init() {
		dlbf = new DefaultListableBeanFactory();
	}

	@Test
	public void 싱글톤_빈_등록_테스트() {
		dlbf.registerSingleton("avante", new Avante("아방이"));
		dlbf.registerSingleton("avante", new Avante("아방이자식"));
		dlbf.registerSingleton("morning", new Avante("모닝이"));
		Car car = dlbf.getBean("avante", Car.class);
		car.on();
	}
}
