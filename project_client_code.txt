package project;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;
public class Client {
	
	public static final int MAX_DATA_SIZE = 405; // max data packet size - 30*8+5 : max number of 2D vectors = 30, each have 8 bits + 5 bytes of datapacket header.
	public static final int PKT_SIZE = 1;
	public static final int MAX_VECTORS_SENT = 50;
	private static DatagramSocket clientSocket;
	
//======================================================================================================================================		  	
	
	
//methods to create a datapacket for every sequence number, changing float array of data vectors into 2D array ofvector data bytes, reading float data vectors from a data file:
	
    public static byte[] DataPacket(int seqnum, byte[][] vectorbytes, int totalsize) {
    	byte[] Buffer = new byte[MAX_DATA_SIZE];
    	int s= 0;
    	int seq = seqnum*MAX_VECTORS_SENT;
    	//System.out.println(Arrays.deepToString(vectorbytes));
		
    	if(totalsize-seqnum*MAX_VECTORS_SENT>=MAX_VECTORS_SENT) {
    		Buffer[0] = (byte) 0;
        	Buffer[1] = (byte) ((seqnum>>8) & 0xff);
        	Buffer[2] = (byte) (seqnum & 0xff);
    		Buffer[3] = (byte) ((MAX_VECTORS_SENT>>8) & 0xff);
        	Buffer[4] = (byte) (MAX_VECTORS_SENT & 0xff);
        	int i = 5;
        	while(i<MAX_DATA_SIZE) {
        		for(int j=0;j<8;j++) {
        			Buffer[i] = vectorbytes[seq][j];// correction required 
        			//System.out.print("step:vectorbytes formed: \n" + vectorbytes[seq][j]+"\n");
        			i = i+1;
        		}//j
        		
        		seq++;
        		
        	}//i
        	//making data packet by adding data vectors into the Buffer

        	return Buffer;
    	}
    	else {
    		
    		s = totalsize-seq;
    		byte[] Buffer1 = new byte[(s*8) +5];
    		Buffer1[0] = (byte) 0;
        	Buffer1[1] = (byte) ((seqnum>>8) & 0xff);
        	Buffer1[2] = (byte) (seqnum & 0xff);
    		Buffer1[3] = (byte) ((s>>8) & 0xff);
        	Buffer1[4] = (byte) (s & 0xff);
        	int i = 5;
        	while(i<s*8) {
        		for(int j=0;j<8;j++) {
        			Buffer1[i++] = vectorbytes[seq][j];//here 
        			//System.out.print("step:vectorbytes formed: \n" + vectorbytes[seq][j]+"\n");

        		}//j
        		seq++;
        	}//i
        	return Buffer1;
    		}
    		
    }   
    public static byte[][] VectorBytes(ArrayList<Float> vectors){
    	//make a 2D array of data vector bytes. 
		 
    			int i = 0;
    			int z = 0;
    			
    			float f,g;
    			int f1,g1;
    			byte[][] vectorbytes = new byte[(vectors.size()/2)][8];
    			System.out.println("Number of 2D vectors in the file: "+vectors.size()/2);
    			while(i<(vectors.size())) {
    				
    				f = 100*vectors.get(i);
    				g = 100*vectors.get(i+1);
    				
    				f1 = (int)f;		
    				g1 = (int)g;
    		
    				vectorbytes[z][0]= (byte) (f1 & 0xFF);
    				vectorbytes[z][1]= (byte) (f1>>8 & 0xFF);
    				vectorbytes[z][2]= (byte) (f1>>16 & 0xFF);
    				vectorbytes[z][3]= (byte) (f1>>24 & 0xFF);	
    				vectorbytes[z][4]= (byte) (g1 & 0xFF);
    				vectorbytes[z][5]= (byte) (g1>>8 & 0xFF);
    				vectorbytes[z][6]= (byte) (g1>>16 & 0xFF);
    				vectorbytes[z][7]= (byte) (g1>>24 & 0xFF);
    				//System.out.print("step:freshvectorbytes formed: \n" + vectorbytes[z][4]+"\n");
    				i=i+2;
    				z++;
    				
    			}
    			return vectorbytes;
    }    
    public static ArrayList<Float> Read(String S){
    	
    	ArrayList<Float> vectors = new ArrayList<Float>();		
    	 try {
	            
	            //  Get data from this file using a file reader. 
	            FileReader fr = new FileReader(S);
	            // To store the contents read via File Reader
	            BufferedReader br = new BufferedReader(fr);                                                 
	            // Read br and store a line in 'data', print data
	            String data;
	            while((data = br.readLine()) != null) 
	            {
	                //data = br.readLine( );
	            	 Pattern regex = Pattern.compile("(?m)[-]?\\d+\\.\\d+" , Pattern.MULTILINE);
	                 String input = data;
	                 Matcher matcher = regex.matcher(input); // data matching to pattern
	                 while (matcher.find()) {
	                	 float digit  =  Float.parseFloat(matcher.group());
	                	 vectors.add(digit); // adding float vector values to array list
	                	}
	            
	                 
	            } 
	            br.close();
	        } catch(IOException e) {
	            System.out.println("bad!");
	        }
    	 return vectors;
    }
    
    
 //======================================================================================================================================		   
 //main function

	public static void main(String[] args) throws Exception {
		
		
	    final int SERVER_PORT_NUM = 6699;
		final int MAX_DACK_SIZE = 3; // maximum 
		final int MAX_PKT_SIZE = 1; // maximum 
		final int MAX_CLUS_SIZE = 17; // maximum 
		
		
		byte[] datam = new byte[MAX_DATA_SIZE];
		byte[] dackm = new byte[MAX_DACK_SIZE];
		byte[] reqm = new byte[MAX_PKT_SIZE];
		byte[] rackm = new byte[MAX_PKT_SIZE];
		byte[] clusm = new byte[MAX_CLUS_SIZE];
		byte[] cackm = new byte[MAX_PKT_SIZE];
		// send and receive UDP packets
		
		DatagramPacket dackPacket = new DatagramPacket(dackm, dackm.length);
		DatagramPacket rackPacket = new DatagramPacket(rackm, rackm.length);
		
		ArrayList<Float> vectors = Read("D:\\640\\project\\data02.txt");		
		 
// got the data vectors from the text file to array list of size N*2, N = number of 2D vectors
//======================================================================================================================================
		 byte[][] vectorbytes = VectorBytes(vectors);
		 InetAddress IPadr = InetAddress.getByName("localhost");
		 InetAddress serverAddress;
	     clientSocket = new DatagramSocket();
// vectors are coverted into bytes and ready to be sent.
		
//======================================================================================================================================
		int seqnum = 0;
		int startnum = 0;		
		int counter = 1;
		int TIME_OUT_VAL = 1000;
		int switcher = 0;
		
		datam = DataPacket(seqnum++, vectorbytes, vectors.size()/2);
		DatagramPacket dataPacket = new DatagramPacket(datam, datam.length, IPadr, SERVER_PORT_NUM);
		
		
		// sending the UDP packet to the server 
		clientSocket.send(dataPacket);
		while((vectors.size()/2)-(startnum)>=0) {
			try
			{
			clientSocket.setSoTimeout(TIME_OUT_VAL);
			clientSocket.receive(dackPacket); // the timeout timer starts ticking here
			switcher = 1;
			}
			catch(InterruptedIOException e)
			{
				if(counter>=4) {
					// timeout - timer expired before receiving the response from the server
					System.out.print("\nClient socket timeout! part1:dack issue. Exception message: " + e.getMessage());
					System.exit(0);
				}
				clientSocket.send(dataPacket);
				counter++;//  it should start freshhhh after a data packet has been received
				TIME_OUT_VAL = 2*TIME_OUT_VAL;
				switcher = 0;
				
			}
			if(switcher==1) {
			if(dackPacket.getData()[0]==(byte)1) {
				if((int)((0xff & dackPacket.getData()[1])<<8 | (0xff & dackPacket.getData()[2])<<0 )==seqnum-1) //recieved dack seq num  = previous seq num of data packet  
				{
					
					startnum = startnum +((dataPacket.getLength()-5)/8); //the start index of next data packet vector bytes / also number of data vectors sent already
					if(startnum==vectors.size()/2) {
						System.out.println("last dack seqnum: "+(int)((0xff & dackPacket.getData()[1])<<8 | (0xff & dackPacket.getData()[2])<<0 ) );
						System.out.println("Number of 2D vectors sent to the server: "+startnum);
						break;
					}
					datam = DataPacket(seqnum++, vectorbytes, vectors.size()/2); //::: there is an error here : not stopping after sending all vectors. 
					//System.out.println("datam1/2/3/4: "+Arrays.toString(datam));
					dataPacket = new DatagramPacket(datam, datam.length, IPadr, SERVER_PORT_NUM); 
					clientSocket.send(dataPacket);
					
				}
				else {
					
					seqnum-=1;
					datam = DataPacket(seqnum++, vectorbytes, vectors.size()/2);
					dataPacket = new DatagramPacket(datam, datam.length, IPadr, SERVER_PORT_NUM);
					// sending the old data packet to the server 
					clientSocket.send(dataPacket);					
				}
			counter = 0;
			}
		}
		}


		
//======================================================================================================================================
//send req and receive rack		
		TIME_OUT_VAL = 1000;
		switcher = 0;
		counter = 0;
		while(true) {
			
			reqm[0] = (byte) 2;
			DatagramPacket reqPacket = new DatagramPacket(reqm, reqm.length, IPadr, SERVER_PORT_NUM);
			//DatagramPacket reqPacket = new DatagramPacket(reqm, reqm.length, IPadr, SERVER_PORT_NUM);
			//clientSocket = new DatagramSocket(); // randomly chosen client port number
			// sending the UDP packet to the server 
			
			// receiving the server's response
			try
			{
				clientSocket.setSoTimeout(TIME_OUT_VAL);
				clientSocket.send(reqPacket);
				clientSocket.receive(rackPacket); // the timeout timer starts ticking here
				switcher = 1;
				
			}
			catch(InterruptedIOException e)
			{
				if(counter>=4) {
					// timeout - timer expired before receiving the response from the server
					System.out.print("\nClient socket rack timeout! Exception message: " + e.getMessage() );
					System.exit(0);
				}
				clientSocket.send(reqPacket);
				counter++;
				TIME_OUT_VAL = 2*TIME_OUT_VAL;
				switcher = 0;
			}
			if(switcher==1) {
			if(rackPacket.getData()[0]==(byte)3) {
				System.out.println("req/rack done!!");
				counter = 0;
				break;
			}
			}
		}// req[-rack DONE!!!!!
	

//======================================================================================================================================
//receive clus and send cack		
		
float[] finalcluster = new float[4];
int switcher1 =1;
int check =0; // just to differentiate the conditions whether it's a 30 second timeout for not receiving for the first time or not receiving after cack being sent
int c = 0; //another check so that m2, m2 wont get printed again - like a switch
TIME_OUT_VAL = 1000;
while(true) {
	DatagramPacket clusPacket = new DatagramPacket(clusm, clusm.length);
	System.out.println("waits for cluster...\n");
	// receiving the server's response
	try
	{
	clientSocket.setSoTimeout(30*TIME_OUT_VAL);
	clientSocket.receive(clusPacket); // the timeout timer starts ticking here
//	if(clusPacket.getData()[0]!=4) {
//		System.out.println("wrong type");
//		Thread.sleep(50*TIME_OUT_VAL);
//	}
	check =0;
	switcher1 =1;
	}
	catch(InterruptedIOException e)
	{
	// timeout - timer expired before receiving the response from the server
	if(check == 0) {	
	System.out.print("\nClient socket timeout 30 seconds for clus packet! Exception message: " + e.getMessage() );
	System.exit(0);
	}
	if(check == 1) {
		System.out.println("Been 30 secs and server did not send clus again. DONE!!!!!!");
		break;
	}
	
	}
	if(switcher1==1) {
	
	if(clusPacket.getData()[0]==(byte)4) {
		int k=1;
		
		for(int l=0;l<4;l++) {
			float value = (float)((clusPacket.getData()[k] & 0xFF) <<  0 | (clusPacket.getData()[k+1] & 0xFF) <<  8 | (clusPacket.getData()[k+2] & 0xFF) <<  16 | (clusPacket.getData()[k+3] & 0xFF) <<  24);
			float fh = value/100;	
			
			finalcluster[l] = fh;
			k=k+4;
		}//getting centroid values from received bytes
		
		cackm[0] = (byte) 5;
		
		serverAddress = clusPacket.getAddress();
		int serverPort = clusPacket.getPort();
		DatagramPacket cackPacket = new DatagramPacket(cackm, cackm.length);
		// sending the UDP packet to the server 
	
			cackPacket.setAddress(serverAddress); // destination IP address
			cackPacket.setPort(serverPort); // destination port number
		clientSocket.send(cackPacket);
		if(c == 0) {
		System.out.println("centroids [m1(x,y),m2(x,y)], repectively, are: \n" + Arrays.toString(finalcluster)); //could print separately
		c = 1; //extra care so it wont print m1, m2 again when the duplicate clus arrives...
		}
		
		check = 1;
		} //type 4 if
	}//switcher =1 if
} //clus cack loop


clientSocket.close();
	}}
