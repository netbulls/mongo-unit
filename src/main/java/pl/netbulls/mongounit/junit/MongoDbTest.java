package pl.netbulls.mongounit.junit;

import com.mongodb.util.JSON;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.junit.Rule;
import pl.netbulls.mongounit.embedded.MongoStarter;

/**
 * Base class for all mongodb test.
 */
public abstract class MongoDbTest
{
	@Rule
	public FixtureRule fixtureRule;

	@PostConstruct
	public void init()
	{
		fixtureRule = new FixtureRule(getMongoStarter());
	}

	/**
	 * @return return mongo starter.
	 */
	abstract protected MongoStarter getMongoStarter();

	/**
	 * Clears data from database.
	 */
	public void clearData(String[] collections)
	{
		getMongoStarter().clearData(collections);
	}

	/**
	 * @param json not normalized json.
	 *
	 * @return normalized json (formatted).
	 */
	public static String normJson(String json)
	{
		return JSON.serialize(JSON.parse(json));
	}

	/**
	 * @param json json object.
	 *
	 * @return normalized json (formatted).
	 */
	public static String normJson(BSONObject json)
	{
		return JSON.serialize(json);
	}

	/**
	 * @param content json content.
	 *
	 * @return empty json document.
	 */
	public static BSONObject json(String content)
	{
		return (BSONObject) JSON.parse(content);
	}

	/**
	 * @return empty json document.
	 */
	public static BasicBSONObject json()
	{
		return new BasicBSONObject();
	}

	/**
	 * @param key key for first entry on json.
	 * @param value value for first key entry in json.
	 *
	 * @return json object.
	 */
	public static BasicBSONObject json(String key, Object value)
	{
		return new BasicBSONObject(key, value);
	}

	/**
	 * @param keys keys for json object.
	 *
	 * @return json object.
	 */
	public static BasicBSONObject json(JsonPair... keys)
	{
		BasicBSONObject basicBSONObject = new BasicBSONObject();
		for (Pair<String, Object> p : keys)
			basicBSONObject.append(p.getKey(), p.getValue());
		return basicBSONObject;
	}

	/**
	 * @param key key for key.
	 * @param value value for key.
	 *
	 * @return key.
	 */
	public static JsonPair key(String key, Object value)
	{
		return new JsonPair(key, value);
	}
}
