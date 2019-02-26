package defaultListableBeanFactory;

import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rnthd2 on 2019. 2. 19..
 */

/**
 * 공유된 bean 인스턴스의 기본 registry
 */
public class DefaultSingletonBeanRegistryTest extends SimpleAliasRegistryTest implements SingletonBeanRegistry{

	/**
	 * 싱글톤 객체 캐시
	 */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/**
	 * 싱글톤 팩토리 캐시
	 */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/**
	 * todo 초기 싱글톤 객체
	 */
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

	/**
	 * 등록된 싱글톤 집합, 등록된 순서대로
	 */
	private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

	/**
	 * 지금 있는 bean 이름 집합
	 *
	 * newSetFromMap의 매개변수는 Map<String, Boolean> 의 형태
	 * SetFromMap new class 반환
	 *      private final Map<E, Boolean> m;  // The backing map
	 *      private transient Set<E> s;       // Its keySet
	 *
	 */
	private final Set<String> singletonsCurrentlyInCreation =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 * 생성에서 예외된 bean 이름 집합
	 */
	private final Set<String> inCreationCheckExclusions =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 * exception 리스트
	 */
	@Nullable
	private Set<Exception> suppressedExceptions;

	/**
	 * 없앨건지말지
	 */
	private boolean singletonsCurrentlyInDestruction = false;

	/**
	 * 임시 bean 이름들
	 */
	private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

	/**
	 * 유지된 이름들의 map????
	 */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/**
	 * bean에 종속된 이름들
	 */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/**
	 * bean에 부수적인 이름들
	 */
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException{
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(singletonObject, "Singleton object must not be null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if(oldObject != null){
				throw new IllegalStateException("Could not register object [" + singletonObject + "] under bean name " + beanName +" : this is already object [ " + oldObject + "] bound");
			}
			addSingleTon(beanName, singletonObject);
		}

	}

	/**
	 * [추가] 이 팩토리의 싱글턴 캐시에 주어진 싱글턴 객체를
	 * 싱글 톤의 초기 등록 요청
	 * todo this.earlySingletonObjects.remove(beanName) 에는 지운다? >> 초기 싱글톤 객체는 지운다
	 *
	 * @param beanName
	 * @param singletonObject
	 */
	protected void addSingleTon(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.put(beanName, singletonObject);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.add(beanName);
		}
	}

	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory mush not be null");
		synchronized (this.singletonObjects) {
			if(!this.singletonObjects.containsKey(beanName)){
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
				this.registeredSingletons.add(beanName);
			}
		}
	}

	@Override
	@Nullable
	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}

	/**
	 * [조회] name으로 등록된 싱글톤 객체를
	 * 이미 인스턴스화된 싱글톤인지 확인하고
	 * 지금 만들어진 싱글톤의 레퍼런스도 허락한다.
	 *
	 * @param beanName 찾을 애
	 * @param allowEarlyReference  초기 레퍼런스 생성 여부
	 * @return
	 */
	@Nullable
	public Object getSingleton(String beanName, boolean allowEarlyReference) {
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						singletonObject = singletonFactory.getObject();
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return singletonObject;
	}

	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name mush not be null");
		synchronized (this.singletonObjects) {
			Object singletonObject = this.singletonObjects.get(beanName);
			if( singletonObject == null) {
				if(this.singletonsCurrentlyInDestruction){
					/**
					 super("Error creating bean with name '" + beanName + "': " + msg);
					 this.beanName = beanName;
					 this.resourceDescription = null;
					 */
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while singletons of this factory are in destruction " +
							"(Do not request a bean from a BeanFactory in a destory method implementation!");
				}
				if(logger.isDebugEnabled()){
					logger.debug("Creation shared instance of singleton bean '" + beanName + "'");
				}
				beforeSingletonCreation(beanName);
			}
		}
	}

	/**
	 * [확인] 특정 싱글톤 빈이 현재 만들어져 있는지 아닌지
 	 * @param beanName
	 * @return
	 */
	public boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}

	/**
	 * [콜백] 싱글톤 만들기 전에
	 *
	 * @param beanName
	 * todo @see #isSingletonCurrentlyInCreation
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}
}
