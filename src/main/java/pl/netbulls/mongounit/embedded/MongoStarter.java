package pl.netbulls.mongounit.embedded;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Starter for embedded mongodb server.
 */
public class MongoStarter implements BeanFactoryAware
{
	/**
	 * Mongodb server port.
	 */
	private int port;
	/**
	 * Mongodb database name.
	 */
	private String database;
	/**
	 * Mongodb server process.
	 */
	private MongodProcess mongodProcess;
	/**
	 * Mongodb executable.
	 */
	private MongodExecutable mongodExe;
	/**
	 * Mongodb connector instance.
	 */
	private Mongo mongo;
	/**
	 * Bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Starts the mongodb embedded server.
	 */
	public void start()
	{
		try
		{
			IMongodConfig mongodConfig =
					new MongodConfigBuilder().version(Versions.withFeatures(new GenericVersion("2.4.1"), Feature.SYNC_DELAY))
							.net(new Net(port, Network.localhostIsIPv6())).build();
			MongodStarter runtime = MongodStarter.getInstance(createRuntimeConfigWithFileOutput());
			mongodExe = runtime.prepare(mongodConfig);
			mongodProcess = mongodExe.start();
			mongo = beanFactory.getBean(Mongo.class);
		}
		catch (IOException e)
		{

			if (mongodProcess != null)
			{
				mongodProcess.stop();
				mongodProcess = null;
			}
			throw new RuntimeException("Cannot start mongodb server.", e);
		}
	}

	/**
	 * Stops the mongodb server if started.
	 */
	public void stop()
	{
		if (mongodExe != null)
		{
			mongodExe.stop();
			mongodExe = null;
		}
		if (mongodProcess != null)
		{
			mongodProcess.stop();
			mongodProcess = null;
		}
		mongo = null;
	}

	/**
	 * Drop collections.
	 *
	 * @param collections name of collections.
	 */
	public void clearData(String[] collections)
	{
		for (String name : collections)
			mongo.getDB(database).getCollection(name).drop();
	}

	/**
	 * Remove all (no system) collections - clear database.
	 */
	public void clearData()
	{
		DB databas = mongo.getDB(database);
		for (String collectionName : databas.getCollectionNames())
		{
			if (!collectionName.startsWith("system."))
				databas.getCollection(collectionName).drop();
		}
	}

	/**
	 * Load data to collection.
	 *
	 * @param collection collection name.
	 * @param data array of document to load.
	 */
	public void fillData(String collection, DBObject... data)
	{
		mongo.getDB(database).getCollection(collection).insert(data);
	}

	/**
	 * @param port Mongodb server port.
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @param database Mongodb database name.
	 */
	public void setDatabase(String database)
	{
		this.database = database;
	}

	public void setBeanFactory(BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	/**
	 * Creates runtime config with file output.
	 *
	 * @return runtime config.
	 */
	private IRuntimeConfig createRuntimeConfigWithFileOutput()
	{
		IStreamProcessor mongodOutput = Processors.named("[mongod>]", new TmpFileStreamProcessor("mongod-"));
		IStreamProcessor mongodError = new TmpFileStreamProcessor("mongod-error-");
		IStreamProcessor commandsOutput = Processors.namedConsole("[console>]");

		return new RuntimeConfigBuilder().defaults(Command.MongoD)
				.processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput)).build();
	}

	/**
	 * Stream processor to tmp file.
	 */
	private static class TmpFileStreamProcessor implements IStreamProcessor
	{
		private File file;
		private FileOutputStream outputStream;

		public TmpFileStreamProcessor(String fileNamePrefix)
		{
			try
			{
				file = File.createTempFile(fileNamePrefix, ".log");
				System.out.println("Created mongo output file: " + file.getAbsolutePath());
			}
			catch (IOException e)
			{
				throw new RuntimeException("Cannot create tmp file.", e);
			}
		}

		@Override
		public void process(String block)
		{
			try
			{
				if (outputStream == null)
					createOutputStream();
				outputStream.write(block.getBytes());
			}
			catch (IOException e)
			{
				throw new RuntimeException("Cannot write to file stream.", e);
			}
		}

		@Override
		public void onProcessed()
		{
			try
			{
				if (outputStream != null)
					outputStream.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException("Cannot close file stream.", e);
			}
		}

		/**
		 * Creates output stream if not already created.
		 */
		private synchronized void createOutputStream()
		{
			if (outputStream != null)
				return;
			try
			{
				outputStream = new FileOutputStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException("Tmp file cannot be found.", e);
			}
		}

	}
}
