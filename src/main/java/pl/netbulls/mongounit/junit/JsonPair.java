package pl.netbulls.mongounit.junit;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Json pair/key class.
 */
public class JsonPair extends MutablePair<String, Object>
{
	/**
	 * Create a new key instance.
	 *
	 * @param left the left value, may be null
	 * @param right the right value, may be null
	 */
	public JsonPair(String left, Object right)
	{
		super(left, right);
	}
}
