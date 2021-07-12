import java.io.*;
import java.util.*;
import javax.comm.*;
 
public class GSMConnect implements SerialPortEventListener, 
  CommPortOwnershipListener {
 
  private String comPort = "COM3"; // This COM Port must be connect with GSM Modem or your mobile phone
  private String messageString = "";
  private CommPortIdentifier portId = null;
  private Enumeration portList;
  private InputStream inputStream = null;
  private OutputStream outputStream = null;
  private SerialPort serialPort;
 
  /** Creates a new instance of GSMConnect */
  public GSMConnect(String comm) {
 
    this.comPort = comm;
 
  }
 
  public boolean init() {
    portList = CommPortIdentifier.getPortIdentifiers();
    while (portList.hasMoreElements()) {
      portId = (CommPortIdentifier) portList.nextElement();
      if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        if (portId.getName().equals(comPort)) {
          return true;
        }
      }
    }
    return false;
  }
 
  public void checkStatus() {
    send("AT+CREG?\r\n");
  }
 
  public void dial(String phoneNumber) {
    try {
//dial to this phone number
      messageString = "ATD" + phoneNumber + ";\n\r";
      outputStream.write(messageString.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
 
  public void send(String cmd) {
    try {
      outputStream.write(cmd.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
 
  public void sendMessage(String phoneNumber, String message) {
    send("AT+CMGS=\"" + phoneNumber + "\"\r\n");
    send(message + '\032');
  }
 
  public void hangup() {
    send("ATH\r\n");
  }
 
  public void connect() throws NullPointerException {
    if (portId != null) {
      try {
        portId.addPortOwnershipListener(this);
        serialPort = (SerialPort) portId.open("MobileGateWay", 2000);
      } catch (PortInUseException e) {
        e.printStackTrace();
      }
 
      try {
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
      } catch (IOException e) {
        e.printStackTrace();
      }
 
      try {
        /** These are the events we want to know about*/
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
      } catch (TooManyListenersException e) {
        e.printStackTrace();
      }
 
//Register to home network of sim card
 
      send("ATZ\r\n");
 
    } else {
      throw new NullPointerException("COM Port not found!!");
    }
  }
 
  public void serialEvent(javax.comm.SerialPortEvent serialPortEvent) {
    switch (serialPortEvent.getEventType()) {
      case SerialPortEvent.BI:
      case SerialPortEvent.OE:
      case SerialPortEvent.FE:
      case SerialPortEvent.PE:
      case SerialPortEvent.CD:
      case SerialPortEvent.CTS:
      case SerialPortEvent.DSR:
      case SerialPortEvent.RI:
      case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
      case SerialPortEvent.DATA_AVAILABLE:
 
        byte[] readBuffer = new byte[2048];
        try {
          while (inputStream.available() & gt;
          0) 
          {
            int numBytes = inputStream.read(readBuffer);
          }
//print response message
          System.out.print(new String(readBuffer));
        } catch (IOException e) {
        }
        break;
    }
  }
 
  public void ownershipChange(int type) {
    switch (type) {
      case CommPortOwnershipListener.PORT_UNOWNED:
        System.out.println(portId.getName() + ": PORT_UNOWNED");
        break;
      case CommPortOwnershipListener.PORT_OWNED:
        System.out.println(portId.getName() + ": PORT_OWNED");
        break;
      case CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED:
        System.out.println(portId.getName() + ": PORT_INUSED");
        break;
    }
 
  }
 
  public static void main(String args[]) {
    GSMConnect gsm = new GSMConnect(comPort);
    if (gsm.init()) {
      try {
        gsm.connect();
        gsm.checkStatus();
        Thread.sleep(5000);
        gsm.sendMessage("Mobile Phone Number", "Your Message");
        Thread.sleep(20000);
        gsm.hangup();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Can't init this card");
    }
  }
}