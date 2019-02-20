package defaultListableBeanFactory;

/**
 * Created by rnthd2 on 2019. 2. 14..
 */

/**
 * 알리아스 관리계의 킹왕짱 인터페이스
 */
public interface AliasRegistryTest {

	/**
	 * [등록] 이름으로
	 * @param name 정식이름
	 * @param alias 등록될 알리아스
	 * @throws IllegalStateException 이미 있으면 이거 써라
	 */
	void registerAlias(String name, String alias);

	/**
	 * [삭제] 알리아스로
	 * @param alias 지울거
	 * @throws IllegalStateException 지울게 없으면
	 */
	void removeAlias(String alias);

	/**
	 * [조회] 이미 있는건지 여부를 이름으로 확인
	 * @param name
	 * @return 이미 있는 이름인지 아닌지
	 */
	boolean isAlias(String name);

	/**
	 * [조회] 이미 있는 알리아스 반환
	 * @param name 확인하고싶은 알리아스
	 * @return 알리아스들, 없으면 빈 배열 반환
	 */
	String[] getAliases(String name);
}
