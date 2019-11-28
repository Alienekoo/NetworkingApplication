package serverproject;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
	
private static DatagramSocket serverSocket;
public static final int MAX_DATA_SIZE = 405; // should I keep changing it????
public static final int MAX_CLUS_SIZE = 17; // maximum clus packet size
public static final double eo = 0.00001;

//======================================================================================================================================
//Methods to find distance between two vectors, centroid and cluster bytes.

public static float Distance(float[] m, float[][] vector,int l) {
	// find out distance here
	// m1  = x1,y1  ......... vector[l][0], vector[l][1] = x2,y2
	  float x1 = m[0]; 
	  float y1 = m[1];
	  float x2 = vector[l][0]; 
	  float y2 = vector[l][1];
	 return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
		
	}
public static float Distance(float[] m, float[] l) {
	// find out distance here
	// m1  = x1,y1  ......... vector[l][0], vector[l][1] = x2,y2
	  float x1 = m[0]; 
	  float y1 = m[1];
	  float x2 = l[0]; 
	  float y2 = l[1];
	 return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

	}
public static byte[] Clus(float[] m1, float[] m2) {
	m1[0] = m1[0]*100;
	m1[1] = m1[1]*100;
	m2[0] = m2[0]*100;
	m2[1] = m2[1]*100;
	
	byte[] clusm = new byte[MAX_CLUS_SIZE];
	 
	clusm[0] = (byte) 4;
	int p = 1;		
		clusm[p++]= (byte)((int)m1[0] & (0xFF));
		clusm[p++]= (byte)(((int)m1[0]>>8) & (0xFF));
		clusm[p++]= (byte)(((int)m1[0]>>16) & (0xFF));
		clusm[p++]= (byte)(((int)m1[0]>>24) & (0xFF));		
		clusm[p++]= (byte)((int)m1[1] & (0xFF));
		clusm[p++]= (byte)(((int)m1[1]>>8) & (0xFF));
		clusm[p++]= (byte)(((int)m1[1]>>16) & (0xFF));
		clusm[p++]= (byte)(((int)m1[1]>>24) & (0xFF));	
		clusm[p++]= (byte)((int)m2[0] & (0xFF));
		clusm[p++]= (byte)(((int)m2[0]>>8) & (0xFF));
		clusm[p++]= (byte)(((int)m2[0]>>16) & (0xFF));
		clusm[p++]= (byte)(((int)m2[0]>>24) & (0xFF));
		clusm[p++]= (byte)((int)m2[1] & (0xFF));
		clusm[p++]= (byte)(((int)m2[1]>>8) & (0xFF));
		clusm[p++]= (byte)(((int)m2[1]>>16) & (0xFF));
		clusm[p++]= (byte)(((int)m2[1]>>24) & (0xFF));
	return clusm;
}
public static float[] Centroid(ArrayList<Float>vectors) {
	int z = 0;
	ArrayList<Float> dimension1 = new ArrayList<Float>();
	ArrayList<Float> dimension2 = new ArrayList<Float>();
	while(z<vectors.size()) {
		dimension1.add(vectors.get(z++));
		dimension2.add(vectors.get(z++));
		// x coordinated in dimension 1, y coordinates in dimension2 
	}
	float[][] vector2D = new float[dimension1.size()][2]; 
	z=0;
	while(z<dimension1.size()) {
		vector2D[z][0] = dimension1.get(z);
		vector2D[z][1] =  dimension2.get(z++);
	} // 2D array of all the vectors

	float max_dimension1 = Collections.max(dimension1); // max x 
	float min_dimension1 = Collections.min(dimension1); // min x
	float max_dimension2 = Collections.max(dimension2); // max y
	float min_dimension2 = Collections.min(dimension2); // min y
	// find max n min of both coordinates

	Random rand = new Random();
	float[] m1 = new float[2];
	float[] m2 = new float[2];

	m1[0] = rand.nextFloat() * (max_dimension1 - min_dimension1) + min_dimension1; // m1x between max x and min x
	m1[1] = rand.nextFloat() * (max_dimension2 - min_dimension2) + min_dimension2; // m1y between max y and min y
	m2[0] = rand.nextFloat() * (max_dimension1 - min_dimension1) + min_dimension1; // m2x between max x and min x
	m2[1] = rand.nextFloat() * (max_dimension2 - min_dimension2) + min_dimension2; // m2y between max y and min y


	//Formation of clusters
	ArrayList<Float[]> c1 = new ArrayList<Float[]>();
	ArrayList<Float[]> c2 = new ArrayList<Float[]>();
	z = 0;
	while(true) {
		c1.clear();
		c2.clear();
		
		// 1.make clusters
		for(int l=0;l<dimension1.size();l++) {
			if(Distance(m1,vector2D,l) < Distance(m2,vector2D,l)){
				Float[] e = {vector2D[l][0],vector2D[l][1]};
				c1.add(e);
			}
			else {
				Float[] e = {vector2D[l][0],vector2D[l][1]};
				c2.add(e);
				
			}
		}//for loop
	//clusters are done. 	
	// 2.average of the clusters - finding centroid:
			float[] n1 = new float[2];
			float[] n2 = new float[2];
			n1[0] = 0;
			n1[1] = 0;
			n2[0] = 0;
			n2[1] = 0;
			for(int b = 0; b < c1.size(); b++) {
			    n1[0] += c1.get(b)[0];
			    n1[1] += c1.get(b)[1];
			
		}
			for(int b = 0; b < c2.size(); b++) {
			    n2[0] += c2.get(b)[0];
			    n2[1] += c2.get(b)[1];
			
		}
			//System.out.print("step: trying to n1,n2 centroid\n"+ n1[1]);
			n1[0] = n1[0]/c1.size();
			n1[1] = n1[1]/c1.size();
			n2[0] = n2[0]/c1.size();
			n2[1] = n2[1]/c1.size();
			// avaerage cluster check with m1, m2 :::: divide them with total length 
			
		if(Distance(m1,n1) +Distance(m2,n2)<eo) {
			
			break;
		}
		else {
			m1 = n1;
			m2 = n2;
		}
		
	}
	float[]m = {m1[0], m1[1], m2[0],m2[1]};
	return m;
}
//======================================================================================================================================
//main function:

public static void main(String[] args) throws Exception
{

final int MAX_DACK_SIZE = 3; // maximum dack packet size in bytes
final int MAX_PKT_SIZE = 1; // 1 byte packets  
final int SERVER_PORT_NUM = 6699; // server port number: it should be the same
	
byte[] datam = new byte[MAX_DATA_SIZE];
byte[] dackm = new byte[MAX_DACK_SIZE];
byte[] reqm = new byte[MAX_PKT_SIZE];
byte[] rackm = new byte[MAX_PKT_SIZE];
byte[] clusm = new byte[MAX_CLUS_SIZE];
byte[] cackm = new byte[MAX_PKT_SIZE];
// send and receive UDP packets
DatagramPacket dataPacket = new DatagramPacket(datam, datam.length);
DatagramPacket dackPacket = new DatagramPacket(dackm, dackm.length); // add address and portnumber of client
DatagramPacket reqPacket = new DatagramPacket(reqm, reqm.length);	
DatagramPacket cackPacket = new DatagramPacket(cackm, cackm.length);
serverSocket = new DatagramSocket(SERVER_PORT_NUM);
// client's IP address object
InetAddress clientAddress;
ArrayList<Float> vectors = new ArrayList<Float>();
// do this forever
dackm[0] = (byte) 1;
	
//======================================================================================================================================
//start server: receive data packets and send dacks:
	System.out.println("server started");
	while(true)
	{
	
	int counter = 0; //used to make sure the code gives exception after 4 times of failure to receive any packet
	int TIME_OUT_VAL = 1000; 
	
// receiving client's request
	
	try {
	serverSocket.setSoTimeout(30*TIME_OUT_VAL);	//making sure socket does not time out while receiving for 30 seconds
	serverSocket.receive(dataPacket);   //receive datapacket for the beginning
	System.out.println("first packet arrived..");
	}
	catch(SocketTimeoutException e) {
		System.out.println("\nServer socket timeout\n");
		System.exit(0);
	}
	clientAddress = dataPacket.getAddress();	
	int clientPort = dataPacket.getPort();	
	int value = 0; //values (32 bit integers) I extract from the data packets
	float data = 0; //float values of the data
	int seqnum = 0; // expected seqnum from the data packet
	int switcher1 = 1; // 1 only when I receive some packet
	
//continuously receiving data packets and sending dack packets	
	
	while(true) {
	if(dataPacket.getData()[0]== (byte) 0) {
		int k = 5; //just a counter used to extract data from the data packet
		if(switcher1==1) {
			if((int)((0xff & dataPacket.getData()[1])<<8 | (0xff & dataPacket.getData()[2])<<0 )==seqnum) {
			dackm[1] = dataPacket.getData()[1];
			dackm[2] = dataPacket.getData()[2];
			dackPacket = new DatagramPacket(dackm, dackm.length); // should add address and portnumber of client
			dackPacket.setAddress(clientAddress); // destination IP address
			dackPacket.setPort(clientPort); // destination port number
			serverSocket.send(dackPacket);//dack sent 
			
			while(k<dataPacket.getLength()) {
				value = (int)((dataPacket.getData()[k] & 0xFF) <<  0 | (dataPacket.getData()[k+1] & 0xFF) <<  8 | (dataPacket.getData()[k+2] & 0xFF) <<  16 | (dataPacket.getData()[k+3] & 0xFF) <<  24);
				data = (float)value/100;		
				vectors.add(data);
				k=k+4;
			}
			counter = 0;
			 // 405 coz max size of data bytes being sent is 405 per data packet. not related to the total size of the vector count. for every packet it runs again and again. 
			seqnum++;
			
		} // if correct seqnum received, then dack of the new data will be sent
			else {
				//can also add a counter here and give an exception if I come here 4 times saying my dack is not being received by client
				serverSocket.send(dackPacket); //sends old dack packet again if it receives old/duplicate data packet
			} 
		}//switcher = 1
		
		try
		{
		serverSocket.setSoTimeout(TIME_OUT_VAL);
		serverSocket.receive(dataPacket); // the timeout timer starts ticking here
//		if((int)((0xff & dataPacket.getData()[1])<<8 | (0xff & dataPacket.getData()[2])<<0 )==seqnum-1) {
//			Thread.sleep(1000); // wait for the timeout if the old/duplicate data packet is received 
//		}
		switcher1 = 1;
		if(dataPacket.getData()[0]>2) {
			Thread.sleep(3000); // for invalid data types, ignore and keep receiving within the time.	
		}
		}
		catch(InterruptedIOException e)
		{
			if(counter>=4) {
				// timeout - timer expired before receiving the response from the server
				System.out.print("\nServer socket data packet receiving timeout! Exception message: " + e.getMessage() );
				System.exit(0);
			}
			serverSocket.send(dackPacket); // sends old dack saying I did not receive new data packet in time
			counter++;
			TIME_OUT_VAL = 2*TIME_OUT_VAL;
			switcher1 = 0;
			
		}
	
	}
	else if(dataPacket.getData()[0]== (byte) 2) {
		break; // done with data, it receieved req packet
	}
	else {
		try {
		serverSocket.setSoTimeout(TIME_OUT_VAL);
		serverSocket.receive(dataPacket);
		switcher1 = 1;
		if(dataPacket.getData()[0]>2) {
			Thread.sleep(3000); // for invalid data types, ignore and keep receiving within the time.	
			switcher1 = 0;
		}
		}
		catch(InterruptedIOException e)
		{
			if(counter>=4) {
				// timeout - timer expired before receiving the response from the server
				System.out.print("\nServer socket data packet receiving timeout! Invalid data packet. Exception message: " + e.getMessage() );
				System.exit(0);
			}
			serverSocket.send(dackPacket); // sends old dack saying I did not receive new data packet in time
			counter++;
			TIME_OUT_VAL = 2*TIME_OUT_VAL;
			switcher1 = 0;
			
		}
	}
	
}//data while
//data received.
	
//======================================================================================================================================
//req and rack
	TIME_OUT_VAL = 1000;
	reqPacket = dataPacket;
	int rackCounter = 0;
while(true) {
	if (reqPacket.getData()[0]== (byte) 2) {
		rackm[0]=(byte) 3;
		DatagramPacket rackPacket = new DatagramPacket(rackm, rackm.length);
		rackPacket.setAddress(clientAddress); // destination IP address
		rackPacket.setPort(clientPort); // destination port number
		
		try {
			serverSocket.send(rackPacket);
			serverSocket.setSoTimeout(TIME_OUT_VAL);
			serverSocket.receive(reqPacket); //waits if req comes again, just in case
			if(reqPacket.getData()[0]!=2) {
				serverSocket.receive(reqPacket); //invalid req packet type, ignore it.
			}
		}
		catch(InterruptedIOException e) {
			System.out.println("req/rack done."); //if no req comes in time, safely assume that the rack has been received
			break; 
		}
	}//req while
	
}	
//======================================================================================================================================
//clustering process-algorithm - methods
float[] m = Centroid(vectors);
float[] m1 = {m[0],m[1]};
float[] m2 = {m[2],m[3]};
System.out.println("the cluster array I am sending is: "+Arrays.toString(m)+" \n");
//======================================================================================================================================
//clus in bytes
clusm = Clus(m1,m2); //clus packet buffer to be sent
counter = 0; //refreshing counter

//sending clus and receiving cack 
TIME_OUT_VAL = 1000;
while(true) {
		
	DatagramPacket clusPacket = new DatagramPacket(clusm, clusm.length);
	clusPacket.setAddress(clientAddress);
	clusPacket.setPort(clientPort);

try
{
	serverSocket.send(clusPacket);
	serverSocket.setSoTimeout(TIME_OUT_VAL);
    serverSocket.receive(cackPacket); // the timeout timer starts ticking here
   
}
catch(InterruptedIOException e)
{
	if(counter>=4) {
		
		// timeout - timer expired before receiving the response from the server
		System.out.println("\nServer socket timeout! Clus sent but no cack. Exception message: " + e.getMessage() );
		System.exit(0);
	}
	counter++;
	TIME_OUT_VAL = 2*TIME_OUT_VAL;
	
	
}

if(cackPacket.getData()[0]== (byte)5) {
	counter = 10;
	System.out.println("The cack has been received.");
	break;
}


}
//if(counter == 10) {
//	break;
//}//break the main while

	}}}	