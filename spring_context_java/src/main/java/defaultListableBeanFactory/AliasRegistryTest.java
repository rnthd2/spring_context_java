package defaultListableBeanFactory;

/**
 * Created by rnthd2 on 2019. 2. 14..
 */
public interface AliasRegistryTest {
	void registerAlias(String var1, String var2);

	void removeAlias(String var1);

	boolean isAlias(String val1);

	String[] getAliases(String var1);
}
