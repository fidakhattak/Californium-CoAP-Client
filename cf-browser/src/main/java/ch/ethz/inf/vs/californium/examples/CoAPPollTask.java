package ch.ethz.inf.vs.californium.examples;


import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.ResponseHandler;
import ch.ethz.inf.vs.californium.coap.TokenManager;

public class CoAPPollTask extends TimerTask {

	Timer timer = new Timer(true);
	private String URI, Payload;
	private static pollTaskStats stats;
    private ManagePollInterface callback;
	/*Statistics to be kept  */
    static int wait_counter; /*max set to 16 secs currently */


	CoAPPollTask(String uri, String payload, int freq, int totalPolReq) {	
			initialize(uri, payload, freq, totalPolReq);
	}

	CoAPPollTask(String uri, String payload, int freq, int totalPolReq, ManagePollInterface callback) {	
		initialize(uri, payload, freq, totalPolReq);
		this.callback = callback;
}

	private void initialize (String uri, String payload, int freq, int totalPolReq)
	{
		URI = uri;
		Payload = payload;
		System.out.println("Freq == " + freq);
		stats = new pollTaskStats(1, freq, totalPolReq);
		timer.schedule(this, 0, 1000/freq);
		wait_counter = 0;
				
	}

	public void run() {
		
		
		if (stats.getPolReqLeft() > 0) {
			GETRequest request = new GETRequest();

			if (request.requiresToken()) {
				request.setToken(TokenManager.getInstance().acquireToken());
			}
			request.setURI(URI);
			request.setPayload(Payload);
			request.registerResponseHandler(new pollHandler());
			execute(request);
			stats.setPolReqLeft(stats.getPolReqLeft()-1);
			//			stats.setCurrentPolReq(stats.getCurrentPolReq()+1);
		}
	
		
		/*If there are still outstanding requests to this task, wait for a maximum
		 * of 32 seconds before saving the stats and exiting */
		
		else
		if (stats.getOutstandingRequests() > 0 && stats.getPolReqLeft() == 0 )
			while (wait_counter < 32 && stats.getOutstandingRequests() == 0) {
             {
				try {	
									Thread.sleep(1000);	
									wait_counter++;
					}
				 catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
             exit();
		 }
		
		else
		if(stats.getOutstandingRequests() == 0 && stats.getPolReqLeft()== 0)
			 exit();
	}

	private void exit() {
		saveStats();
		this.callback.update(this);
	}
	

	private void execute(Request request) {
		try {
			request.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	/* This function save stats in a file. It is called before calling the 
	 * manager.update() function in exit ()
	 *  */
	
	private void saveStats() {
		
	}

	/* This function is called by the pollHandler to set the stats for this poll task */
	public static void setStats(Response response) {

		stats.setCurrentPolRes(stats.getCurrentPolRes()+1);
		stats.setTotalRTT(stats.getTotalRTT()+response.getRTT());

		/* Total Retransmissions for this tasks. A single packet retransmitted multiple times is multiple retransmissions*/
		stats.setTotRetransmissions(stats.getTotRetransmissions()+response.getRetransmissioned());
		stats.getOutstandingRequests();
	}


	public String getURI() {
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

}

 /* A class to store the important stats that will be logged later */

class pollTaskStats {

	private  int task_number;
	private  double averageRTT;
	private  double totalRTT;
	private  int totRetransmissions;
	private  int totalPollRequests;
	private  int currentPolRes;
	private  int polReqLeft;
	private  int pps;
	private  int outstandingRequests;

    
	public pollTaskStats(int task_number, int pps, int totalPollRequests) {
		super();
		this.task_number = task_number;
		this.pps = pps;
		this.totalPollRequests = totalPollRequests;
		this.polReqLeft=totalPollRequests;
     	this.currentPolRes = 0;
		this.averageRTT = 0;
		this.totalRTT = 0;
		this.totRetransmissions = 0;
		
	}
		
	public int getTask_number() {
		return task_number;
	}

	public void setTask_number(int task_number) {
		this.task_number = task_number;
	}

	//public int getCurrentPolReq() {
	//		return currentPolReq;
	//	}

	//	public void setCurrentPolReq(int currentPolReq) {
	//		this.currentPolReq = currentPolReq;
	//	}

	public int getCurrentPolRes() {
		return currentPolRes;
	}

	public void setCurrentPolRes(int currentPolRes) {
		this.currentPolRes = currentPolRes;
	}

	public double getAverageRTT() {
		return averageRTT;
	}

	public void setAverageRTT(double averageRTT) {
		this.averageRTT = averageRTT;
	}

	public double getTotalRTT() {
		return totalRTT;
	}

	public void setTotalRTT(double totalRTT) {
		this.totalRTT = totalRTT;
	}

	public int getTotRetransmissions() {
		return totRetransmissions;
	}

	public void setTotRetransmissions(int totRetransmissions) {
		this.totRetransmissions = totRetransmissions;
	}

	public int getTotalPollRequests() {
		return totalPollRequests;
	}

	public void setTotalPollRequests(int totalPollRequests) {
		this.totalPollRequests = totalPollRequests;
	}

	public int getPolReqLeft() {
		return polReqLeft;
	}

	public void setPolReqLeft(int polReqLeft) {
		this.polReqLeft = polReqLeft;
	}

	public int getPps() {
		return pps;
	}

	public void setPps(int pps) {
		this.pps = pps;
	}

	public int getOutstandingRequests() {
		this.outstandingRequests = (this.totalPollRequests - this.polReqLeft) - (this.currentPolRes);
		return this.outstandingRequests;
	}
}


class pollHandler implements ResponseHandler {

	@Override
	public void handleResponse(Response response) {
		// TODO Auto-generated method stub
		CoAPPollTask.setStats(response); 

	}

}
