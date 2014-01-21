package pl.netbulls.mongounit.junit;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.io.IOException;
import org.bson.BSONObject;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import pl.netbulls.mongounit.annotation.Fixture;
import pl.netbulls.mongounit.embedded.MongoStarter;

/**
 * Rule to JUnit loading fixtures to mongo database.
 */
public class FixtureRule implements TestRule
{
	/**
	 * Mongodb starter. Can load data to collection and clear database.
	 */
	private MongoStarter mongoStarter;

	/**
	 * @param mongoStarter {@link #mongoStarter}
	 */
	public FixtureRule(MongoStarter mongoStarter)
	{
		this.mongoStarter = mongoStarter;
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				loadFixture(description);
				base.evaluate();
				clearDataBase();
			}
		};
	}

	/**
	 * Load fixtures to mongo database. Path to fixtures is taken from annotation {@link Fixture}.
	 *
	 * @param description {@link Description}.
	 *
	 * @throws java.io.IOException if cannot load files.
	 */
	private void loadFixture(Description description) throws IOException
	{
		Fixture annotation = description.getAnnotation(Fixture.class);
		if (annotation != null)
		{
			String[] fixtureFiles = annotation.value();
			for (String fixturePath : fixtureFiles)
			{
				BSONObject fixtureData =
						(BSONObject) JSON.parse(Resources.toString(Resources.getResource(fixturePath), Charsets.UTF_8));
				if (!(fixtureData instanceof BasicDBList))
					throw new RuntimeException("fixture file: " + fixturePath + " not contain list of objects");

				BasicDBList collectionList = (BasicDBList) fixtureData;
				for (Object collection : collectionList)
				{
					BSONObject collectionJsonObject = (BSONObject) collection;
					String collectionName = (String) collectionJsonObject.get("collectionName");
					if (collectionName == null)
						throw new RuntimeException("'collectionName' is required");
					BasicDBList collectionData = (BasicDBList) collectionJsonObject.get("data");
					if (collectionData == null)
						throw new RuntimeException("'data' is required");
					mongoStarter.fillData(collectionName, collectionData.toArray(new DBObject[collectionData.size()]));
				}
			}
		}
	}

	/**
	 * Clear data base. Is executed after each test, also for test without {@link Fixture} annotation.
	 */
	private void clearDataBase()
	{
		mongoStarter.clearData();
	}
}
