import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FOTAServer {

    public static void main(String[] args) {
        int port = 10000;
        ServerSocket serverSocket = null;
        Socket socket = null;

        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Launch Server");
            while (true) {
                socket = serverSocket.accept();
                System.out.println("Connecting Completed");

                FileReceiver fl = new FileReceiver(socket);
                fl.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class FileReceiver extends Thread{
    Socket socket = null;
    InputStream inputStream = null;
    DataOutputStream dataOutputStream = null;

    String dirPath = "C:\\Users\\lsmn0\\Documents\\LSM\\FOTAImage\\";
//    String dirPath = "/home/pi/fotaserver";

    ArrayList<String> files = new ArrayList<>();

    public FileReceiver(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        boolean isValid = false;
        StringBuffer data = new StringBuffer();

        /*디렉토리 목록 불러오기*/
        /*
        File dirFile=new File(dirPath);
        File []fileList=dirFile.listFiles();
        for(File tempFile : fileList) {
            if(tempFile.isFile()) {
                String tempFileName = tempFile.getName();
                files.add(tempFile.getName());
                System.out.println(tempFileName);
            }
        }
         */
        File dirFile=new File(dirPath);
        File []fileList=dirFile.listFiles();
        File tempFile = fileList[0];
        String fileName = null;
        if(tempFile.isFile()) {
            fileName = tempFile.getName();
            System.out.println("FileName: " + fileName);
        }

        try {
            // Request 받기
            inputStream = socket.getInputStream();
            byte[] receiveBuffer = new byte[100];
            inputStream.read(receiveBuffer);

            // Request 파싱
            // Request 포맷 [0x02][DATA][0x0d][0x03]
            if(receiveBuffer[0] == 0x02) {
                System.out.println("Valid Request");
                for(int i = 1; receiveBuffer[i] != 0x03; i++) {
                    data.append((char)receiveBuffer[i]);
                    System.out.format("%x ", receiveBuffer[i]);
                }
                System.out.println("\nRequest Data: " + data.toString());
                isValid = true;
            } else {
                System.out.println("Invalid Request");
                isValid = false;
            }

            // Response
            // Response Valid: [0x02][DATA file name][0x0d][0x03] Invalid: 0x15
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.flush();

            sleep(1000);
            if (isValid) {
                int i;
                byte[] response = new byte[20];
                response[0] = 0x02;
                for(i = 0; fileName.charAt(i) != '.'; i++) {
                    response[i + 1] = (byte)fileName.charAt(i);
//                    System.out.print(fileName.charAt(i));
                }
                response[i + 1] = 0x0d;
                response[i + 2] = 0x03;
                dataOutputStream.write(response);
            } else {
                // NAK
                dataOutputStream.write(0x15);
            }

            System.out.println("Socket Close");
            inputStream.close();
            dataOutputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}