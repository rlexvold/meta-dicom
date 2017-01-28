package net.metafusion.admin;

public class TaskRunnerFactory
{
	public static TaskRunnerFactory factory = new TaskRunnerFactory();

	public static TaskRunnerFactory getTaskRunnerFactory()
	{
		return factory;
	}

	public static void setTaskRunnerFactory(TaskRunnerFactory factory)
	{
		TaskRunnerFactory.factory = factory;
	}

	public TaskRunner getTaskRunner()
	{
		return new TaskRunner();
	}
}