package ch.ethz.inf.vs.californium.examples;

import java.util.Iterator;

/* Poll Manager class. An object of this class is created everytime a new poll task
 * is assigned to the CoAP Client. This manager takes care of the polling tasks
 * in loop. Once all the tasks are completed, it returns. 
 */

public class ManagePollTasks implements ManagePollInterface{
	private static int startPps;
	private static int endPps;
	private static int step;
	private int totalPollRequests;
	private static String uri, payload;
	
	public ManagePollTasks(int startPps, int endPps, int step,
			int totalPollRequests, String uri, String payload) {
		super();
		this.startPps = startPps;
		this.endPps = endPps;
		this.step = step;
		this.totalPollRequests = totalPollRequests;
		this.uri = uri;
		this.payload = payload;
		 
		if (startPps <= endPps)
			newPollTask();
	}

	
	@Override
	public void update(CoAPPollTask task) {
		// TODO Auto-generated method stub
	
		removePoll(task);
		task.cancel();
		System.out.println("startPps =" +this.startPps +"endPps = " + this.endPps + "step = " + this.step);
		if (this.startPps <= this.endPps)
		newPollTask();
		else
			return;			
	}
	
	private void newPollTask() {
			CoAPPollTask pollTask = new CoAPPollTask(uri, payload, startPps,
				totalPollRequests,this);
			CoAPClientExtensive.polledServers.add(pollTask);
			this.startPps += this.step;
			System.out.println("startPps = " +this.startPps + "step = " + step);
}
	
	private static void removePoll(CoAPPollTask task) {

		Iterator iterator;
		CoAPPollTask element;
		iterator = CoAPClientExtensive.polledServers.iterator();
		while (iterator.hasNext()) {
			element = (CoAPPollTask) iterator.next();
			if (element.equals(task)) {
				element.cancel();
				CoAPClientExtensive.polledServers.remove(element);
			}
		}
		return;
	}
}
