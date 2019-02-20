package defaultListableBeanFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rnthd2 on 2019. 2. 14..
 */

/**
 * {@link AliasRegistryTest} 인터페이스의 간단한 구현
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 의 아버지가 AliasRegistry
 */
public class SimpleAliasRegistryTest implements AliasRegistryTest{

	protected final Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 맵<알리아스, 이름>
	 */
	private final Map<String, String> aliasMap = new ConcurrentHashMap<String, String>(16);

	/**
	 * 이미 이름이 있으면 해당 알리아스를 지움...? 쿨함
	 * 이미 알리아스가 있으면 그냥 return
	 * 이러나저러나 남아있으면 IllegalStateException
	 *
	 * @param name 정식이름
	 * @param alias 등록될 알리아스
	 */
	@Override
	public void registerAlias(String name, String alias){
		Assert.hasText(name,"'name' must not be empty");
		Assert.hasText(alias, "'alas' mush not be empty");
		synchronized (this.aliasMap) {
			if(alias.equals(name)) {
				this.aliasMap.remove(alias);
				if(logger.isDebugEnabled()) {
					logger.debug("Alias definition '" + alias + "' ignored since it points to same name");
				}
			}
			else {
				String registeredName = this.aliasMap.get(alias);
				if( registeredName != null) {
					if(registeredName.equals(name)) {
						// An existing alias
						return;
					}
					/*todo 얘를 좀 봐야될 듯!!*/
					if(!allowAliasOverriding()) {
						throw new IllegalStateException("Cannot define alias '" + alias + "' for name '" +
						name + "': It is already registered for name '" + registeredName + "'.");
					}
				}
				checkForAliasCircle(name, alias);
				this.aliasMap.put(alias, name);
				if(logger.isTraceEnabled()){
					logger.trace("Alias definition '" + alias +"' registered for name '" + name + "'");
				}
			}
		}
	}

	/**
	 * 오버라이딩이 된 얘인지 리턴
	 * @return
	 */
	protected boolean allowAliasOverriding(){
		return true;
	}

	/**
	 * 있는애인지 확인해보자
	 * @param name
	 * @param alias
	 * @return
	 */
	public boolean hasAlias(String name, String alias){
		for (Map.Entry<String, String> entry : this.aliasMap.entrySet()) {
			String registeredName = entry.getValue();
			if(registeredName.equals(name)){
				String registeredAlias = entry.getKey();
				if(registeredAlias.equals(alias) || hasAlias(registeredAlias, alias)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * [삭제]
	 * @param alias 지울거
	 */
	@Override
	public void removeAlias(String alias){
		synchronized (this.aliasMap) {
			String name = this.aliasMap.remove(alias);
			if( name == null) {
				throw new IllegalStateException("No alias '" + alias + "' registered");
			}
		}
	}

	/**
	 * [확인] 존재 여부
	 * @param name
	 * @return
	 */
	public boolean isAlias(String name){
		return this.aliasMap.containsKey(name);
	}

	public String[] getAliases(String name){
		ArrayList result = new ArrayList();
		Map var3 = this.aliasMap;
		synchronized (this.aliasMap){
			this.retrieveAliases(name, result);
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * 이름으로 확인해서 같으면 거기 리스트에 추가하자
	 * @param name
	 * @param result
	 */
	private void retrieveAliases(String name, List<String> result){
		this.aliasMap.forEach((alias, registeredName) -> {
			if(registeredName.equals(name)) {
				result.add(alias);
				this.retrieveAliases(alias, result);
			}
		});
	}

	/**
	 * todo StringValueResolver이게 뭔지 봐야될듯
	 * @param valueResolver
	 */
	public void resolveAliases(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		synchronized (this.aliasMap) {
			HashMap<String, String> aliasCopy = new HashMap(this.aliasMap);
			aliasCopy.forEach((alias, registeredName) -> {
				String resolvedAlias = valueResolver.resolveStringValue(alias);
				String resolvedName = valueResolver.resolveStringValue(registeredName);
				if (resolvedAlias != null && resolvedName != null && !resolvedAlias.equals(resolvedName)) {
					if (!resolvedAlias.equals(alias)) {
						String existingName = (String) this.aliasMap.get(resolvedAlias);
						if (existingName != null)
							if (existingName.equals(resolvedName)) {
								this.aliasMap.remove(alias);//????
								return;
							}
						throw new IllegalStateException("Cannot register resolved alias");
					}
					checkForAliasCircle(resolvedName, resolvedAlias);
					this.aliasMap.remove(alias);
					this.aliasMap.put(resolvedAlias, resolvedName);
				} else if (!registeredName.equals(resolvedName)) {
					this.aliasMap.put(alias, resolvedName);
				}
			});
		}
	}

	protected void checkForAliasCircle(String name, String alias) {
		if(hasAlias(alias, name)) {
			throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" + name +"': Circular reference -'" + name + "' is a direct or indirect alias for '" + alias + "' already");

		}
	}

	/**
	 * 어따쓰지
	 * @param name
	 * @return
	 */
	public String  canonicalName(String name){
		String canonicalName = name;
		// Handle aliasing..
		String resolvedName;
		do{
			resolvedName = this.aliasMap.get(canonicalName);
			if(resolvedName != null){
				canonicalName = resolvedName;
			}
		}
		while (resolvedName != null);
		return canonicalName;
	}



}
